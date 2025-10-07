from typing import Dict

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, EmailStr

from crypto import CertificateAuthority, IssuedCertificate


class Citizen(BaseModel):
    name: str
    email: EmailStr


class CertificateRequest(BaseModel):
    email: EmailStr
    common_name: str


app = FastAPI(title="Cartório Digital - PKI e Certificados")
ca = CertificateAuthority("Cartorio Digital CA")
registrations: Dict[str, Citizen] = {}


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "pki"}


@app.post("/citizens")
def enroll_citizen(citizen: Citizen) -> Dict[str, str]:
    registrations[citizen.email] = citizen
    return {"message": "Citizen registered", "email": citizen.email}


@app.get("/citizens")
def list_citizens() -> Dict[str, Dict[str, str]]:
    return {
        "citizens": {
            email: citizen.model_dump()
            for email, citizen in registrations.items()
        }
    }


@app.get("/ca")
def ca_certificate() -> Dict[str, str]:
    return {"certificate": ca.certificate_pem}


@app.post("/certificates")
def issue_certificate(request: CertificateRequest) -> Dict[str, str]:
    if request.email not in registrations:
        raise HTTPException(status_code=404, detail="Citizen not registered")
    issued = ca.issue_certificate(request.common_name, request.email)
    return _serialize_certificate(issued)


@app.get("/certificates")
def list_certificates() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "certificates": {
            str(serial): _serialize_certificate(cert)
            for serial, cert in ca.list_all().items()
        }
    }


@app.get("/certificates/{serial_number}")
def get_certificate(serial_number: int) -> Dict[str, str]:
    cert = ca.get(serial_number)
    if not cert:
        raise HTTPException(status_code=404, detail="Certificate not found")
    return _serialize_certificate(cert)


@app.post("/certificates/{serial_number}/revoke")
def revoke_certificate(serial_number: int) -> Dict[str, str]:
    cert = ca.revoke(serial_number)
    if not cert:
        raise HTTPException(status_code=404, detail="Certificate not found")
    return _serialize_certificate(cert)


def _serialize_certificate(cert: IssuedCertificate) -> Dict[str, str]:
    return {
        "serial_number": str(cert.serial_number),
        "subject": cert.subject,
        "pem": cert.pem,
        "not_valid_before": cert.not_valid_before.isoformat(),
        "not_valid_after": cert.not_valid_after.isoformat(),
        "revoked": str(cert.revoked).lower(),
    }
