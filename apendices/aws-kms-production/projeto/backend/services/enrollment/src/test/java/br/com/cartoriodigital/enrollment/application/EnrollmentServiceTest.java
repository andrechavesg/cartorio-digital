package br.com.cartoriodigital.enrollment.application;

import br.com.cartoriodigital.enrollment.application.EnrollmentService.ApprovalCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.AttachEvidenceCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.CreateEnrollmentCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.RejectionCommand;
import br.com.cartoriodigital.enrollment.domain.ApprovalRole;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequest;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequestRepository;
import br.com.cartoriodigital.enrollment.domain.EnrollmentStatus;
import br.com.cartoriodigital.enrollment.domain.ProfileRequirement;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnrollmentServiceTest {

    private EnrollmentService service;
    private InMemoryEnrollmentRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryEnrollmentRepository();
        service = new EnrollmentService(repository);
    }

    @Test
    void requiresMandatoryEvidenceBeforeSubmission() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest enrollment = service.create(new CreateEnrollmentCommand(
                "applicant-1",
                "TLS-Server",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial", true)),
                now
        ));

        assertThrows(IllegalStateException.class, () -> service.submitForReview(enrollment.getId()));
    }

    @Test
    void dualApprovalPromotesReadyForIssuance() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest enrollment = service.create(new CreateEnrollmentCommand(
                "applicant-2",
                "TLS-Server",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial", true)),
                now
        ));

        service.attachEvidence(enrollment.getId(), new AttachEvidenceCommand("GOV_ID", "s3://evidence/gov", "hash", now));
        service.submitForReview(enrollment.getId());
        service.requestApproval(enrollment.getId());
        service.approve(enrollment.getId(), new ApprovalCommand(ApprovalRole.RA_AGENT, "ra-1", "sig-ra", now.plusMinutes(1), "verificado"));
        service.approve(enrollment.getId(), new ApprovalCommand(ApprovalRole.SECURITY_OFFICER, "sec-1", "sig-sec", now.plusMinutes(2), "auditoria"));

        EnrollmentRequest updated = service.getById(enrollment.getId());
        assertEquals(EnrollmentStatus.READY_FOR_ISSUANCE, updated.getStatus());
    }

    @Test
    void duplicateApprovalRoleFails() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest enrollment = bootstrapEnrollment(now);
        service.approve(enrollment.getId(), new ApprovalCommand(ApprovalRole.RA_AGENT, "ra-1", "sig-ra", now.plusMinutes(1), "ok"));
        assertThrows(IllegalArgumentException.class, () -> service.approve(enrollment.getId(), new ApprovalCommand(ApprovalRole.RA_AGENT, "ra-2", "sig", now.plusMinutes(2), "dup")));
    }

    @Test
    void rejectSetsTerminalState() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest enrollment = bootstrapEnrollment(now);
        EnrollmentRequest rejected = service.reject(enrollment.getId(), new RejectionCommand("ra-1", "sig", now.plusMinutes(5), "dados inconsistentes"));
        assertEquals(EnrollmentStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void unknownEnrollmentThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> service.getById(UUID.randomUUID()));
    }

    private EnrollmentRequest bootstrapEnrollment(OffsetDateTime now) {
        EnrollmentRequest enrollment = service.create(new CreateEnrollmentCommand(
                "applicant-3",
                "TLS-Server",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial", true)),
                now
        ));
        service.attachEvidence(enrollment.getId(), new AttachEvidenceCommand("GOV_ID", "s3://evidence/gov", "hash", now));
        service.submitForReview(enrollment.getId());
        service.requestApproval(enrollment.getId());
        return service.getById(enrollment.getId());
    }

    private static class InMemoryEnrollmentRepository implements EnrollmentRequestRepository {
        private final ConcurrentHashMap<UUID, EnrollmentRequest> storage = new ConcurrentHashMap<>();

        @Override
        public EnrollmentRequest save(EnrollmentRequest request) {
            storage.put(request.getId(), request);
            return request;
        }

        @Override
        public Optional<EnrollmentRequest> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<EnrollmentRequest> findByApplicant(String applicantId) {
            return storage.values().stream()
                    .filter(req -> req.getApplicantId().equals(applicantId))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }
}
