from typing import Dict

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, EmailStr

from crypto import CertificateAuthority, IssuedCertificate
from tls import validate_mutual_tls


class Citizen(BaseModel):
    name: str
    email: EmailStr


class ClientCertificateRequest(BaseModel):
    email: EmailStr
    common_name: str


class ServerCertificateRequest(BaseModel):
    hostname: str


class HandshakeRequest(BaseModel):
    server_serial: int
    client_serial: int


app = FastAPI(title="Cartório Digital - TLS/mTLS")
ca = CertificateAuthority("Cartorio Digital TLS CA")
registrations: Dict[str, Citizen] = {}


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "tls"}


@app.post("/citizens")
def enroll_citizen(citizen: Citizen) -> Dict[str, str]:
    registrations[citizen.email] = citizen
    return {"message": "Citizen registered", "email": citizen.email}


@app.post("/certificates/client")
def issue_client_certificate(request: ClientCertificateRequest) -> Dict[str, str]:
    if request.email not in registrations:
        raise HTTPException(status_code=404, detail="Citizen not registered")
    cert = ca.issue_client_certificate(request.common_name, request.email)
    return _serialize_certificate(cert)


@app.post("/certificates/server")
def issue_server_certificate(request: ServerCertificateRequest) -> Dict[str, str]:
    cert = ca.issue_server_certificate(request.hostname)
    return _serialize_certificate(cert)


@app.get("/certificates")
def list_certificates() -> Dict[str, Dict[str, Dict[str, str]]]:
    return {
        "certificates": {
            str(serial): _serialize_certificate(cert)
            for serial, cert in ca.list_all().items()
        }
    }


@app.post("/tls/handshake")
def simulate_handshake(request: HandshakeRequest) -> Dict[str, str]:
    ok = validate_mutual_tls(ca, request.server_serial, request.client_serial)
    if not ok:
        raise HTTPException(status_code=400, detail="mTLS validation failed")
    return {"message": "mTLS negotiation succeeded"}


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
