package br.com.cartoriodigital.validation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa um item do cache OCSP local para respostas em baixa latência.
 */
@Entity
@Table(name = "ocsp_cache_entry")
public class OcspCacheEntry {

    @Id
    @Column(name = "entry_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "certificate_serial", nullable = false, length = 64, unique = true)
    private String certificateSerial;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OcspStatusCode status;

    @Column(name = "this_update", nullable = false)
    private OffsetDateTime thisUpdate;

    @Column(name = "next_update", nullable = false)
    private OffsetDateTime nextUpdate;

    @Column(name = "revocation_reason", length = 64)
    private String revocationReason;

    @Column(name = "revocation_time")
    private OffsetDateTime revocationTime;

    @Column(name = "produced_at", nullable = false)
    private OffsetDateTime producedAt;

    protected OcspCacheEntry() {
        // JPA
    }

    private OcspCacheEntry(String certificateSerial,
                           OcspStatusCode status,
                           OffsetDateTime thisUpdate,
                           OffsetDateTime nextUpdate,
                           OffsetDateTime producedAt) {
        this.id = UUID.randomUUID();
        this.certificateSerial = Objects.requireNonNull(certificateSerial, "certificateSerial");
        this.status = Objects.requireNonNull(status, "status");
        this.thisUpdate = Objects.requireNonNull(thisUpdate, "thisUpdate");
        this.nextUpdate = Objects.requireNonNull(nextUpdate, "nextUpdate");
        this.producedAt = Objects.requireNonNull(producedAt, "producedAt");
    }

    public static OcspCacheEntry good(String certificateSerial, OffsetDateTime thisUpdate, OffsetDateTime nextUpdate) {
        return new OcspCacheEntry(certificateSerial, OcspStatusCode.GOOD, thisUpdate, nextUpdate, thisUpdate);
    }

    public void markRevoked(String reason, OffsetDateTime revocationTime, OffsetDateTime thisUpdate, OffsetDateTime nextUpdate) {
        this.status = OcspStatusCode.REVOKED;
        this.revocationReason = Objects.requireNonNull(reason, "reason");
        this.revocationTime = Objects.requireNonNull(revocationTime, "revocationTime");
        this.thisUpdate = Objects.requireNonNull(thisUpdate, "thisUpdate");
        this.nextUpdate = Objects.requireNonNull(nextUpdate, "nextUpdate");
        this.producedAt = thisUpdate;
    }

    public void refresh(OffsetDateTime thisUpdate, OffsetDateTime nextUpdate) {
        this.thisUpdate = Objects.requireNonNull(thisUpdate, "thisUpdate");
        this.nextUpdate = Objects.requireNonNull(nextUpdate, "nextUpdate");
        this.producedAt = thisUpdate;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @PreUpdate
    void preUpdate() {
        // nothing yet
    }

    public OcspStatusCode getStatus() {
        return status;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public OffsetDateTime getRevocationTime() {
        return revocationTime;
    }

    public OffsetDateTime getNextUpdate() {
        return nextUpdate;
    }
}
