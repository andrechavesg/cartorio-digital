package br.com.cartoriodigital.identity.application;

import br.com.cartoriodigital.identity.domain.Applicant;
import br.com.cartoriodigital.identity.domain.ApplicantRepository;
import br.com.cartoriodigital.identity.domain.RAActorRole;
import br.com.cartoriodigital.identity.domain.RAApproval;
import br.com.cartoriodigital.identity.domain.VerificationMethod;
import br.com.cartoriodigital.identity.domain.VerificationResult;
import br.com.cartoriodigital.identity.domain.VerificationSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class ApplicantService {

    private final ApplicantRepository repository;

    @Inject
    public ApplicantService(ApplicantRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Applicant create(CreateApplicantCommand command) {
        Objects.requireNonNull(command, "command");
        repository.findByDocumentId(command.documentId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Applicant already exists for document: " + command.documentId());
                });

        Applicant applicant = Applicant.create(
                command.personType(),
                command.documentId(),
                command.createdAt(),
                command.actorId(),
                command.actorSignature()
        );
        if (command.consentHashes() != null) {
            command.consentHashes().forEach(applicant::appendConsentHash);
        }
        return repository.save(applicant);
    }

    @Transactional
    public VerificationSession startVerification(UUID applicantId, StartVerificationCommand command) {
        Applicant applicant = repository.findById(applicantId)
                .orElseThrow(() -> new NotFoundException("Applicant not found: " + applicantId));
        VerificationSession session = applicant.startVerification(
                command.method(),
                command.evidenceUri(),
                command.verifierId(),
                command.signature(),
                command.startedAt()
        );
        repository.save(applicant);
        return session;
    }

    @Transactional
    public VerificationSession completeVerification(UUID applicantId, UUID sessionId, CompleteVerificationCommand command) {
        Applicant applicant = repository.findById(applicantId)
                .orElseThrow(() -> new NotFoundException("Applicant not found: " + applicantId));
        applicant.completeVerification(
                sessionId,
                command.result(),
                command.score(),
                command.verifierId(),
                command.signature(),
                command.completedAt()
        );
        repository.save(applicant);
        return applicant.getVerificationSessions().stream()
                .filter(session -> session.getSessionId().equals(sessionId))
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public Applicant decide(UUID applicantId, DecisionCommand command) {
        Applicant applicant = repository.findById(applicantId)
                .orElseThrow(() -> new NotFoundException("Applicant not found: " + applicantId));
        if (command.decisionType() == DecisionType.APPROVE) {
            DualControlApproval raAgentApproval = command.raAgent();
            DualControlApproval securityOfficerApproval = command.securityOfficer();
            if (raAgentApproval == null) {
                throw new IllegalArgumentException("RA agent approval is required");
            }
            if (securityOfficerApproval == null) {
                throw new IllegalArgumentException("Security officer approval is required");
            }
            RAApproval raApproval = RAApproval.of(raAgentApproval.actorId(), RAActorRole.RA_AGENT, raAgentApproval.signature(), raAgentApproval.timestamp());
            RAApproval securityApproval = RAApproval.of(securityOfficerApproval.actorId(), RAActorRole.SECURITY_OFFICER, securityOfficerApproval.signature(), securityOfficerApproval.timestamp());
            applicant.approve(raApproval, securityApproval, command.reason());
        } else {
            DualControlApproval raAgentApproval = command.raAgent();
            if (raAgentApproval == null) {
                throw new IllegalArgumentException("RA agent approval is required");
            }
            RAApproval raApproval = RAApproval.of(raAgentApproval.actorId(), RAActorRole.RA_AGENT, raAgentApproval.signature(), raAgentApproval.timestamp());
            applicant.reject(raApproval, command.reason());
        }
        return repository.save(applicant);
    }

    @Transactional
    public List<VerificationSession> listEvidence(UUID applicantId) {
        Applicant applicant = repository.findById(applicantId)
                .orElseThrow(() -> new NotFoundException("Applicant not found: " + applicantId));
        return new ArrayList<>(applicant.getVerificationSessions());
    }

    public enum DecisionType {
        APPROVE,
        REJECT
    }

    public record CreateApplicantCommand(
            br.com.cartoriodigital.identity.domain.PersonType personType,
            String documentId,
            List<String> consentHashes,
            OffsetDateTime createdAt,
            String actorId,
            String actorSignature
    ) {
    }

    public record StartVerificationCommand(
            VerificationMethod method,
            String evidenceUri,
            String verifierId,
            String signature,
            OffsetDateTime startedAt
    ) {
    }

    public record CompleteVerificationCommand(
            VerificationResult result,
            Double score,
            String verifierId,
            String signature,
            OffsetDateTime completedAt
    ) {
    }

    public record DecisionCommand(
            DecisionType decisionType,
            DualControlApproval raAgent,
            DualControlApproval securityOfficer,
            String reason
    ) {
    }

    public record DualControlApproval(
            String actorId,
            String signature,
            OffsetDateTime timestamp
    ) {
    }
}
