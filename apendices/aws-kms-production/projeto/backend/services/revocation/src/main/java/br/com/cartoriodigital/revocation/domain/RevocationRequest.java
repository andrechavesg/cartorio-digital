package br.com.cartoriodigital.revocation.domain;

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
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Agregado para controlar pedidos de revogação com dupla custódia.
 */
@Entity
@Table(name = "revocation_request")
public class RevocationRequest {

    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Column(name = "certificate_serial", nullable = false, length = 64)
    private String certificateSerial;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 64)
    private RevocationReason reason;

    @Column(name = "requested_by", nullable = false, length = 64)
    private String requestedBy;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 48)
    private RevocationStatus status;

    @ElementCollection
    @CollectionTable(name = "revocation_approval", joinColumns = @JoinColumn(name = "request_id"))
    private List<ApprovalDecision> approvals = new ArrayList<>();

    @Column(name = "rejection_reason", length = 512)
    private String rejectionReason;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected RevocationRequest() {
        // JPA
    }

    private RevocationRequest(UUID requestId,
                              String certificateSerial,
                              RevocationReason reason,
                              String requestedBy,
                              OffsetDateTime requestedAt) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.certificateSerial = Objects.requireNonNull(certificateSerial, "certificateSerial");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy");
        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
        this.status = RevocationStatus.PENDING_REVIEW;
    }

    public static RevocationRequest create(String certificateSerial,
                                           RevocationReason reason,
                                           String requester,
                                           OffsetDateTime requestedAt) {
        return new RevocationRequest(UUID.randomUUID(), certificateSerial, reason, requester, requestedAt);
    }

    public void registerApproval(ApprovalDecision decision) {
        ensureNotTerminal();
        boolean roleAlreadyApproved = approvals.stream()
                .anyMatch(existing -> existing.role() == decision.role());
        if (roleAlreadyApproved) {
            throw new IllegalArgumentException("Role already approved: " + decision.role());
        }

        approvals.add(decision);
        if (status == RevocationStatus.PENDING_REVIEW && decision.role() == ApprovalActorRole.RA_AGENT) {
            status = RevocationStatus.AWAITING_SECURITY_APPROVAL;
        }
        if (hasDualControlApprovals()) {
            status = RevocationStatus.APPROVED;
        }
    }

    public void reject(String reason, ApprovalDecision decision) {
        ensureNotTerminal();
        this.rejectionReason = Objects.requireNonNull(reason, "reason");
        this.status = RevocationStatus.REJECTED;
        approvals.add(decision);
    }

    public void markCompleted(OffsetDateTime completedAt) {
        if (status != RevocationStatus.APPROVED) {
            throw new IllegalStateException("Only approved requests can be completed");
        }
        this.status = RevocationStatus.COMPLETED;
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
    }

    private boolean hasDualControlApprovals() {
        Set<ApprovalActorRole> roles = approvals.stream()
                .map(ApprovalDecision::role)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ApprovalActorRole.class)));
        return roles.contains(ApprovalActorRole.RA_AGENT) && roles.contains(ApprovalActorRole.SECURITY_OFFICER);
    }

    private void ensureNotTerminal() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Revocation already closed");
        }
    }

    @PrePersist
    void prePersist() {
        if (requestId == null) {
            requestId = UUID.randomUUID();
        }
        if (requestedAt == null) {
            requestedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        // nothing special for now
    }

    public RevocationStatus getStatus() {
        return status;
    }

    public List<ApprovalDecision> getApprovals() {
        return Collections.unmodifiableList(approvals);
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
}
