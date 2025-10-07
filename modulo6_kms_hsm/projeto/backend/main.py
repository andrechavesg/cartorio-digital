import hashlib
from typing import Dict

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, EmailStr

from crypto import IssuedCredential, KmsBackedCA
from kms import MockKMS


class CredentialRequest(BaseModel):
    email: EmailStr
    common_name: str


class SignRequest(BaseModel):
    alias: str
    payload: str


app = FastAPI(title="Cartório Digital - Integração KMS/HSM")
kms = MockKMS()
ca = KmsBackedCA("Cartorio Digital KMS CA", kms)


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "kms-hsm"}


@app.get("/ca")
def get_ca() -> Dict[str, str]:
    return {"certificate": ca.certificate_pem}


@app.post("/credentials")
def issue_credential(request: CredentialRequest) -> Dict[str, str]:
    credential = ca.issue_certificate(request.common_name, request.email)
    return _serialize_credential(credential)


@app.get("/credentials")
def list_credentials() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "credentials": {
            str(serial): _serialize_credential(credential)
            for serial, credential in ca.list_credentials().items()
        }
    }


@app.post("/kms/sign")
def sign_with_kms(request: SignRequest) -> Dict[str, str]:
    if request.alias not in kms.list_keys():
        raise HTTPException(status_code=404, detail="Alias not found")
    digest = hashlib.sha256(request.payload.encode()).digest()
    signature = kms.sign(request.alias, digest)
    return {"signature": signature}


@app.get("/kms/keys")
def list_keys() -> Dict[str, Dict[str, int]]:
    return {"keys": kms.list_keys()}


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
