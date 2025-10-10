package br.com.cartoriodigital.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Evento auditável recebido via Kinesis/Firehose.
 */
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    private AuditEventType type;

    @Column(name = "source_service", nullable = false, length = 64)
    private String sourceService;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Column(name = "integrity_proof", nullable = false, length = 256)
    private String integrityProof;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected AuditEvent() {
        // JPA
    }

    private AuditEvent(UUID eventId,
                       AuditEventType type,
                       String sourceService,
                       String payloadHash,
                       String integrityProof,
                       OffsetDateTime occurredAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.type = Objects.requireNonNull(type, "type");
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService");
        this.payloadHash = Objects.requireNonNull(payloadHash, "payloadHash");
        this.integrityProof = Objects.requireNonNull(integrityProof, "integrityProof");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public static AuditEvent create(AuditEventType type,
                                    String sourceService,
                                    String payloadHash,
                                    String integrityProof,
                                    OffsetDateTime occurredAt) {
        return new AuditEvent(UUID.randomUUID(), type, sourceService, payloadHash, integrityProof, occurredAt);
    }

    public void reinforceProof(String newIntegrityProof) {
        this.integrityProof = Objects.requireNonNull(newIntegrityProof, "integrityProof");
    }

    @PrePersist
    void prePersist() {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
    }

    public AuditEventType getType() {
        return type;
    }

    public String getIntegrityProof() {
        return integrityProof;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
