package br.com.cartoriodigital.identity.infrastructure.rest;

import br.com.cartoriodigital.identity.application.ApplicantService;
import br.com.cartoriodigital.identity.application.ApplicantService.CompleteVerificationCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.CreateApplicantCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.DecisionCommand;
import br.com.cartoriodigital.identity.application.ApplicantService.DecisionType;
import br.com.cartoriodigital.identity.application.ApplicantService.DualControlApproval;
import br.com.cartoriodigital.identity.application.ApplicantService.StartVerificationCommand;
import br.com.cartoriodigital.identity.domain.PersonType;
import br.com.cartoriodigital.identity.domain.VerificationMethod;
import br.com.cartoriodigital.identity.domain.VerificationResult;
import br.com.cartoriodigital.identity.domain.VerificationSession;
import br.com.cartoriodigital.identity.domain.Applicant;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Path("/v1/applicants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Identity Applicants", description = "Fluxos de prova de identidade e decisões conforme DOC-ICP-05")
public class ApplicantResource {

    private final ApplicantService service;

    @Inject
    public ApplicantResource(ApplicantService service) {
        this.service = service;
    }

    @POST
    @Operation(summary = "Registra um novo solicitante com coleta de consentimento e auditoria RA")
    public Response createApplicant(CreateApplicantRequest request) {
        CreateApplicantCommand command = new CreateApplicantCommand(
                PersonType.valueOf(request.personType()),
                request.documentId(),
                request.consentHashes(),
                request.timestamp(),
                request.actor().actorId(),
                request.actor().signature()
        );
        Applicant applicant = service.create(command);
        return Response.status(Response.Status.CREATED)
                .entity(ApplicantResponse.from(applicant))
                .build();
    }

    @POST
    @Path("{id}/verification")
    @Operation(summary = "Inicia sessão de verificação de identidade")
    public Response startVerification(@PathParam("id") UUID applicantId, StartVerificationRequest request) {
        StartVerificationCommand command = new StartVerificationCommand(
                VerificationMethod.valueOf(request.method()),
                request.evidenceUri(),
                request.verifier().actorId(),
                request.verifier().signature(),
                request.verifier().timestamp()
        );
        VerificationSession session = service.startVerification(applicantId, command);
        return Response.status(Response.Status.CREATED)
                .entity(VerificationSessionResponse.from(session))
                .build();
    }

    @PATCH
    @Path("{id}/verification/{sessionId}")
    @Operation(summary = "Finaliza sessão de verificação com resultado e score")
    public Response completeVerification(@PathParam("id") UUID applicantId,
                                         @PathParam("sessionId") UUID sessionId,
                                         CompleteVerificationRequest request) {
        CompleteVerificationCommand command = new CompleteVerificationCommand(
                VerificationResult.valueOf(request.result()),
                request.score(),
                request.verifier().actorId(),
                request.verifier().signature(),
                request.verifier().timestamp()
        );
        VerificationSession session = service.completeVerification(applicantId, sessionId, command);
        return Response.ok(VerificationSessionResponse.from(session)).build();
    }

    @PATCH
    @Path("{id}/decision")
    @Operation(summary = "Aplica decisão de aprovação ou rejeição com dupla custódia")
    public Response decide(@PathParam("id") UUID applicantId, DecisionRequest request) {
        DecisionCommand command = new DecisionCommand(
                DecisionType.valueOf(request.type()),
                toApproval(request.raAgent()),
                request.securityOfficer() == null ? null : toApproval(request.securityOfficer()),
                request.reason()
        );
        Applicant applicant = service.decide(applicantId, command);
        return Response.ok(ApplicantResponse.from(applicant)).build();
    }

    @GET
    @Path("{id}/evidence")
    @Operation(summary = "Lista sessões de verificação e respectivos artefatos de evidência")
    public Response listEvidence(@PathParam("id") UUID applicantId) {
        List<VerificationSessionResponse> payload = service.listEvidence(applicantId)
                .stream()
                .map(VerificationSessionResponse::from)
                .toList();
        return Response.ok(payload).build();
    }

    private static DualControlApproval toApproval(ActorSignature signature) {
        return new DualControlApproval(signature.actorId(), signature.signature(), signature.timestamp());
    }

    // DTOs
    public record CreateApplicantRequest(
            @NotBlank String personType,
            @NotBlank String documentId,
            List<@NotBlank @Size(max = 128) String> consentHashes,
            @NotNull OffsetDateTime timestamp,
            @NotNull ActorSignature actor
    ) {
    }

    public record ActorSignature(
            @NotBlank String actorId,
            @NotBlank String signature,
            @NotNull OffsetDateTime timestamp
    ) {
    }

    public record StartVerificationRequest(
            @NotBlank String method,
            @NotBlank String evidenceUri,
            @NotNull ActorSignature verifier
    ) {
    }

    public record CompleteVerificationRequest(
            @NotBlank String result,
            Double score,
            @NotNull ActorSignature verifier
    ) {
    }

    public record DecisionRequest(
            @NotBlank String type,
            @NotNull ActorSignature raAgent,
            ActorSignature securityOfficer,
            String reason
    ) {
    }

    public record ApplicantResponse(
            UUID id,
            String personType,
            String documentId,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<String> consentHashes
    ) {
        static ApplicantResponse from(Applicant applicant) {
            return new ApplicantResponse(
                    applicant.getId(),
                    applicant.getPersonType().name(),
                    applicant.getDocumentId(),
                    applicant.getStatus().name(),
                    applicant.getCreatedAt(),
                    applicant.getUpdatedAt(),
                    applicant.getConsentHashes()
            );
        }
    }

    public record VerificationSessionResponse(
            UUID sessionId,
            String method,
            String result,
            Double score,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            String evidenceUri
    ) {
        static VerificationSessionResponse from(VerificationSession session) {
            return new VerificationSessionResponse(
                    session.getSessionId(),
                    session.getMethod().name(),
                    session.getResult().name(),
                    session.getScore(),
                    session.getStartedAt(),
                    session.getCompletedAt(),
                    session.getEvidenceUri()
            );
        }
    }
}
