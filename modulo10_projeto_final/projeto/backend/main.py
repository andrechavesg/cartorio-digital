from typing import Dict, List

from fastapi import FastAPI, HTTPException, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr

from acme import AcmeDirectory, AcmeOrder
from code_signing import CodeSigningCA, SigningCredential
from compliance import CitizenRecord, ComplianceEngine
from crypto import CertificateAuthority, IssuedCertificate
from kms import MockKMS
from kms_ca import IssuedCredential, KmsBackedCA
from observability import ObservabilityHub
from pipeline import PipelineExecution, PipelineOrchestrator
from signing import ArtifactSigner, SignedArtifact
from tls import validate_mutual_tls


class CitizenInput(BaseModel):
    name: str
    email: EmailStr
    document_type: str
    document_id: str


class ClientCertificateRequest(BaseModel):
    email: EmailStr
    common_name: str


class ServerCertificateRequest(BaseModel):
    hostname: str


class HandshakeRequest(BaseModel):
    server_serial: int
    client_serial: int


class AcmeOrderRequest(BaseModel):
    domain: str


class AcmeCompleteRequest(BaseModel):
    token: str


class CredentialRequest(BaseModel):
    email: EmailStr
    common_name: str


class SignCredentialRequest(BaseModel):
    common_name: str


class ArtifactRequest(BaseModel):
    artifact_id: str
    key_alias: str
    payload: str
    description: str | None = None


class PipelineRequest(BaseModel):
    application: str
    environment: str
    payload: str


class MetricRequest(BaseModel):
    name: str
    value: float
    labels: Dict[str, str] | None = None


class TraceRequest(BaseModel):
    span_id: str
    name: str
    duration_ms: float
    attributes: Dict[str, str] | None = None


class LogRequest(BaseModel):
    level: str
    message: str
    context: Dict[str, str] | None = None


app = FastAPI(title="Cartório Digital - Projeto Final")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.options("/{full_path:path}")
def preflight(full_path: str) -> Response:
    return Response(status_code=204)

citizens: Dict[str, CitizenRecord] = {}
compliance_engine = ComplianceEngine()
audit_log: List[Dict[str, object]] = []

primary_ca = CertificateAuthority("Cartorio Digital Root CA")
acme_directory = AcmeDirectory(primary_ca)

kms = MockKMS()
kms_ca = KmsBackedCA("Cartorio Digital KMS CA", kms)
code_sign_ca = CodeSigningCA("Cartorio Digital Code Signing", kms)
artifact_signer = ArtifactSigner(kms)
pipeline_orchestrator = PipelineOrchestrator(kms, code_sign_ca, artifact_signer)
observability = ObservabilityHub()


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "modules": "1-10"}


@app.post("/citizens")
def register_citizen(citizen: CitizenInput) -> Dict[str, str]:
    record = CitizenRecord(**citizen.model_dump())
    citizens[citizen.email] = record
    observability.record_log("INFO", "Citizen registered", email=citizen.email)
    return {"message": "Citizen registered", "email": citizen.email}


@app.get("/citizens")
def list_citizens() -> Dict[str, Dict[str, str]]:
    return {"citizens": {email: record.__dict__ for email, record in citizens.items()}}


@app.post("/certificates/client")
def issue_client_certificate(request: ClientCertificateRequest) -> Dict[str, object]:
    citizen = citizens.get(request.email)
    if not citizen:
        raise HTTPException(status_code=404, detail="Citizen not registered")
    certificate = primary_ca.issue_client_certificate(request.common_name, request.email)
    results = compliance_engine.evaluate(citizen, certificate)
    audit_entry = {
        "email": citizen.email,
        "results": [result.__dict__ for result in results],
        "certificate_serial": certificate.serial_number,
    }
    audit_log.append(audit_entry)
    if not all(result.passed for result in results):
        observability.record_log("WARN", "Compliance failure", email=citizen.email)
        raise HTTPException(status_code=400, detail=audit_entry)
    observability.record_metric("certificates_issued_total", 1, type="client")
    return {
        "certificate": _serialize_certificate(certificate),
        "compliance": audit_entry,
    }


@app.post("/certificates/server")
def issue_server_certificate(request: ServerCertificateRequest) -> Dict[str, str]:
    certificate = primary_ca.issue_server_certificate(request.hostname)
    observability.record_metric("certificates_issued_total", 1, type="server")
    return _serialize_certificate(certificate)


@app.get("/certificates")
def list_certificates() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "certificates": {
            str(serial): _serialize_certificate(cert)
            for serial, cert in primary_ca.list_all().items()
        }
    }


@app.post("/certificates/{serial_number}/revoke")
def revoke_certificate(serial_number: int) -> Dict[str, str]:
    cert = primary_ca.revoke(serial_number)
    if not cert:
        raise HTTPException(status_code=404, detail="Certificate not found")
    observability.record_log("INFO", "Certificate revoked", serial=str(serial_number))
    return _serialize_certificate(cert)


@app.post("/tls/handshake")
def simulate_handshake(request: HandshakeRequest) -> Dict[str, str]:
    if not validate_mutual_tls(primary_ca, request.server_serial, request.client_serial):
        observability.record_log("ERROR", "mTLS failure")
        raise HTTPException(status_code=400, detail="mTLS validation failed")
    observability.record_metric("mtls_handshakes_total", 1, status="success")
    return {"message": "mTLS negotiation succeeded"}


@app.post("/acme/orders")
def new_acme_order(request: AcmeOrderRequest) -> Dict[str, str]:
    order = acme_directory.new_order(request.domain, token=_generate_token(request.domain))
    observability.record_metric("acme_orders_total", 1, status="pending")
    return _serialize_order(order)


@app.post("/acme/orders/{order_id}/complete")
def complete_acme_order(order_id: str, completion: AcmeCompleteRequest) -> Dict[str, object]:
    try:
        certificate = acme_directory.complete_challenge(order_id, completion.token)
    except KeyError as exc:
        raise HTTPException(status_code=404, detail="Order not found") from exc
    except ValueError as exc:
        observability.record_log("ERROR", "ACME challenge failed", order=order_id)
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    observability.record_metric("acme_orders_total", 1, status="valid")
    return {"certificate": _serialize_certificate(certificate)}


@app.get("/acme/orders")
def list_acme_orders() -> Dict[str, Dict[str, str]]:
    return {
        "orders": {
            order_id: _serialize_order(order)
            for order_id, order in acme_directory.list_orders().items()
        }
    }


@app.post("/kms/credentials")
def issue_kms_credential(request: CredentialRequest) -> Dict[str, str]:
    credential = kms_ca.issue_certificate(request.common_name, request.email)
    observability.record_metric("kms_credentials_total", 1, email=request.email)
    return _serialize_credential(credential)


@app.get("/kms/credentials")
def list_kms_credentials() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "credentials": {
            str(serial): _serialize_credential(credential)
            for serial, credential in kms_ca.list_credentials().items()
        }
    }


@app.get("/kms/keys")
def list_keys() -> Dict[str, Dict[str, int]]:
    return {"keys": kms.list_keys()}


@app.post("/signing/credentials")
def issue_signing_credential(request: SignCredentialRequest) -> Dict[str, str]:
    credential = code_sign_ca.issue_signing_certificate(request.common_name)
    observability.record_metric("signing_credentials_total", 1)
    return _serialize_signing_credential(credential)


@app.get("/signing/credentials")
def list_signing_credentials() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "credentials": {
            str(serial): _serialize_signing_credential(credential)
            for serial, credential in code_sign_ca.list_credentials().items()
        }
    }


@app.post("/signing/artifacts")
def sign_artifact(request: ArtifactRequest) -> Dict[str, str]:
    if request.key_alias not in kms.list_keys():
        raise HTTPException(status_code=404, detail="Key alias not found")
    artifact = artifact_signer.sign(
        request.artifact_id,
        request.key_alias,
        request.payload.encode(),
        {"description": request.description or ""},
    )
    observability.record_metric("artifacts_signed_total", 1)
    return _serialize_artifact(artifact)


@app.get("/signing/artifacts")
def list_signed_artifacts() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "artifacts": {
            artifact_id: _serialize_artifact(artifact)
            for artifact_id, artifact in artifact_signer.list_signed().items()
        }
    }


@app.post("/pipelines")
def trigger_pipeline(request: PipelineRequest) -> Dict[str, object]:
    execution = pipeline_orchestrator.run_pipeline(
        request.application, request.environment, request.payload
    )
    observability.record_metric("pipelines_executed_total", 1, environment=request.environment)
    return _serialize_pipeline(execution)


@app.get("/pipelines")
def list_pipelines() -> Dict[str, Dict[str, object]]:
    return {
        "pipelines": {
            execution.pipeline_id: _serialize_pipeline(execution)
            for execution in pipeline_orchestrator.list_executions().values()
        }
    }


@app.post("/observability/metrics")
def record_metric_endpoint(request: MetricRequest) -> Dict[str, str]:
    labels = request.labels or {}
    sample = observability.record_metric(request.name, request.value, **labels)
    return {
        "name": sample.name,
        "value": sample.value,
        "labels": sample.labels,
        "recorded_at": sample.recorded_at.isoformat(),
    }


@app.post("/observability/traces")
def record_trace_endpoint(request: TraceRequest) -> Dict[str, str]:
    attributes = request.attributes or {}
    span = observability.record_trace(request.span_id, request.name, request.duration_ms, **attributes)
    return {
        "span_id": span.span_id,
        "name": span.name,
        "duration_ms": span.duration_ms,
        "attributes": span.attributes,
        "timestamp": span.timestamp.isoformat(),
    }


@app.post("/observability/logs")
def record_log_endpoint(request: LogRequest) -> Dict[str, str]:
    context = request.context or {}
    log = observability.record_log(request.level, request.message, **context)
    return {
        "level": log.level,
        "message": log.message,
        "timestamp": log.timestamp.isoformat(),
        "context": log.context,
    }


@app.get("/observability/snapshot")
def snapshot() -> Dict[str, object]:
    return observability.snapshot()


@app.get("/observability/metrics/prometheus")
def prometheus() -> str:
    return observability.render_prometheus()


@app.get("/audit")
def list_audit() -> Dict[str, List[Dict[str, object]]]:
    return {"entries": audit_log}


def _generate_token(domain: str) -> str:
    return f"token-{abs(hash(domain)) % 10_000_000}"


def _serialize_certificate(cert: IssuedCertificate) -> Dict[str, str]:
    return {
        "serial_number": str(cert.serial_number),
        "subject": cert.subject,
        "pem": cert.pem,
        "not_valid_before": cert.not_valid_before.isoformat(),
        "not_valid_after": cert.not_valid_after.isoformat(),
        "revoked": str(cert.revoked).lower(),
        "purpose": cert.purpose,
    }


def _serialize_order(order: AcmeOrder) -> Dict[str, str]:
    return {
        "order_id": order.order_id,
        "domain": order.domain,
        "token": order.token,
        "status": order.status,
        "certificate_serial": str(order.certificate_serial) if order.certificate_serial else None,
    }


def _serialize_credential(credential: IssuedCredential) -> Dict[str, str]:
    return {
        "serial_number": str(credential.serial_number),
        "subject": credential.subject,
        "certificate": credential.certificate_pem,
        "key_alias": credential.key_alias,
        "public_key": credential.public_key_pem,
        "not_valid_before": credential.not_valid_before.isoformat(),
        "not_valid_after": credential.not_valid_after.isoformat(),
    }


def _serialize_signing_credential(credential: SigningCredential) -> Dict[str, str]:
    return {
        "serial_number": str(credential.serial_number),
        "subject": credential.subject,
        "certificate": credential.certificate_pem,
        "key_alias": credential.key_alias,
        "not_valid_before": credential.not_valid_before.isoformat(),
        "not_valid_after": credential.not_valid_after.isoformat(),
    }


def _serialize_artifact(artifact: SignedArtifact) -> Dict[str, str]:
    return {
        "artifact_id": artifact.artifact_id,
        "digest": artifact.digest,
        "signature": artifact.signature,
        "key_alias": artifact.key_alias,
        "signed_at": artifact.signed_at.isoformat(),
        "metadata": artifact.metadata,
    }


def _serialize_pipeline(execution: PipelineExecution) -> Dict[str, object]:
    return {
        "pipeline_id": execution.pipeline_id,
        "application": execution.application,
        "environment": execution.environment,
        "credential": {
            "serial_number": str(execution.credential.serial_number),
            "key_alias": execution.credential.key_alias,
        },
        "artifact": {
            "id": execution.artifact.artifact_id,
            "digest": execution.artifact.digest,
            "signature": execution.artifact.signature,
        },
        "steps": [
            {
                "name": step.name,
                "status": step.status,
                "detail": step.detail,
                "completed_at": step.completed_at.isoformat(),
            }
            for step in execution.steps
        ],
    }
