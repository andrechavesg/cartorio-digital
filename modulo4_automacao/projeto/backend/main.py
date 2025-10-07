import secrets
from typing import Dict

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from acme import AcmeDirectory, AcmeOrder
from crypto import CertificateAuthority, IssuedCertificate


class OrderRequest(BaseModel):
    domain: str


class ChallengeCompletion(BaseModel):
    token: str


app = FastAPI(title="Cartório Digital - Automação ACME")
ca = CertificateAuthority("Cartorio Digital ACME CA")
directory = AcmeDirectory(ca)


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "module": "automacao"}


@app.get("/ca")
def get_ca() -> Dict[str, str]:
    return {"certificate": ca.certificate_pem}


@app.post("/acme/orders")
def new_order(request: OrderRequest) -> Dict[str, str]:
    token = secrets.token_urlsafe(16)
    order = directory.new_order(request.domain, token)
    return _serialize_order(order)


@app.post("/acme/orders/{order_id}/complete")
def complete_order(order_id: str, completion: ChallengeCompletion) -> Dict[str, str]:
    try:
        certificate = directory.complete_challenge(order_id, completion.token)
    except KeyError as exc:
        raise HTTPException(status_code=404, detail="Order not found") from exc
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    return {
        "message": "order validated",
        "certificate": _serialize_certificate(certificate),
    }


@app.get("/acme/orders")
def list_orders() -> Dict[str, Dict[str, str]]:
    return {
        "orders": {
            order_id: _serialize_order(order)
            for order_id, order in directory.list_orders().items()
        }
    }


def _serialize_order(order: AcmeOrder) -> Dict[str, str]:
    return {
        "order_id": order.order_id,
        "domain": order.domain,
        "token": order.token,
        "status": order.status,
        "certificate_serial": (
            str(order.certificate_serial) if order.certificate_serial else None
        ),
    }


def _serialize_certificate(cert: IssuedCertificate) -> Dict[str, str]:
    return {
        "serial_number": str(cert.serial_number),
        "subject": cert.subject,
        "pem": cert.pem,
        "not_valid_before": cert.not_valid_before.isoformat(),
        "not_valid_after": cert.not_valid_after.isoformat(),
    }
