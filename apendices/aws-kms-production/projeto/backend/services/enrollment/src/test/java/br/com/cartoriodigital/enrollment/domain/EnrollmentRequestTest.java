package br.com.cartoriodigital.enrollment.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnrollmentRequestTest {

    @Test
    void requiresMandatoryEvidenceBeforeReview() {
        EnrollmentRequest request = EnrollmentRequest.create(
                "applicant-1",
                "TLS-Server-Profile",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial com foto", true)),
                OffsetDateTime.now()
        );

        assertThrows(IllegalStateException.class, request::submitForReview);
    }

    @Test
    void dualApprovalPromotesToReadyForIssuance() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest request = EnrollmentRequest.create(
                "applicant-1",
                "TLS-Server-Profile",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial com foto", true)),
                now
        );

        request.attachEvidence(EnrollmentEvidence.of("GOV_ID", "s3://evidence/gov", "hash-1", now));
        request.submitForReview();
        request.requestApproval();

        request.registerApproval(EnrollmentDecision.approve("ra-agent-1", ApprovalRole.RA_AGENT, "sig-1", now.plusMinutes(5), "Doc verificado"));
        request.registerApproval(EnrollmentDecision.approve("sec-officer-1", ApprovalRole.SECURITY_OFFICER, "sig-2", now.plusMinutes(6), "Fluxo dual-control"));

        assertEquals(EnrollmentStatus.READY_FOR_ISSUANCE, request.getStatus());
    }

    @Test
    void sameRoleCannotApproveTwice() {
        OffsetDateTime now = OffsetDateTime.now();
        EnrollmentRequest request = EnrollmentRequest.create(
                "applicant-1",
                "TLS-Server-Profile",
                List.of(ProfileRequirement.of("GOV_ID", "Documento oficial com foto", true)),
                now
        );

        request.attachEvidence(EnrollmentEvidence.of("GOV_ID", "s3://evidence/gov", "hash-1", now));
        request.submitForReview();
        request.requestApproval();

        request.registerApproval(EnrollmentDecision.approve("ra-agent-1", ApprovalRole.RA_AGENT, "sig-1", now.plusMinutes(5), "Primeira decisão"));

        assertThrows(IllegalArgumentException.class, () ->
                request.registerApproval(EnrollmentDecision.approve("ra-agent-2", ApprovalRole.RA_AGENT, "sig-2", now.plusMinutes(6), "Duplicado"))
        );
    }
}
