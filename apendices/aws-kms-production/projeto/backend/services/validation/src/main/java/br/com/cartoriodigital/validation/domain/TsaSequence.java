package br.com.cartoriodigital.validation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Contador monotônico da TSA conforme DOC-ICP-15.
 */
@Entity
@Table(name = "tsa_sequence")
public class TsaSequence {

    @Id
    @Column(name = "tsa_id", nullable = false, length = 64)
    private String tsaId;

    @Column(name = "current_value", nullable = false)
    private long currentValue;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected TsaSequence() {
        // JPA
    }

    private TsaSequence(String tsaId, long currentValue, OffsetDateTime updatedAt) {
        this.tsaId = Objects.requireNonNull(tsaId, "tsaId");
        this.currentValue = currentValue;
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }

    public static TsaSequence bootstrap(String tsaId, OffsetDateTime createdAt) {
        return new TsaSequence(tsaId, 0L, createdAt);
    }

    public long nextValue() {
        currentValue++;
        updatedAt = OffsetDateTime.now();
        return currentValue;
    }

    public long currentValue() {
        return currentValue;
    }

    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
}
