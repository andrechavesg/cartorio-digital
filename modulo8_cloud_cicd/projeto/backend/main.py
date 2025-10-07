from typing import Dict

from fastapi import FastAPI
from pydantic import BaseModel

from crypto import CodeSigningCA
from kms import MockKMS
from pipeline import PipelineOrchestrator
from signing import ArtifactSigner


class PipelineRequest(BaseModel):
    application: str
    environment: str
    payload: str


app = FastAPI(title="Cartório Digital - Cloud e CI/CD")
kms = MockKMS()
ca = CodeSigningCA("Cartorio Digital CI/CD", kms)
signer = ArtifactSigner(kms)
orchestrator = PipelineOrchestrator(kms, ca, signer)


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "cloud-cicd"}


@app.post("/pipelines")
def trigger_pipeline(request: PipelineRequest) -> Dict[str, object]:
    execution = orchestrator.run_pipeline(
        application=request.application,
        environment=request.environment,
        payload=request.payload,
    )
    return _serialize_execution(execution)


@app.get("/pipelines")
def list_pipelines() -> Dict[str, Dict[str, object]]:
    return {
        "pipelines": {
            pipeline_id: _serialize_execution(execution)
            for pipeline_id, execution in orchestrator.list_executions().items()
        }
    }


@app.get("/kms/keys")
def list_keys() -> Dict[str, Dict[str, int]]:
    return {"keys": kms.list_keys()}


@app.get("/signing/credentials")
def list_credentials() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "credentials": {
            str(serial): _serialize_credential(credential)
            for serial, credential in ca.list_credentials().items()
        }
    }


@app.get("/signing/artifacts")
def list_artifacts() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "artifacts": {
            artifact_id: _serialize_artifact(artifact)
            for artifact_id, artifact in signer.list_signed().items()
        }
    }


def _serialize_execution(execution) -> Dict[str, object]:
    return {
        "pipeline_id": execution.pipeline_id,
        "application": execution.application,
        "environment": execution.environment,
        "credential": {
            "serial_number": str(execution.credential.serial_number),
            "certificate": execution.credential.certificate_pem,
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


def _serialize_credential(credential) -> Dict[str, str]:
    return {
        "serial_number": str(credential.serial_number),
        "subject": credential.subject,
        "certificate": credential.certificate_pem,
        "key_alias": credential.key_alias,
        "not_valid_before": credential.not_valid_before.isoformat(),
        "not_valid_after": credential.not_valid_after.isoformat(),
    }


def _serialize_artifact(artifact) -> Dict[str, str]:
    return {
        "artifact_id": artifact.artifact_id,
        "digest": artifact.digest,
        "signature": artifact.signature,
        "key_alias": artifact.key_alias,
        "signed_at": artifact.signed_at.isoformat(),
        "metadata": artifact.metadata,
    }
from typing import Dict

from fastapi import FastAPI
from pydantic import BaseModel

from crypto import CodeSigningCA
from kms import MockKMS
from pipeline import PipelineOrchestrator
from signing import ArtifactSigner

