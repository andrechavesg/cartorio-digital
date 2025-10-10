package br.com.cartoriodigital.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Sessão de verificação realizada pela RA.
 */
@Entity
@Table(name = "verification_session")
public class VerificationSession {

    @Id
    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 32)
    private VerificationMethod method;

    @Column(name = "evidence_uri", nullable = false, length = 512)
    private String evidenceUri;

    @Column(name = "verifier_id", nullable = false, length = 64)
    private String verifierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 32)
    private VerificationResult result;

    @Column(name = "score")
    private Double score;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    protected VerificationSession() {
        // JPA
    }

    private VerificationSession(Applicant applicant,
                                VerificationMethod method,
                                String evidenceUri,
                                String verifierId,
                                OffsetDateTime startedAt) {
        this.sessionId = UUID.randomUUID();
        this.applicant = Objects.requireNonNull(applicant, "applicant");
        this.method = Objects.requireNonNull(method, "method");
        this.evidenceUri = Objects.requireNonNull(evidenceUri, "evidenceUri");
        this.verifierId = Objects.requireNonNull(verifierId, "verifierId");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
        this.result = VerificationResult.INCONCLUSIVE;
    }

    public static VerificationSession start(Applicant applicant,
                                            VerificationMethod method,
                                            String evidenceUri,
                                            String verifierId,
                                            OffsetDateTime startedAt) {
        return new VerificationSession(applicant, method, evidenceUri, verifierId, startedAt);
    }

    public void complete(VerificationResult result, Double score, OffsetDateTime completedAt) {
        if (!this.result.equals(VerificationResult.INCONCLUSIVE)) {
            throw new IllegalStateException("Session already completed");
        }
        this.result = Objects.requireNonNull(result, "result");
        this.score = score;
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt");
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public VerificationResult getResult() {
        return result;
    }

    public VerificationMethod getMethod() {
        return method;
    }

    public Double getScore() {
        return score;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public String getEvidenceUri() {
        return evidenceUri;
    }

    public String getVerifierId() {
        return verifierId;
    }
}
