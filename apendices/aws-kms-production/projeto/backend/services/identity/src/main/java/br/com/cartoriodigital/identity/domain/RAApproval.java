package br.com.cartoriodigital.identity.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma aprovação assinada digitalmente por um ator da RA.
 */
public final class RAApproval {
    private final UUID approvalId;
    private final String actorId;
    private final RAActorRole role;
    private final String signature;
    private final OffsetDateTime approvedAt;

    private RAApproval(UUID approvalId, String actorId, RAActorRole role, String signature, OffsetDateTime approvedAt) {
        this.approvalId = Objects.requireNonNull(approvalId, "approvalId");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.role = Objects.requireNonNull(role, "role");
        this.signature = Objects.requireNonNull(signature, "signature");
        this.approvedAt = Objects.requireNonNull(approvedAt, "approvedAt");
    }

    public static RAApproval of(String actorId, RAActorRole role, String signature, OffsetDateTime approvedAt) {
        return new RAApproval(UUID.randomUUID(), actorId, role, signature, approvedAt);
    }

    public UUID approvalId() {
        return approvalId;
    }

    public String actorId() {
        return actorId;
    }

    public RAActorRole role() {
        return role;
    }

    public String signature() {
        return signature;
    }

    public OffsetDateTime approvedAt() {
        return approvedAt;
    }
}
