package br.com.cartoriodigital.enrollment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class EnrollmentDecision {

    @Column(name = "decision_id", nullable = false, length = 36)
    private String decisionId;

    @Column(name = "decision_actor_id", nullable = false, length = 64)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_role", nullable = false, length = 32)
    private ApprovalRole role;

    @Column(name = "decision_signature", nullable = false, length = 512)
    private String signature;

    @Column(name = "decision_at", nullable = false)
    private OffsetDateTime decidedAt;

    @Column(name = "decision_justification", length = 512)
    private String justification;

    protected EnrollmentDecision() {
        // JPA
    }

    private EnrollmentDecision(UUID decisionId,
                               String actorId,
                               ApprovalRole role,
                               String signature,
                               OffsetDateTime decidedAt,
                               String justification) {
        this.decisionId = Objects.requireNonNull(decisionId, "decisionId").toString();
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.role = Objects.requireNonNull(role, "role");
        this.signature = Objects.requireNonNull(signature, "signature");
        this.decidedAt = Objects.requireNonNull(decidedAt, "decidedAt");
        this.justification = justification;
    }

    public static EnrollmentDecision approve(String actorId,
                                             ApprovalRole role,
                                             String signature,
                                             OffsetDateTime decidedAt,
                                             String justification) {
        return new EnrollmentDecision(UUID.randomUUID(), actorId, role, signature, decidedAt, justification);
    }

    public static EnrollmentDecision reject(String actorId,
                                            String signature,
                                            OffsetDateTime decidedAt,
                                            String justification) {
        return new EnrollmentDecision(UUID.randomUUID(), actorId, ApprovalRole.RA_AGENT, signature, decidedAt, justification);
    }

    public UUID decisionId() {
        return UUID.fromString(decisionId);
    }

    public String actorId() {
        return actorId;
    }

    public ApprovalRole role() {
        return role;
    }

    public String signature() {
        return signature;
    }

    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    public String justification() {
        return justification;
    }
}
