package br.com.cartoriodigital.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro imutável das ações realizadas por agentes de registro.
 */
@Entity
@Table(name = "ra_action_log")
public class RAActionLog {

    @Id
    @Column(name = "action_id", nullable = false, updatable = false)
    private UUID actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Column(name = "actor_id", nullable = false, length = 64)
    private String actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_role", nullable = false, length = 32)
    private RAActorRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 48)
    private RAActionType action;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "signature", nullable = false, length = 512)
    private String signature;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RAActionLog() {
        // JPA
    }

    private RAActionLog(Applicant applicant,
                        String actorId,
                        RAActorRole role,
                        RAActionType action,
                        String reason,
                        String signature,
                        OffsetDateTime createdAt) {
        this.actionId = UUID.randomUUID();
        this.applicant = Objects.requireNonNull(applicant, "applicant");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.role = Objects.requireNonNull(role, "role");
        this.action = Objects.requireNonNull(action, "action");
        this.reason = reason;
        this.signature = Objects.requireNonNull(signature, "signature");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static RAActionLog create(Applicant applicant,
                                     String actorId,
                                     RAActorRole role,
                                     RAActionType action,
                                     String reason,
                                     String signature,
                                     OffsetDateTime createdAt) {
        return new RAActionLog(applicant, actorId, role, action, reason, signature, createdAt);
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (actionId == null) {
            actionId = UUID.randomUUID();
        }
    }

    @PreUpdate
    void preventUpdate() {
        throw new UnsupportedOperationException("RAActionLog is immutable");
    }

    public UUID getActionId() {
        return actionId;
    }

    public RAActionType getAction() {
        return action;
    }

    public RAActorRole getRole() {
        return role;
    }

    public String getActorId() {
        return actorId;
    }

    public String getReason() {
        return reason;
    }

    public String getSignature() {
        return signature;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
