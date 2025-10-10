package br.com.cartoriodigital.enrollment.application;

import br.com.cartoriodigital.enrollment.domain.ApprovalRole;
import br.com.cartoriodigital.enrollment.domain.EnrollmentDecision;
import br.com.cartoriodigital.enrollment.domain.EnrollmentEvidence;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequest;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequestRepository;
import br.com.cartoriodigital.enrollment.domain.EnrollmentStatus;
import br.com.cartoriodigital.enrollment.domain.ProfileRequirement;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EnrollmentService {

    private final EnrollmentRequestRepository repository;

    @Inject
    public EnrollmentService(EnrollmentRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EnrollmentRequest create(CreateEnrollmentCommand command) {
        EnrollmentRequest enrollment = EnrollmentRequest.create(
                command.applicantId(),
                command.profileId(),
                command.requirements(),
                command.createdAt()
        );
        repository.save(enrollment);
        return enrollment;
    }

    @Transactional
    public EnrollmentRequest attachEvidence(UUID enrollmentId, AttachEvidenceCommand command) {
        EnrollmentRequest enrollment = loadEnrollment(enrollmentId);
        enrollment.attachEvidence(EnrollmentEvidence.of(
                command.type(),
                command.uri(),
                command.hash(),
                command.collectedAt()
        ));
        return repository.save(enrollment);
    }

    @Transactional
    public EnrollmentRequest submitForReview(UUID enrollmentId) {
        EnrollmentRequest enrollment = loadEnrollment(enrollmentId);
        enrollment.submitForReview();
        return repository.save(enrollment);
    }

    @Transactional
    public EnrollmentRequest requestApproval(UUID enrollmentId) {
        EnrollmentRequest enrollment = loadEnrollment(enrollmentId);
        enrollment.requestApproval();
        return repository.save(enrollment);
    }

    @Transactional
    public EnrollmentRequest approve(UUID enrollmentId, ApprovalCommand command) {
        EnrollmentRequest enrollment = loadEnrollment(enrollmentId);
        EnrollmentDecision decision = EnrollmentDecision.approve(
                command.actorId(),
                command.role(),
                command.signature(),
                command.decidedAt(),
                command.justification()
        );
        enrollment.registerApproval(decision);
        return repository.save(enrollment);
    }

    @Transactional
    public EnrollmentRequest reject(UUID enrollmentId, RejectionCommand command) {
        EnrollmentRequest enrollment = loadEnrollment(enrollmentId);
        EnrollmentDecision decision = EnrollmentDecision.reject(
                command.actorId(),
                command.signature(),
                command.decidedAt(),
                command.reason()
        );
        enrollment.reject(command.reason(), decision);
        return repository.save(enrollment);
    }

    public EnrollmentRequest getById(UUID id) {
        return loadEnrollment(id);
    }

    public List<EnrollmentRequest> listByApplicant(String applicantId) {
        return repository.findByApplicant(applicantId);
    }

    private EnrollmentRequest loadEnrollment(UUID enrollmentId) {
        return repository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found: " + enrollmentId));
    }

    public record CreateEnrollmentCommand(
            String applicantId,
            String profileId,
            List<ProfileRequirement> requirements,
            OffsetDateTime createdAt
    ) {
    }

    public record AttachEvidenceCommand(
            String type,
            String uri,
            String hash,
            OffsetDateTime collectedAt
    ) {
    }

    public record ApprovalCommand(
            ApprovalRole role,
            String actorId,
            String signature,
            OffsetDateTime decidedAt,
            String justification
    ) {
        public ApprovalCommand {
            if (role != ApprovalRole.RA_AGENT && role != ApprovalRole.SECURITY_OFFICER && role != ApprovalRole.SUPERVISOR) {
                throw new IllegalArgumentException("Unsupported role for approval: " + role);
            }
        }
    }

    public record RejectionCommand(
            String actorId,
            String signature,
            OffsetDateTime decidedAt,
            String reason
    ) {
    }

    public EnrollmentStatus statusOf(UUID id) {
        return getById(id).getStatus();
    }
}
