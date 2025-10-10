package br.com.cartoriodigital.issuance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Registro de auditoria interno para mudanças de estado da emissão.
 */
@Embeddable
public class IssuanceAuditEntry {

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_event", nullable = false, length = 64)
    private IssuanceEventType eventType;

    @Column(name = "audit_actor_id", nullable = false, length = 64)
    private String actorId;

    @Column(name = "audit_details", length = 512)
    private String details;

    @Column(name = "audit_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected IssuanceAuditEntry() {
        // JPA
    }

    private IssuanceAuditEntry(IssuanceEventType eventType, String actorId, String details, OffsetDateTime occurredAt) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.actorId = Objects.requireNonNull(actorId, "actorId");
        this.details = details;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public static IssuanceAuditEntry record(IssuanceEventType eventType, String actorId, String details, OffsetDateTime occurredAt) {
        return new IssuanceAuditEntry(eventType, actorId, details, occurredAt);
    }

    public IssuanceEventType getEventType() {
        return eventType;
    }

    public String getActorId() {
        return actorId;
    }

    public String getDetails() {
        return details;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
