package br.com.cartoriodigital.issuance.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CertificateOrderTest {

    @Test
    void completesSuccessfulFlow() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = CertificateOrder.create("enrollment-1", "TLS-Server-Profile", "csr-pem", "ca-root", now);

        order.queue("orchestrator", now.plusSeconds(5));
        order.markSigning("signer", now.plusSeconds(10));
        IssuedCertificate certificate = order.complete("signer", IssuedCertificate.of("pem", "chain", "s3://p12", now.plusSeconds(20)), now.plusSeconds(20));

        assertEquals(CertificateOrderStatus.SIGNED, order.getStatus());
        assertEquals("pem", certificate.getCertificatePem());
        assertEquals(4, order.getAuditTrail().size()); // received + queued + signing + completed
    }

    @Test
    void preventsInvalidTransition() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = CertificateOrder.create("enrollment-1", "TLS-Server-Profile", "csr-pem", "ca-root", now);
        assertThrows(IllegalStateException.class, () -> order.markSigning("actor", now));
    }

    @Test
    void revokeOnlyAfterSigning() {
        OffsetDateTime now = OffsetDateTime.now();
        CertificateOrder order = CertificateOrder.create("enrollment-1", "TLS-Server-Profile", "csr-pem", "ca-root", now);
        assertThrows(IllegalStateException.class, () -> order.revoke("actor", "invalid", now));
    }
}
