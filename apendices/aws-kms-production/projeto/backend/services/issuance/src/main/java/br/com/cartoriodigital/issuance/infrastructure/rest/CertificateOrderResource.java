package br.com.cartoriodigital.issuance.infrastructure.rest;

import br.com.cartoriodigital.issuance.application.CertificateOrderService;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.CompleteOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.CreateOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.FailureCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.QueueOrderCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.RevokeCommand;
import br.com.cartoriodigital.issuance.application.CertificateOrderService.TransitionCommand;
import br.com.cartoriodigital.issuance.domain.CertificateOrder;
import br.com.cartoriodigital.issuance.domain.CertificateOrderStatus;
import br.com.cartoriodigital.issuance.domain.IssuanceAuditEntry;
import br.com.cartoriodigital.issuance.domain.IssuedCertificate;
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

@Path("/v1/certificate-orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Certificate Issuance", description = "Orquestra pedidos de emissão com integração a HSM/KMS")
public class CertificateOrderResource {

    private final CertificateOrderService service;

    @Inject
    public CertificateOrderResource(CertificateOrderService service) {
        this.service = service;
    }

    @POST
    @Operation(summary = "Cria pedido de emissão de certificado")
    public Response create(@Valid CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.enrollmentId(),
                request.profileId(),
                request.csrPem(),
                request.caAlias(),
                request.requestedAt()
        );
        CertificateOrder order = service.create(command);
        return Response.status(Response.Status.CREATED)
                .entity(CertificateOrderResponse.from(order))
                .build();
    }

    @PATCH
    @Path("{id}/queue")
    @Operation(summary = "Enfileira pedido para processamento pelo orquestrador de assinaturas")
    public Response queue(@PathParam("id") UUID id, @Valid OperationalRequest request) {
        CertificateOrder order = service.queue(id, new QueueOrderCommand(
                request.actorId(),
                request.at()
        ));
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @PATCH
    @Path("{id}/signing")
    @Operation(summary = "Atualiza pedido para estado de assinatura (acesso ao HSM)")
    public Response markSigning(@PathParam("id") UUID id, @Valid OperationalRequest request) {
        CertificateOrder order = service.markSigning(id, new TransitionCommand(
                request.actorId(),
                request.at()
        ));
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @PATCH
    @Path("{id}/complete")
    @Operation(summary = "Completa emissão e armazena certificados emitidos")
    public Response complete(@PathParam("id") UUID id, @Valid CompleteOrderRequest request) {
        CertificateOrder order = service.complete(id, new CompleteOrderCommand(
                request.actorId(),
                request.certificatePem(),
                request.certificateChainPem(),
                request.pkcs12Uri(),
                request.issuedAt()
        ));
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @PATCH
    @Path("{id}/fail")
    @Operation(summary = "Registra falha de emissão")
    public Response fail(@PathParam("id") UUID id, @Valid FailureRequest request) {
        CertificateOrder order = service.fail(id, new FailureCommand(
                request.actorId(),
                request.reason(),
                request.at()
        ));
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @PATCH
    @Path("{id}/revoke")
    @Operation(summary = "Revoga certificado emitido")
    public Response revoke(@PathParam("id") UUID id, @Valid RevokeRequest request) {
        CertificateOrder order = service.revoke(id, new RevokeCommand(
                request.actorId(),
                request.reason(),
                request.revokedAt()
        ));
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Consulta pedido de emissão")
    public Response getById(@PathParam("id") UUID id) {
        CertificateOrder order = service.getById(id);
        return Response.ok(CertificateOrderResponse.from(order)).build();
    }

    @GET
    @Operation(summary = "Lista pedidos de emissão por matrícula/enrollment")
    public Response listByEnrollment(@QueryParam("enrollmentId") String enrollmentId) {
        if (enrollmentId == null || enrollmentId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorPayload.of("INVALID_REQUEST", "enrollmentId is required"))
                    .build();
        }
        List<CertificateOrderResponse> payload = service.listByEnrollment(enrollmentId)
                .stream()
                .map(CertificateOrderResponse::from)
                .toList();
        return Response.ok(payload).build();
    }

    // DTOs
    public record CreateOrderRequest(
            @NotBlank String enrollmentId,
            @NotBlank String profileId,
            @NotBlank String csrPem,
            @NotBlank String caAlias,
            @NotNull OffsetDateTime requestedAt
    ) {
    }

    public record OperationalRequest(
            @NotBlank String actorId,
            @NotNull OffsetDateTime at
    ) {
    }

    public record CompleteOrderRequest(
            @NotBlank String actorId,
            @NotBlank String certificatePem,
            String certificateChainPem,
            String pkcs12Uri,
            @NotNull OffsetDateTime issuedAt
    ) {
    }

    public record FailureRequest(
            @NotBlank String actorId,
            @NotBlank @Size(max = 512) String reason,
            @NotNull OffsetDateTime at
    ) {
    }

    public record RevokeRequest(
            @NotBlank String actorId,
            @NotBlank @Size(max = 512) String reason,
            @NotNull OffsetDateTime revokedAt
    ) {
    }

    public record CertificateOrderResponse(
            UUID orderId,
            String enrollmentId,
            String profileId,
            CertificateOrderStatus status,
            OffsetDateTime requestedAt,
            OffsetDateTime updatedAt,
            IssuedCertificateResponse issuedCertificate,
            String failureReason,
            OffsetDateTime revokedAt,
            List<AuditEntryResponse> auditTrail
    ) {
        static CertificateOrderResponse from(CertificateOrder order) {
            IssuedCertificate issued = order.getIssuedCertificate();
            IssuedCertificateResponse issuedResponse = issued == null ? null : IssuedCertificateResponse.from(issued);
            return new CertificateOrderResponse(
                    order.getOrderId(),
                    order.getEnrollmentId(),
                    order.getProfileId(),
                    order.getStatus(),
                    order.getRequestedAt(),
                    order.getUpdatedAt(),
                    issuedResponse,
                    order.getFailureReason(),
                    order.getRevokedAt(),
                    order.getAuditTrail().stream().map(AuditEntryResponse::from).toList()
            );
        }
    }

    public record IssuedCertificateResponse(
            String certificatePem,
            String certificateChainPem,
            String pkcs12Uri,
            OffsetDateTime issuedAt
    ) {
        static IssuedCertificateResponse from(IssuedCertificate certificate) {
            return new IssuedCertificateResponse(
                    certificate.getCertificatePem(),
                    certificate.getCertificateChainPem(),
                    certificate.getPkcs12ObjectUri(),
                    certificate.getIssuedAt()
            );
        }
    }

    public record AuditEntryResponse(
            String eventType,
            String actorId,
            String details,
            OffsetDateTime occurredAt
    ) {
        static AuditEntryResponse from(IssuanceAuditEntry entry) {
            return new AuditEntryResponse(
                    entry.getEventType().name(),
                    entry.getActorId(),
                    entry.getDetails(),
                    entry.getOccurredAt()
            );
        }
    }

    public record ErrorPayload(String code, String message) {
        static ErrorPayload of(String code, String message) {
            return new ErrorPayload(code, message);
        }
    }
}
