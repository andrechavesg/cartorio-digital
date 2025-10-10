package br.com.cartoriodigital.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Solicitação de exportação de relatórios para auditoria externa.
 */
@Entity
@Table(name = "audit_export_request")
public class AuditExportRequest {

    public enum ExportFormat {
        CSV,
        JSON
    }

    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 16)
    private ExportFormat format;

    @Column(name = "requested_by", nullable = false, length = 64)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "artifact_uri", length = 512)
    private String artifactUri;

    protected AuditExportRequest() {
        // JPA
    }

    private AuditExportRequest(UUID requestId,
                               ExportFormat format,
                               String requestedBy,
                               OffsetDateTime requestedAt) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.format = Objects.requireNonNull(format, "format");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
        this.status = "REQUESTED";
    }

    public static AuditExportRequest create(ExportFormat format,
                                            String requestedBy,
                                            OffsetDateTime requestedAt) {
        return new AuditExportRequest(UUID.randomUUID(), format, requestedBy, requestedAt);
    }

    public void markCompleted(String artifactUri) {
        this.status = "COMPLETED";
        this.artifactUri = Objects.requireNonNull(artifactUri, "artifactUri");
    }

    public String getStatus() {
        return status;
    }

    public String getArtifactUri() {
        return artifactUri;
    }
}
