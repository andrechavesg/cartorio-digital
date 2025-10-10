package br.com.cartoriodigital.revocation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de aprovação para revogação com assinatura digital.
 */
@Embeddable
public class ApprovalDecision {

    @Column(name = "approval_id", nullable = false, length = 36)
    private String approvalId;

    @Column(name = "approval_actor_id", nullable = false, length = 64)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role", nullable = false, length = 64)
    private ApprovalActorRole role;

    @Column(name = "approval_signature", nullable = false, length = 512)
    private String signature;

    @Column(name = "approval_at", nullable = false)
    private OffsetDateTime approvedAt;

    protected ApprovalDecision() {
        // JPA
    }

    private ApprovalDecision(UUID approvalId, String actorId, ApprovalActorRole role, String signature, OffsetDateTime approvedAt) {
        this.approvalId = Objects.requireNonNull(approvalId, "approvalId").toString();
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.role = Objects.requireNonNull(role, "role");
        this.signature = Objects.requireNonNull(signature, "signature");
        this.approvedAt = Objects.requireNonNull(approvedAt, "approvedAt");
    }

    public static ApprovalDecision of(String actorId, ApprovalActorRole role, String signature, OffsetDateTime approvedAt) {
        return new ApprovalDecision(UUID.randomUUID(), actorId, role, signature, approvedAt);
    }

    public UUID approvalId() {
        return UUID.fromString(approvalId);
    }

    public String actorId() {
        return actorId;
    }

    public ApprovalActorRole role() {
        return role;
    }

    public String signature() {
        return signature;
    }

    public OffsetDateTime approvedAt() {
        return approvedAt;
    }
}
