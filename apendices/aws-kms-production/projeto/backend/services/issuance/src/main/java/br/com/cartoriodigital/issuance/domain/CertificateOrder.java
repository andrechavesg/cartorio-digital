package br.com.cartoriodigital.issuance.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado que representa um pedido de emissão de certificado conforme fluxos REST/CMP/SCEP/ACME.
 */
@Entity
@Table(name = "certificate_order")
public class CertificateOrder {

    @Id
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "enrollment_id", nullable = false, length = 36)
    private String enrollmentId;

    @Column(name = "profile_id", nullable = false, length = 64)
    private String profileId;

    @Column(name = "csr_pem", nullable = false, length = 8192)
    private String csrPem;

    @Column(name = "ca_alias", nullable = false, length = 128)
    private String caAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CertificateOrderStatus status;

    @Embedded
    private IssuedCertificate issuedCertificate;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ElementCollection
    @CollectionTable(name = "certificate_order_audit", joinColumns = @JoinColumn(name = "order_id"))
    private List<IssuanceAuditEntry> auditTrail = new ArrayList<>();

    protected CertificateOrder() {
        // JPA
    }

    private CertificateOrder(UUID orderId,
                             String enrollmentId,
                             String profileId,
                             String csrPem,
                             String caAlias,
                             OffsetDateTime requestedAt) {
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.enrollmentId = Objects.requireNonNull(enrollmentId, "enrollmentId");
        this.profileId = Objects.requireNonNull(profileId, "profileId");
        this.csrPem = Objects.requireNonNull(csrPem, "csrPem");
        this.caAlias = Objects.requireNonNull(caAlias, "caAlias");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
        this.updatedAt = requestedAt;
        this.status = CertificateOrderStatus.REQUESTED;
        registerAudit(IssuanceEventType.ORDER_RECEIVED, "system", "Pedido criado", requestedAt);
    }

    public static CertificateOrder create(String enrollmentId,
                                          String profileId,
                                          String csrPem,
                                          String caAlias,
                                          OffsetDateTime requestedAt) {
        return new CertificateOrder(UUID.randomUUID(), enrollmentId, profileId, csrPem, caAlias, requestedAt);
    }

    public void queue(String actorId, OffsetDateTime queuedAt) {
        transitionTo(CertificateOrderStatus.QUEUED, actorId, queuedAt, IssuanceEventType.ORDER_QUEUED, null);
    }

    public void markSigning(String actorId, OffsetDateTime signingAt) {
        transitionTo(CertificateOrderStatus.SIGNING, actorId, signingAt, IssuanceEventType.SIGNING_STARTED, null);
    }

    public IssuedCertificate complete(String actorId, IssuedCertificate certificate, OffsetDateTime completedAt) {
        Objects.requireNonNull(certificate, "certificate");
        transitionTo(CertificateOrderStatus.SIGNED, actorId, completedAt, IssuanceEventType.SIGNING_COMPLETED, null);
        this.issuedCertificate = certificate;
        return certificate;
    }

    public void fail(String actorId, String reason, OffsetDateTime failedAt) {
        Objects.requireNonNull(reason, "reason");
        transitionTo(CertificateOrderStatus.FAILED, actorId, failedAt, IssuanceEventType.ORDER_FAILED, reason);
        this.failureReason = reason;
    }

    public void revoke(String actorId, String reason, OffsetDateTime revokedAt) {
        if (status != CertificateOrderStatus.SIGNED) {
            throw new IllegalStateException("Only signed certificates can be revoked");
        }
        this.status = CertificateOrderStatus.REVOKED;
        registerAudit(IssuanceEventType.ORDER_REVOKED, actorId, reason, revokedAt);
        this.revokedAt = revokedAt;
        touch(revokedAt);
    }

    private void transitionTo(CertificateOrderStatus target,
                              String actorId,
                              OffsetDateTime at,
                              IssuanceEventType eventType,
                              String details) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Transition from %s to %s is not allowed".formatted(status, target));
        }
        this.status = target;
        registerAudit(eventType, actorId, details, at);
        touch(at);
    }

    private void registerAudit(IssuanceEventType eventType, String actorId, String details, OffsetDateTime at) {
        auditTrail.add(IssuanceAuditEntry.record(eventType, actorId, details, at));
    }

    private void touch(OffsetDateTime at) {
        this.updatedAt = Objects.requireNonNullElseGet(at, OffsetDateTime::now);
    }

    @PrePersist
    void prePersist() {
        if (orderId == null) {
            orderId = UUID.randomUUID();
        }
        if (requestedAt == null) {
            requestedAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = requestedAt;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public CertificateOrderStatus getStatus() {
        return status;
    }

    public List<IssuanceAuditEntry> getAuditTrail() {
        return Collections.unmodifiableList(auditTrail);
    }

    public IssuedCertificate getIssuedCertificate() {
        return issuedCertificate;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getCsrPem() {
        return csrPem;
    }

    public String getCaAlias() {
        return caAlias;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
