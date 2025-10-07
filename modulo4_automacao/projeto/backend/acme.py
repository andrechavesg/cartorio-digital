"""Simplified ACME order management for the project."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Dict

from crypto import CertificateAuthority, IssuedCertificate


@dataclass
class AcmeOrder:
    order_id: str
    domain: str
    token: str
    created_at: datetime
    status: str = "pending"
    certificate_serial: int | None = None

    def is_expired(self) -> bool:
        return datetime.now(timezone.utc) > self.created_at + timedelta(minutes=15)


class AcmeDirectory:
    def __init__(self, ca: CertificateAuthority) -> None:
        self.ca = ca
        self._orders: Dict[str, AcmeOrder] = {}

    def new_order(self, domain: str, token: str) -> AcmeOrder:
        order = AcmeOrder(
            order_id=f"order-{len(self._orders)+1}",
            domain=domain,
            token=token,
            created_at=datetime.now(timezone.utc),
        )
        self._orders[order.order_id] = order
        return order

    def complete_challenge(self, order_id: str, provided_token: str) -> IssuedCertificate:
        order = self._orders[order_id]
        if order.is_expired():
            order.status = "expired"
            raise ValueError("Order expired")
        if order.token != provided_token:
            raise ValueError("Invalid challenge token")
        certificate = self.ca.issue_server_certificate(order.domain)
        order.status = "valid"
        order.certificate_serial = certificate.serial_number
        return certificate

    def get_order(self, order_id: str) -> AcmeOrder:
        return self._orders[order_id]

    def list_orders(self) -> Dict[str, AcmeOrder]:
        return dict(self._orders)
