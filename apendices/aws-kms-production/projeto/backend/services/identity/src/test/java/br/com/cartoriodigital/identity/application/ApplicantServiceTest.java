package br.com.cartoriodigital.identity.application;

import br.com.cartoriodigital.identity.application.ApplicantService.CompleteVerificationCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.CreateApplicantCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.DecisionCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.DecisionType;
import br.com.cartoriodigital.identity.application.ApplicantService.DualControlApproval;
import br.com.cartoriodigital.identity.application.ApplicantService.StartVerificationCommand;
import br.com.cartoriodigital.identity.domain.Applicant;
import br.com.cartoriodigital.identity.domain.ApplicantRepository;
import br.com.cartoriodigital.identity.domain.PersonType;
import br.com.cartoriodigital.identity.domain.VerificationMethod;
import br.com.cartoriodigital.identity.domain.VerificationResult;
import br.com.cartoriodigital.identity.domain.VerificationSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicantServiceTest {

    private ApplicantService service;
    private InMemoryApplicantRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryApplicantRepository();
        service = new ApplicantService(repository);
    }

    @Test
    void createAndApproveApplicant() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = service.create(new CreateApplicantCommand(
                PersonType.NATURAL_PERSON,
                "12345678900",
                List.of("hash-consent"),
                now,
                "agent-1",
                "sig-agent"
        ));

        assertNotNull(applicant.getId());
        assertEquals("12345678900", applicant.getDocumentId());

        VerificationSession session = service.startVerification(applicant.getId(), new StartVerificationCommand(
                VerificationMethod.IN_PERSON,
                "s3://evidence/1",
                "agent-1",
                "sig-start",
                now.plusMinutes(1)
        ));

        assertEquals(VerificationResult.INCONCLUSIVE, session.getResult());

        VerificationSession completed = service.completeVerification(applicant.getId(), session.getSessionId(), new CompleteVerificationCommand(
                VerificationResult.APPROVED,
                0.97,
                "agent-1",
                "sig-complete",
                now.plusMinutes(5)
        ));
        assertEquals(VerificationResult.APPROVED, completed.getResult());

        Applicant decided = service.decide(applicant.getId(), new DecisionCommand(
                DecisionType.APPROVE,
                new DualControlApproval("agent-1", "sig-approve", now.plusMinutes(6)),
                new DualControlApproval("security-1", "sig-security", now.plusMinutes(6)),
                "Documento válido"
        ));

        assertEquals("APPROVED", decided.getStatus().name());
    }

    @Test
    void rejectWithoutSecurityOfficer() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = service.create(new CreateApplicantCommand(
                PersonType.NATURAL_PERSON,
                "99999999999",
                Collections.emptyList(),
                now,
                "agent-1",
                "sig-agent"
        ));

        Applicant decided = service.decide(applicant.getId(), new DecisionCommand(
                DecisionType.REJECT,
                new DualControlApproval("agent-1", "sig-reject", now.plusMinutes(1)),
                null,
                "Dados inconsistentes"
        ));

        assertEquals("REJECTED", decided.getStatus().name());
    }

    @Test
    void approvingWithoutSecurityOfficerFails() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = service.create(new CreateApplicantCommand(
                PersonType.NATURAL_PERSON,
                "55555555555",
                Collections.emptyList(),
                now,
                "agent-1",
                "sig-agent"
        ));

        assertThrows(IllegalArgumentException.class, () -> service.decide(applicant.getId(), new DecisionCommand(
                DecisionType.APPROVE,
                new DualControlApproval("agent-1", "sig", now.plusMinutes(1)),
                null,
                "Sem segurança"
        )));
    }

    private static class InMemoryApplicantRepository implements ApplicantRepository {
        private final ConcurrentHashMap<UUID, Applicant> storage = new ConcurrentHashMap<>();

        @Override
        public Applicant save(Applicant applicant) {
            storage.put(applicant.getId(), applicant);
            return applicant;
        }

        @Override
        public Optional<Applicant> findById(UUID id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Applicant> findByDocumentId(String documentId) {
            return storage.values().stream()
                    .filter(applicant -> applicant.getDocumentId().equals(documentId))
                    .findFirst();
        }
    }
}
