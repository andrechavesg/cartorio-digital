"""PKI helper tailored for TLS/mTLS experiments."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Dict, Optional

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import ExtendedKeyUsageOID, NameOID


@dataclass
class IssuedCertificate:
    serial_number: int
    subject: str
    pem: str
    not_valid_before: datetime
    not_valid_after: datetime
    revoked: bool = False
    purpose: str = "generic"


class CertificateAuthority:
    def __init__(self, common_name: str) -> None:
        self.common_name = common_name
        self._key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        subject = issuer = x509.Name(
            [x509.NameAttribute(NameOID.COMMON_NAME, common_name)]
        )
        now = datetime.now(timezone.utc)
        self._certificate = (
            x509.CertificateBuilder()
            .subject_name(subject)
            .issuer_name(issuer)
            .public_key(self._key.public_key())
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=3650))
            .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
            .sign(self._key, hashes.SHA256())
        )
        self._issued: Dict[int, IssuedCertificate] = {}

    @property
    def certificate_pem(self) -> str:
        return self._certificate.public_bytes(serialization.Encoding.PEM).decode()

    def _issue(self, builder: x509.CertificateBuilder, purpose: str) -> IssuedCertificate:
        certificate = builder.sign(self._key, hashes.SHA256())
        issued = IssuedCertificate(
            serial_number=certificate.serial_number,
            subject=certificate.subject.rfc4514_string(),
            pem=certificate.public_bytes(serialization.Encoding.PEM).decode(),
            not_valid_before=certificate.not_valid_before_utc,
            not_valid_after=certificate.not_valid_after_utc,
            purpose=purpose,
        )
        self._issued[issued.serial_number] = issued
        return issued

    def issue_client_certificate(self, common_name: str, email: str) -> IssuedCertificate:
        now = datetime.now(timezone.utc)
        builder = (
            x509.CertificateBuilder()
            .subject_name(
                x509.Name(
                    [
                        x509.NameAttribute(NameOID.COMMON_NAME, common_name),
                        x509.NameAttribute(NameOID.EMAIL_ADDRESS, email),
                    ]
                )
            )
            .issuer_name(self._certificate.subject)
            .public_key(self._key.public_key())
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=365))
            .add_extension(
                x509.ExtendedKeyUsage([ExtendedKeyUsageOID.CLIENT_AUTH]),
                critical=False,
            )
        )
        return self._issue(builder, "client")

    def issue_server_certificate(self, hostname: str) -> IssuedCertificate:
        now = datetime.now(timezone.utc)
        builder = (
            x509.CertificateBuilder()
            .subject_name(
                x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, hostname)])
            )
            .issuer_name(self._certificate.subject)
            .public_key(self._key.public_key())
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=90))
            .add_extension(
                x509.SubjectAlternativeName([x509.DNSName(hostname)]), critical=False
            )
            .add_extension(
                x509.ExtendedKeyUsage([ExtendedKeyUsageOID.SERVER_AUTH]),
                critical=False,
            )
        )
        return self._issue(builder, "server")

    def revoke(self, serial_number: int) -> Optional[IssuedCertificate]:
        cert = self._issued.get(serial_number)
        if cert:
            cert.revoked = True
        return cert

    def get(self, serial_number: int) -> Optional[IssuedCertificate]:
        return self._issued.get(serial_number)

    def list_all(self) -> Dict[int, IssuedCertificate]:
        return dict(self._issued)
