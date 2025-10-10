package br.com.cartoriodigital.publisher.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Artefato publicado (CRL, Delta CRL, OCSP snapshot, relatório, etc).
 */
@Embeddable
public class PublishedArtifact {

    @Column(name = "artifact_type", nullable = false, length = 32)
    private String type;

    @Column(name = "artifact_uri", nullable = false, length = 512)
    private String uri;

    @Column(name = "artifact_hash", nullable = false, length = 128)
    private String hash;

    protected PublishedArtifact() {
        // JPA
    }

    private PublishedArtifact(String type, String uri, String hash) {
        this.type = Objects.requireNonNull(type, "type");
        this.uri = Objects.requireNonNull(uri, "uri");
        this.hash = Objects.requireNonNull(hash, "hash");
    }

    public static PublishedArtifact of(String type, String uri, String hash) {
        return new PublishedArtifact(type, uri, hash);
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
}
