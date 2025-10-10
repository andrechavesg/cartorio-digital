package br.com.cartoriodigital.publisher.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agrupa a publicação de artefatos (CRL, delta CRL, notificações OCSP) com seus destinos.
 */
@Entity
@Table(name = "publication_batch")
public class PublicationBatch {

    @Id
    @Column(name = "batch_id", nullable = false, updatable = false)
    private UUID batchId;

    @Column(name = "source_trigger", nullable = false, length = 64)
    private String sourceTrigger;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PublicationStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "error_reason", length = 512)
    private String errorReason;

    @ElementCollection
    @CollectionTable(name = "publication_artifact", joinColumns = @JoinColumn(name = "batch_id"))
    private List<PublishedArtifact> artifacts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "publication_endpoint", joinColumns = @JoinColumn(name = "batch_id"))
    private List<DistributionEndpoint> endpoints = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected PublicationBatch() {
        // JPA
    }

    private PublicationBatch(UUID batchId,
                             String sourceTrigger,
                             OffsetDateTime scheduledAt,
                             List<DistributionEndpoint> endpoints) {
        this.batchId = Objects.requireNonNull(batchId, "batchId");
        this.sourceTrigger = Objects.requireNonNull(sourceTrigger, "sourceTrigger");
        this.scheduledAt = Objects.requireNonNull(scheduledAt, "scheduledAt");
        this.status = PublicationStatus.CREATED;
        this.endpoints.addAll(endpoints);
    }

    public static PublicationBatch schedule(String sourceTrigger,
                                            OffsetDateTime scheduledAt,
                                            List<DistributionEndpoint> endpoints) {
        return new PublicationBatch(UUID.randomUUID(), sourceTrigger, scheduledAt, endpoints);
    }

    public void addArtifact(PublishedArtifact artifact) {
        ensureNotFinished();
        artifacts.add(Objects.requireNonNull(artifact, "artifact"));
    }

    public void start() {
        ensureNotFinished();
        this.status = PublicationStatus.IN_PROGRESS;
        this.startedAt = OffsetDateTime.now();
    }

    public void complete() {
        ensureNotFinished();
        if (artifacts.isEmpty()) {
            throw new IllegalStateException("Cannot complete publication without artifacts");
        }
        this.status = PublicationStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    public void fail(String reason) {
        ensureNotFinished();
        this.status = PublicationStatus.FAILED;
        this.errorReason = Objects.requireNonNull(reason, "reason");
        this.completedAt = OffsetDateTime.now();
    }

    private void ensureNotFinished() {
        if (status == PublicationStatus.COMPLETED || status == PublicationStatus.FAILED) {
            throw new IllegalStateException("Batch already finalized");
        }
    }

    @PrePersist
    void prePersist() {
        if (batchId == null) {
            batchId = UUID.randomUUID();
        }
        if (scheduledAt == null) {
            scheduledAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        // noop
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public List<PublishedArtifact> getArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    public String getErrorReason() {
        return errorReason;
    }
}
