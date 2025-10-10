package br.com.cartoriodigital.revocation.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevocationRequestTest {

    @Test
    void dualControlApprovalsPromoteToApproved() {
        OffsetDateTime now = OffsetDateTime.now();
        RevocationRequest request = RevocationRequest.create("00FF11", RevocationReason.KEY_COMPROMISE, "ra-agent-1", now);

        request.registerApproval(ApprovalDecision.of("ra-agent-1", ApprovalActorRole.RA_AGENT, "sig-1", now.plusMinutes(5)));
        assertEquals(RevocationStatus.AWAITING_SECURITY_APPROVAL, request.getStatus());

        request.registerApproval(ApprovalDecision.of("sec-officer-1", ApprovalActorRole.SECURITY_OFFICER, "sig-2", now.plusMinutes(6)));
        assertEquals(RevocationStatus.APPROVED, request.getStatus());

        request.markCompleted(now.plusMinutes(10));
        assertEquals(RevocationStatus.COMPLETED, request.getStatus());
    }

    @Test
    void cannotApproveTwiceWithSameRole() {
        OffsetDateTime now = OffsetDateTime.now();
        RevocationRequest request = RevocationRequest.create("00FF11", RevocationReason.KEY_COMPROMISE, "ra-agent-1", now);

        request.registerApproval(ApprovalDecision.of("ra-agent-1", ApprovalActorRole.RA_AGENT, "sig-1", now.plusMinutes(5)));
        assertThrows(IllegalArgumentException.class, () ->
                request.registerApproval(ApprovalDecision.of("ra-agent-2", ApprovalActorRole.RA_AGENT, "sig-2", now.plusMinutes(6)))
        );
    }

    @Test
    void completionRequiresApproval() {
        OffsetDateTime now = OffsetDateTime.now();
        RevocationRequest request = RevocationRequest.create("00FF11", RevocationReason.KEY_COMPROMISE, "ra-agent-1", now);
        assertThrows(IllegalStateException.class, () -> request.markCompleted(now.plusMinutes(5)));
    }
}
