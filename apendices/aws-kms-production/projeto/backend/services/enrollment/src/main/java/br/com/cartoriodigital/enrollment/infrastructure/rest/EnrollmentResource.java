package br.com.cartoriodigital.enrollment.infrastructure.rest;

import br.com.cartoriodigital.enrollment.application.EnrollmentService;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.ApprovalCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.AttachEvidenceCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.CreateEnrollmentCommand;
import br.com.cartoriodigital.enrollment.application.EnrollmentService.RejectionCommand;
import br.com.cartoriodigital.enrollment.domain.ApprovalRole;
import br.com.cartoriodigital.enrollment.domain.EnrollmentDecision;
import br.com.cartoriodigital.enrollment.domain.EnrollmentEvidence;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequest;
import br.com.cartoriodigital.enrollment.domain.ProfileRequirement;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/v1/enrollments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Enrollment Service", description = "Orquestra matrícula, evidências e aprovações dual-control")
public class EnrollmentResource {

    private final EnrollmentService service;

    @Inject
    public EnrollmentResource(EnrollmentService service) {
        this.service = service;
    }

    @POST
    @Operation(summary = "Cria nova solicitação de matrícula atrelada a um perfil DOC-ICP-08")
    public Response create(@Valid CreateEnrollmentRequest request) {
        CreateEnrollmentCommand command = new CreateEnrollmentCommand(
                request.applicantId(),
                request.profileId(),
                request.requirements().stream()
                        .map(it -> ProfileRequirement.of(it.code(), it.description(), it.mandatory()))
                        .collect(Collectors.toList()),
                request.createdAt()
        );
        EnrollmentRequest enrollment = service.create(command);
        return Response.status(Response.Status.CREATED)
                .entity(EnrollmentResponse.from(enrollment))
                .build();
    }

    @POST
    @Path("{id}/evidences")
    @Operation(summary = "Anexa evidência criptografada ao processo de matrícula")
    public Response attachEvidence(@PathParam("id") UUID id, @Valid AttachEvidenceRequest request) {
        EnrollmentRequest enrollment = service.attachEvidence(id, new AttachEvidenceCommand(
                request.type(),
                request.uri(),
                request.hash(),
                request.collectedAt()
        ));
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @PATCH
    @Path("{id}/submit")
    @Operation(summary = "Submete matrícula para revisão da equipe RA")
    public Response submit(@PathParam("id") UUID id) {
        EnrollmentRequest enrollment = service.submitForReview(id);
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @PATCH
    @Path("{id}/request-approval")
    @Operation(summary = "Encaminha matrícula para aprovação dual-control")
    public Response requestApproval(@PathParam("id") UUID id) {
        EnrollmentRequest enrollment = service.requestApproval(id);
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @PATCH
    @Path("{id}/approve")
    @Operation(summary = "Registra aprovação de matrícula (RA Agent/Security Officer)")
    public Response approve(@PathParam("id") UUID id, @Valid ApprovalRequest request) {
        ApprovalCommand command = new ApprovalCommand(
                ApprovalRole.valueOf(request.role()),
                request.actorId(),
                request.signature(),
                request.decidedAt(),
                request.justification()
        );
        EnrollmentRequest enrollment = service.approve(id, command);
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @PATCH
    @Path("{id}/reject")
    @Operation(summary = "Rejeita matrícula com justificativa assinada pelo RA Agent")
    public Response reject(@PathParam("id") UUID id, @Valid RejectionRequest request) {
        RejectionCommand command = new RejectionCommand(
                request.actorId(),
                request.signature(),
                request.decidedAt(),
                request.reason()
        );
        EnrollmentRequest enrollment = service.reject(id, command);
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Recupera detalhes completos da matrícula")
    public Response getById(@PathParam("id") UUID id) {
        EnrollmentRequest enrollment = service.getById(id);
        return Response.ok(EnrollmentResponse.from(enrollment)).build();
    }

    @GET
    @Operation(summary = "Lista matrículas por solicitante")
    public Response listByApplicant(@QueryParam("applicantId") String applicantId) {
        if (applicantId == null || applicantId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorPayload.of("INVALID_REQUEST", "applicantId is required"))
                    .build();
        }
        List<EnrollmentResponse> payload = service.listByApplicant(applicantId)
                .stream()
                .map(EnrollmentResponse::from)
                .toList();
        return Response.ok(payload).build();
    }

    // DTOs

    public record CreateEnrollmentRequest(
            @NotBlank String applicantId,
            @NotBlank String profileId,
            @NotNull List<@Valid RequirementPayload> requirements,
            @NotNull OffsetDateTime createdAt
    ) {
    }

    public record RequirementPayload(
            @NotBlank String code,
            @NotBlank String description,
            boolean mandatory
    ) {
    }

    public record AttachEvidenceRequest(
            @NotBlank String type,
            @NotBlank String uri,
            @NotBlank String hash,
            @NotNull OffsetDateTime collectedAt
    ) {
    }

    public record ApprovalRequest(
            @NotBlank String role,
            @NotBlank String actorId,
            @NotBlank @Size(max = 512) String signature,
            @NotNull OffsetDateTime decidedAt,
            @Size(max = 512) String justification
    ) {
    }

    public record RejectionRequest(
            @NotBlank String actorId,
            @NotBlank String signature,
            @NotNull OffsetDateTime decidedAt,
            @NotBlank String reason
    ) {
    }

    public record EnrollmentResponse(
            UUID id,
            String applicantId,
            String profileId,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            List<EvidenceResponse> evidences,
            List<DecisionResponse> decisions,
            String rejectionReason
    ) {
        static EnrollmentResponse from(EnrollmentRequest request) {
            List<EvidenceResponse> evidences = request.getEvidences().stream().map(EvidenceResponse::from).toList();
            List<DecisionResponse> decisions = request.getDecisions().stream().map(DecisionResponse::from).toList();
            return new EnrollmentResponse(
                    request.getId(),
                    request.getApplicantId(),
                    request.getProfileId(),
                    request.getStatus().name(),
                    request.getCreatedAt(),
                    request.getUpdatedAt(),
                    evidences,
                    decisions,
                    request.getRejectionReason()
            );
        }
    }

    public record EvidenceResponse(
            String type,
            String uri,
            String hash,
            OffsetDateTime collectedAt
    ) {
        static EvidenceResponse from(EnrollmentEvidence evidence) {
            return new EvidenceResponse(
                    evidence.getType(),
                    evidence.getUri(),
                    evidence.getHash(),
                    evidence.getCollectedAt()
            );
        }
    }

    public record DecisionResponse(
            String role,
            String actorId,
            String justification,
            OffsetDateTime decidedAt
    ) {
        static DecisionResponse from(EnrollmentDecision decision) {
            return new DecisionResponse(
                    decision.role().name(),
                    decision.actorId(),
                    decision.justification(),
                    decision.decidedAt()
            );
        }
    }

    public record ErrorPayload(String code, String message) {
        static ErrorPayload of(String code, String message) {
            return new ErrorPayload(code, message);
        }
    }
}
