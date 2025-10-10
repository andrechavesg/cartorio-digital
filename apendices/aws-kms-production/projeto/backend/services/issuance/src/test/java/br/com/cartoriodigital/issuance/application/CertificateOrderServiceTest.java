package br.com.cartoriodigital.issuance.application;

import br.com.cartoriodigital.issuance.application.CertificateOrderService.CompleteOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.CreateOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.FailureCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.QueueOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.RevokeCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.TransitionCommand;
import br.com.cartoriodigital.issuance.domain.CertificateOrder;
import br.com.cartoriodigital.issuance.domain.CertificateOrderRepository;
import br.com.cartoriodigital.issuance.domain.CertificateOrderStatus;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CertificateOrderServiceTest {

    private CertificateOrderService service;
    private InMemoryCertificateOrderRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryCertificateOrderRepository();
        service = new CertificateOrderService(repository);
    }

    @Test
    void lifecycleCompletesSuccessfully() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = service.create(new CreateOrderCommand(
                "enrollment-1",
                "TLS-Server",
                "-----BEGIN CSR-----",
                "ac-raiz",
                now
        ));

        service.queue(order.getOrderId(), new QueueOrderCommand("operator-1", now.plusSeconds(5)));
        service.markSigning(order.getOrderId(), new TransitionCommand("orchestrator", now.plusSeconds(10)));
        service.complete(order.getOrderId(), new CompleteOrderCommand(
                "signer-1",
                "-----BEGIN CERT-----",
                "-----BEGIN CHAIN-----",
                "s3://certs/order-1.p12",
                now.plusSeconds(20)
        ));

        CertificateOrder updated = service.getById(order.getOrderId());
        assertEquals(CertificateOrderStatus.SIGNED, updated.getStatus());
        assertEquals("s3://certs/order-1.p12", updated.getIssuedCertificate().getPkcs12ObjectUri());
    }

    @Test
    void failTransitionSetsFailureReason() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = service.create(new CreateOrderCommand(
                "enrollment-2",
                "TLS-Server",
                "CSR",
                "ac-raiz",
                now
        ));

        service.fail(order.getOrderId(), new FailureCommand("operator", "KMS unavailable", now.plusSeconds(2)));

        CertificateOrder failed = service.getById(order.getOrderId());
        assertEquals(CertificateOrderStatus.FAILED, failed.getStatus());
        assertEquals("KMS unavailable", failed.getFailureReason());
    }

    @Test
    void revocationRequiresSignedState() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = service.create(new CreateOrderCommand(
                "enrollment-3",
                "TLS-Server",
                "CSR",
                "ac-raiz",
                now
        ));

        assertThrows(IllegalStateException.class, () -> service.revoke(order.getOrderId(), new RevokeCommand("operator", "compromise", now.plusMinutes(1))));
    }

    @Test
    void unknownOrderThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(UUID.randomUUID()));
    }

    private static class InMemoryCertificateOrderRepository implements CertificateOrderRepository {
        private final ConcurrentHashMap<UUID, CertificateOrder> storage = new ConcurrentHashMap<>();

        @Override
        public CertificateOrder save(CertificateOrder order) {
            storage.put(order.getOrderId(), order);
            return order;
        }

        @Override
        public Optional<CertificateOrder> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<CertificateOrder> findByEnrollmentId(String enrollmentId) {
            return storage.values().stream()
                    .filter(order -> order.getEnrollmentId().equals(enrollmentId))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
}
