package br.com.cartoriodigital.issuance.application;

import br.com.cartoriodigital.issuance.domain.CertificateOrder;
import br.com.cartoriodigital.issuance.domain.CertificateOrderRepository;
import br.com.cartoriodigital.issuance.domain.CertificateOrderStatus;
import br.com.cartoriodigital.issuance.domain.IssuedCertificate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CertificateOrderService {

    private final CertificateOrderRepository repository;

    @Inject
    public CertificateOrderService(CertificateOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CertificateOrder create(CreateOrderCommand command) {
        CertificateOrder order = CertificateOrder.create(
                command.enrollmentId(),
                command.profileId(),
                command.csrPem(),
                command.caAlias(),
                command.requestedAt()
        );
        repository.save(order);
        return order;
    }

    @Transactional
    public CertificateOrder queue(UUID orderId, QueueOrderCommand command) {
        CertificateOrder order = load(orderId);
        order.queue(command.actorId(), command.queuedAt());
        repository.save(order);
        return order;
    }

    @Transactional
    public CertificateOrder markSigning(UUID orderId, TransitionCommand command) {
        CertificateOrder order = load(orderId);
        order.markSigning(command.actorId(), command.at());
        repository.save(order);
        return order;
    }

    @Transactional
    public CertificateOrder complete(UUID orderId, CompleteOrderCommand command) {
        CertificateOrder order = load(orderId);
        IssuedCertificate certificate = IssuedCertificate.of(
                command.certificatePem(),
                command.certificateChainPem(),
                command.pkcs12Uri(),
                command.issuedAt()
        );
        order.complete(command.actorId(), certificate, command.issuedAt());
        repository.save(order);
        return order;
    }

    @Transactional
    public CertificateOrder fail(UUID orderId, FailureCommand command) {
        CertificateOrder order = load(orderId);
        order.fail(command.actorId(), command.reason(), command.failedAt());
        repository.save(order);
        return order;
    }

    @Transactional
    public CertificateOrder revoke(UUID orderId, RevokeCommand command) {
        CertificateOrder order = load(orderId);
        order.revoke(command.actorId(), command.reason(), command.revokedAt());
        repository.save(order);
        return order;
    }

    public CertificateOrder getById(UUID orderId) {
        return load(orderId);
    }

    public List<CertificateOrder> listByEnrollment(String enrollmentId) {
        return repository.findByEnrollmentId(enrollmentId);
    }

    private CertificateOrder load(UUID orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Certificate order not found: " + orderId));
    }

    public record CreateOrderCommand(
            String enrollmentId,
            String profileId,
            String csrPem,
            String caAlias,
            OffsetDateTime requestedAt
    ) {
    }

    public record QueueOrderCommand(
            String actorId,
            OffsetDateTime queuedAt
    ) {
    }

    public record TransitionCommand(
            String actorId,
            OffsetDateTime at
    ) {
    }

    public record CompleteOrderCommand(
            String actorId,
            String certificatePem,
            String certificateChainPem,
            String pkcs12Uri,
            OffsetDateTime issuedAt
    ) {
    }

    public record FailureCommand(
            String actorId,
            String reason,
            OffsetDateTime failedAt
    ) {
    }

    public record RevokeCommand(
            String actorId,
            String reason,
            OffsetDateTime revokedAt
    ) {
    }

    public CertificateOrderStatus statusOf(UUID orderId) {
        return getById(orderId).getStatus();
    }
}
