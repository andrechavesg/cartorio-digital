from typing import Dict

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from crypto import CodeSigningCA, SigningCredential
from kms import MockKMS
from signing import ArtifactSigner, SignedArtifact


class CredentialRequest(BaseModel):
    common_name: str


class ArtifactRequest(BaseModel):
    artifact_id: str
    key_alias: str
    payload: str
    description: str | None = None


app = FastAPI(title="Cartório Digital - Assinatura de Artefatos")
kms = MockKMS()
ca = CodeSigningCA("Cartorio Digital Code Signing", kms)
signer = ArtifactSigner(kms)


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "assinatura"}


@app.post("/signing/credentials")
def issue_signing_credential(request: CredentialRequest) -> Dict[str, str]:
    credential = ca.issue_signing_certificate(request.common_name)
    return _serialize_credential(credential)


@app.get("/signing/credentials")
def list_credentials() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "credentials": {
            str(serial): _serialize_credential(credential)
            for serial, credential in ca.list_credentials().items()
        }
    }


@app.post("/signing/artifacts")
def sign_artifact(request: ArtifactRequest) -> Dict[str, str]:
    if request.key_alias not in kms.list_keys():
        raise HTTPException(status_code=404, detail="Key alias not found")
    metadata = {"description": request.description or ""}
    artifact = signer.sign(
        request.artifact_id,
        request.key_alias,
        request.payload.encode(),
        metadata,
    )
    return _serialize_artifact(artifact)


@app.get("/signing/artifacts")
def list_signed_artifacts() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "artifacts": {
            artifact_id: _serialize_artifact(artifact)
            for artifact_id, artifact in signer.list_signed().items()
        }
    }


@app.get("/kms/keys")
def list_keys() -> Dict[str, Dict[str, int]]:
    return {"keys": kms.list_keys()}


def _serialize_credential(credential: SigningCredential) -> Dict[str, str]:
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
