from typing import Dict, List

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, EmailStr

from compliance import CitizenRecord, ComplianceEngine
from crypto import CertificateAuthority, IssuedCertificate


class CitizenInput(BaseModel):
    name: str
    email: EmailStr
    document_type: str
    document_id: str


class CertificateRequest(BaseModel):
    email: EmailStr
    common_name: str


app = FastAPI(title="Cartório Digital - Requisitos Regulatórios")
ca = CertificateAuthority("Cartorio Digital Compliance CA")
compliance_engine = ComplianceEngine()
registrations: Dict[str, CitizenRecord] = {}
audit_log: List[Dict[str, object]] = []


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "regulatorio"}


@app.post("/citizens")
def register_citizen(citizen: CitizenInput) -> Dict[str, str]:
    record = CitizenRecord(**citizen.model_dump())
    registrations[citizen.email] = record
    return {"message": "Citizen registered", "email": citizen.email}


@app.post("/certificates/compliant")
def issue_compliant_certificate(request: CertificateRequest) -> Dict[str, object]:
    citizen = registrations.get(request.email)
    if not citizen:
        raise HTTPException(status_code=404, detail="Citizen not registered")
    certificate = ca.issue_client_certificate(request.common_name, request.email)
    results = compliance_engine.evaluate(citizen, certificate)
    audit_entry = {
        "email": citizen.email,
        "results": [result.__dict__ for result in results],
        "certificate_serial": certificate.serial_number,
    }
    audit_log.append(audit_entry)
    if not all(result.passed for result in results):
        raise HTTPException(status_code=400, detail=audit_entry)
    return {
        "certificate": _serialize_certificate(certificate),
        "compliance": audit_entry,
    }


@app.get("/audit")
def list_audit() -> Dict[str, List[Dict[str, object]]]:
    return {"entries": audit_log}


def _serialize_certificate(cert: IssuedCertificate) -> Dict[str, str]:
    return {
        "serial_number": str(cert.serial_number),
        "subject": cert.subject,
        "pem": cert.pem,
        "not_valid_before": cert.not_valid_before.isoformat(),
        "not_valid_after": cert.not_valid_after.isoformat(),
    }
