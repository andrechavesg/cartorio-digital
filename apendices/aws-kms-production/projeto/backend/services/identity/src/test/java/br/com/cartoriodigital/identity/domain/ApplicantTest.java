package br.com.cartoriodigital.identity.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicantTest {

    @Test
    void approveRequiresDualControl() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = Applicant.create(PersonType.NATURAL_PERSON, "12345678900", now, "agent-1", "sig-agent");

        applicant.startVerification(VerificationMethod.IN_PERSON, "s3://evidence/1", "agent-1", "sig-start", now);
        applicant.completeVerification(
                applicant.getVerificationSessions().getFirst().getSessionId(),
                VerificationResult.APPROVED,
                0.98,
                "agent-1",
                "sig-complete",
                now.plusMinutes(5)
        );

        RAApproval agentApproval = RAApproval.of("agent-1", RAActorRole.RA_AGENT, "sig-agent-approval", now.plusMinutes(6));
        RAApproval securityApproval = RAApproval.of("security-1", RAActorRole.SECURITY_OFFICER, "sig-security", now.plusMinutes(6));

        applicant.approve(agentApproval, securityApproval, "Documento válido");

        assertEquals(ApplicantStatus.APPROVED, applicant.getStatus());
        // create + start + complete + approvals (2 entries)
        assertEquals(4, applicant.getActionLogs().size());
    }

    @Test
    void rejectFromTerminalStateIsNotAllowed() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = Applicant.create(PersonType.NATURAL_PERSON, "12345678900", now, "agent-1", "sig-agent");
        RAApproval agentApproval = RAApproval.of("agent-1", RAActorRole.RA_AGENT, "sig-agent-approval", now.plusMinutes(2));
        RAApproval securityApproval = RAApproval.of("security-1", RAActorRole.SECURITY_OFFICER, "sig-security", now.plusMinutes(2));
        applicant.approve(agentApproval, securityApproval, "Tudo certo");

        assertThrows(IllegalStateException.class, () -> applicant.reject(agentApproval, "Tentativa de rejeição indevida"));
    }

    @Test
    void approvingWithWrongRoleFails() {
        OffsetDateTime now = OffsetDateTime.now();
        Applicant applicant = Applicant.create(PersonType.NATURAL_PERSON, "12345678900", now, "agent-1", "sig-agent");
        RAApproval wrongApproval = RAApproval.of("auditor-1", RAActorRole.AUDITOR, "sig-aud", now.plusMinutes(5));
        RAApproval securityApproval = RAApproval.of("security-1", RAActorRole.SECURITY_OFFICER, "sig-security", now.plusMinutes(5));

        assertThrows(IllegalArgumentException.class, () -> applicant.approve(wrongApproval, securityApproval, "invalid"));
    }
}
