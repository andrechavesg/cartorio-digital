package br.com.cartoriodigital.enrollment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;
import java.util.Objects;

@Embeddable
public class EnrollmentEvidence {

    @Column(name = "evidence_type", nullable = false, length = 64)
    private String type;

    @Column(name = "evidence_uri", nullable = false, length = 512)
    private String uri;

    @Column(name = "evidence_hash", nullable = false, length = 128)
    private String hash;

    @Column(name = "evidence_collected_at", nullable = false)
    private OffsetDateTime collectedAt;

    protected EnrollmentEvidence() {
        // JPA
    }

    private EnrollmentEvidence(String type, String uri, String hash, OffsetDateTime collectedAt) {
        this.type = Objects.requireNonNull(type, "type");
        this.uri = Objects.requireNonNull(uri, "uri");
        this.hash = Objects.requireNonNull(hash, "hash");
        this.collectedAt = Objects.requireNonNull(collectedAt, "collectedAt");
    }

    public static EnrollmentEvidence of(String type, String uri, String hash, OffsetDateTime collectedAt) {
        return new EnrollmentEvidence(type, uri, hash, collectedAt);
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getHash() {
        return hash;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }
}
