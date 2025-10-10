package br.com.cartoriodigital.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Digest encadeado de auditoria (Merkle root assinado com KMS).
 */
@Entity
@Table(name = "audit_digest")
public class AuditDigest {

    @Id
    @Column(name = "digest_id", nullable = false, updatable = false)
    private UUID digestId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "merkle_root", nullable = false, length = 128)
    private String merkleRoot;

    @Column(name = "kms_signature", length = 512)
    private String kmsSignature;

    @Column(name = "signed_at")
    private OffsetDateTime signedAt;

    protected AuditDigest() {
        // JPA
    }

    private AuditDigest(UUID digestId,
                        OffsetDateTime periodStart,
                        OffsetDateTime periodEnd,
                        String merkleRoot) {
        this.digestId = Objects.requireNonNull(digestId, "digestId");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd");
        this.merkleRoot = Objects.requireNonNull(merkleRoot, "merkleRoot");
    }

    public static AuditDigest create(OffsetDateTime periodStart,
                                     OffsetDateTime periodEnd,
                                     String merkleRoot) {
        return new AuditDigest(UUID.randomUUID(), periodStart, periodEnd, merkleRoot);
    }

    public void sign(String kmsSignature, OffsetDateTime signedAt) {
        this.kmsSignature = Objects.requireNonNull(kmsSignature, "kmsSignature");
        this.signedAt = Objects.requireNonNull(signedAt, "signedAt");
    }

    public boolean isSigned() {
        return kmsSignature != null;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }
}
