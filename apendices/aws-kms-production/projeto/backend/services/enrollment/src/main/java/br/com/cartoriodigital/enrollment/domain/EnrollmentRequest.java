package br.com.cartoriodigital.enrollment.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
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

@Entity
@Table(name = "enrollment_request")
public class EnrollmentRequest {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "applicant_id", nullable = false, length = 36)
    private String applicantId;

    @Column(name = "profile_id", nullable = false, length = 64)
    private String profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private EnrollmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ElementCollection
    @CollectionTable(name = "enrollment_evidence", joinColumns = @JoinColumn(name = "enrollment_id"))
    @OrderColumn(name = "evidence_order")
    private List<EnrollmentEvidence> evidences = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "enrollment_requirement", joinColumns = @JoinColumn(name = "enrollment_id"))
    private List<ProfileRequirement> requirements = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "enrollment_decision", joinColumns = @JoinColumn(name = "enrollment_id"))
    private List<EnrollmentDecision> decisions = new ArrayList<>();

    @Column(name = "rejection_reason", length = 512)
    private String rejectionReason;

    protected EnrollmentRequest() {
        // JPA
    }

    private EnrollmentRequest(UUID id,
                              String applicantId,
                              String profileId,
                              List<ProfileRequirement> requirements,
                              OffsetDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.applicantId = Objects.requireNonNull(applicantId, "applicantId");
        this.profileId = Objects.requireNonNull(profileId, "profileId");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = createdAt;
        this.status = EnrollmentStatus.DRAFT;
        this.requirements.addAll(requirements);
    }

    public static EnrollmentRequest create(String applicantId,
                                           String profileId,
                                           List<ProfileRequirement> requirements,
                                           OffsetDateTime createdAt) {
        return new EnrollmentRequest(UUID.randomUUID(), applicantId, profileId,
                Objects.requireNonNull(requirements, "requirements"), createdAt);
    }

    public void attachEvidence(EnrollmentEvidence evidence) {
        ensureNotTerminal();
        evidences.add(Objects.requireNonNull(evidence, "evidence"));
        touch();
    }

    public void submitForReview() {
        ensureState(EnrollmentStatus.DRAFT);
        validateMandatoryEvidence();
        this.status = EnrollmentStatus.UNDER_REVIEW;
        touch();
    }

    public void requestApproval() {
        if (status != EnrollmentStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only requests under review can move to approval");
        }
        this.status = EnrollmentStatus.PENDING_APPROVAL;
        touch();
    }

    public void registerApproval(EnrollmentDecision decision) {
        ensureNotTerminal();
        boolean roleAlreadyPresent = decisions.stream()
                .anyMatch(existing -> existing.role() == decision.role());
        if (roleAlreadyPresent) {
            throw new IllegalArgumentException("Role already provided decision: " + decision.role());
        }
        decisions.add(decision);
        if (status == EnrollmentStatus.UNDER_REVIEW) {
            status = EnrollmentStatus.PENDING_APPROVAL;
        }
        if (hasRequiredApprovals()) {
            status = EnrollmentStatus.READY_FOR_ISSUANCE;
        }
        touch();
    }

    public void reject(String reason, EnrollmentDecision decision) {
        ensureNotTerminal();
        this.rejectionReason = Objects.requireNonNull(reason, "reason");
        decisions.add(decision);
        this.status = EnrollmentStatus.REJECTED;
        touch();
    }

    private boolean hasRequiredApprovals() {
        Set<ApprovalRole> roles = decisions.stream()
                .map(EnrollmentDecision::role)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ApprovalRole.class)));
        return roles.contains(ApprovalRole.RA_AGENT) && roles.contains(ApprovalRole.SECURITY_OFFICER);
    }

    private void validateMandatoryEvidence() {
        Set<String> evidenceCodes = evidences.stream()
                .map(EnrollmentEvidence::getType)
                .collect(Collectors.toSet());
        boolean missing = requirements.stream()
                .filter(ProfileRequirement::isMandatory)
                .map(ProfileRequirement::getCode)
                .anyMatch(code -> !evidenceCodes.contains(code));
        if (missing) {
            throw new IllegalStateException("Missing mandatory evidence for profile " + profileId);
        }
    }

    private void ensureNotTerminal() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Enrollment already in terminal status");
        }
    }

    private void ensureState(EnrollmentStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Expected status " + expected + " but was " + status);
        }
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public List<EnrollmentEvidence> getEvidences() {
        return Collections.unmodifiableList(evidences);
    }

    public List<EnrollmentDecision> getDecisions() {
        return Collections.unmodifiableList(decisions);
    }

    public String getApplicantId() {
        return applicantId;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
