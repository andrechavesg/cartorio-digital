package br.com.cartoriodigital.validation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro persistente de tokens de carimbo do tempo emitidos.
 */
@Entity
@Table(name = "tsa_token_record")
public class TimestampTokenRecord {

    @Id
    @Column(name = "token_id", nullable = false, updatable = false)
    private UUID tokenId;

    @Column(name = "sequence_value", nullable = false)
    private long sequenceValue;

    @Column(name = "hash_algorithm", nullable = false, length = 32)
    private String hashAlgorithm;

    @Column(name = "message_imprint", nullable = false, length = 256)
    private String messageImprint;

    @Column(name = "token_uri", nullable = false, length = 512)
    private String tokenUri;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    protected TimestampTokenRecord() {
        // JPA
    }

    private TimestampTokenRecord(long sequenceValue,
                                 String hashAlgorithm,
                                 String messageImprint,
                                 String tokenUri,
                                 OffsetDateTime issuedAt) {
        this.tokenId = UUID.randomUUID();
        this.sequenceValue = sequenceValue;
        this.hashAlgorithm = Objects.requireNonNull(hashAlgorithm, "hashAlgorithm");
        this.messageImprint = Objects.requireNonNull(messageImprint, "messageImprint");
        this.tokenUri = Objects.requireNonNull(tokenUri, "tokenUri");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
    }

    public static TimestampTokenRecord create(long sequenceValue,
                                              String hashAlgorithm,
                                              String messageImprint,
                                              String tokenUri,
                                              OffsetDateTime issuedAt) {
        return new TimestampTokenRecord(sequenceValue, hashAlgorithm, messageImprint, tokenUri, issuedAt);
    }

    public long getSequenceValue() {
        return sequenceValue;
    }
}
