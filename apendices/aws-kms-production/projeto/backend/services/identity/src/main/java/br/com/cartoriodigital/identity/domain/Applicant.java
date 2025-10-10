package br.com.cartoriodigital.identity.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
 * Agregado principal do serviço de prova de identidade.
 */
@Entity
@Table(name = "applicant")
public class Applicant {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 32)
    private PersonType personType;

    @Column(name = "document_id", nullable = false, length = 64, unique = true)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ApplicantStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startedAt DESC")
    private List<VerificationSession> verificationSessions = new ArrayList<>();

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<RAActionLog> actionLogs = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "applicant_consent", joinColumns = @JoinColumn(name = "applicant_id"))
    @Column(name = "consent_hash", length = 128)
    private List<String> consentHashes = new ArrayList<>();

    protected Applicant() {
        // JPA
    }

    private Applicant(UUID id, PersonType personType, String documentId, OffsetDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.personType = Objects.requireNonNull(personType, "personType");
        this.documentId = Objects.requireNonNull(documentId, "documentId");
        this.status = ApplicantStatus.PENDING_VERIFICATION;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = createdAt;
    }

    public static Applicant create(PersonType personType, String documentId, OffsetDateTime createdAt, String actorId, String signature) {
        Applicant applicant = new Applicant(UUID.randomUUID(), personType, documentId, createdAt);
        applicant.registerAction(actorId, RAActorRole.RA_AGENT, RAActionType.REGISTRATION_CREATED, null, signature, createdAt);
        return applicant;
    }

    public VerificationSession startVerification(VerificationMethod method,
                                                 String evidenceUri,
                                                 String verifierId,
                                                 String signature,
                                                 OffsetDateTime startedAt) {
        ensureNotTerminal();
        VerificationSession session = VerificationSession.start(this, method, evidenceUri, verifierId, startedAt);
        verificationSessions.add(session);
        registerAction(verifierId, RAActorRole.RA_AGENT, RAActionType.VERIFICATION_STARTED, null, signature, startedAt);
        touch(startedAt);
        return session;
    }

    public void completeVerification(UUID sessionId,
                                     VerificationResult result,
                                     Double score,
                                     String verifierId,
                                     String signature,
                                     OffsetDateTime completedAt) {
        VerificationSession session = verificationSessions.stream()
                .filter(it -> it.getSessionId().equals(sessionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Verification session not found: " + sessionId));
        session.complete(result, score, completedAt);
        registerAction(verifierId, RAActorRole.RA_AGENT, RAActionType.VERIFICATION_COMPLETED, result.name(), signature, completedAt);
        touch(completedAt);
    }

    public void approve(RAApproval raAgentApproval, RAApproval securityApproval, String reason) {
        ensureNotTerminal();
        requireRole(raAgentApproval, RAActorRole.RA_AGENT);
        requireRole(securityApproval, RAActorRole.SECURITY_OFFICER);
        this.status = ApplicantStatus.APPROVED;
        OffsetDateTime now = OffsetDateTime.now();
        registerAction(raAgentApproval.actorId(), RAActorRole.RA_AGENT, RAActionType.APPROVAL_GRANTED, reason, raAgentApproval.signature(), raAgentApproval.approvedAt());
        registerAction(securityApproval.actorId(), RAActorRole.SECURITY_OFFICER, RAActionType.APPROVAL_GRANTED, reason, securityApproval.signature(), securityApproval.approvedAt());
        touch(now);
    }

    public void reject(RAApproval raAgentApproval, String rejectionReason) {
        ensureNotTerminal();
        requireRole(raAgentApproval, RAActorRole.RA_AGENT);
        this.status = ApplicantStatus.REJECTED;
        OffsetDateTime now = OffsetDateTime.now();
        registerAction(raAgentApproval.actorId(), RAActorRole.RA_AGENT, RAActionType.APPROVAL_REJECTED, rejectionReason, raAgentApproval.signature(), raAgentApproval.approvedAt());
        touch(now);
    }

    public void appendConsentHash(String consentHash) {
        this.consentHashes.add(Objects.requireNonNull(consentHash, "consentHash"));
    }

    private void ensureNotTerminal() {
        if (status.isTerminal()) {
            throw new IllegalStateException("Applicant already in terminal status: " + status);
        }
    }

    private void requireRole(RAApproval approval, RAActorRole expectedRole) {
        if (approval.role() != expectedRole) {
            throw new IllegalArgumentException("Expected approval from role " + expectedRole + " but received " + approval.role());
        }
    }

    private void registerAction(String actorId,
                                RAActorRole role,
                                RAActionType actionType,
                                String reason,
                                String signature,
                                OffsetDateTime timestamp) {
        RAActionLog logEntry = RAActionLog.create(this, actorId, role, actionType, reason, signature, timestamp);
        actionLogs.add(logEntry);
    }

    private void touch(OffsetDateTime at) {
        this.updatedAt = Objects.requireNonNullElseGet(at, OffsetDateTime::now);
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

    public ApplicantStatus getStatus() {
        return status;
    }

    public List<VerificationSession> getVerificationSessions() {
        return Collections.unmodifiableList(verificationSessions);
    }

    public List<RAActionLog> getActionLogs() {
        return Collections.unmodifiableList(actionLogs);
    }

    public List<String> getConsentHashes() {
        return Collections.unmodifiableList(consentHashes);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public String getDocumentId() {
        return documentId;
    }
}
