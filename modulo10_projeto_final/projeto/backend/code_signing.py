"""Support for code-signing certificates."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Dict

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import ExtendedKeyUsageOID, NameOID

from kms import MockKMS


@dataclass
class SigningCredential:
    serial_number: int
    subject: str
    certificate_pem: str
    key_alias: str
    not_valid_before: datetime
    not_valid_after: datetime


class CodeSigningCA:
    def __init__(self, common_name: str, kms: MockKMS) -> None:
        self.kms = kms
        self._root_key = rsa.generate_private_key(public_exponent=65537, key_size=4096)
        subject = issuer = x509.Name(
            [x509.NameAttribute(NameOID.COMMON_NAME, common_name)]
        )
        now = datetime.now(timezone.utc)
        self._certificate = (
            x509.CertificateBuilder()
            .subject_name(subject)
            .issuer_name(issuer)
            .public_key(self._root_key.public_key())
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=3650))
            .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
            .sign(self._root_key, hashes.SHA256())
        )
        self._issued: Dict[int, SigningCredential] = {}

    @property
    def certificate_pem(self) -> str:
        return self._certificate.public_bytes(serialization.Encoding.PEM).decode()

    def issue_signing_certificate(self, common_name: str) -> SigningCredential:
        alias = f"code-sign/{len(self._issued)+1}"
        key_metadata = self.kms.create_key(alias)
        now = datetime.now(timezone.utc)
        certificate = (
            x509.CertificateBuilder()
            .subject_name(x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, common_name)]))
            .issuer_name(self._certificate.subject)
            .public_key(
                serialization.load_pem_public_key(key_metadata.public_key_pem.encode())
            )
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=180))
            .add_extension(
                x509.ExtendedKeyUsage([ExtendedKeyUsageOID.CODE_SIGNING]),
                critical=False,
            )
        ).sign(self._root_key, hashes.SHA256())

        credential = SigningCredential(
            serial_number=certificate.serial_number,
            subject=certificate.subject.rfc4514_string(),
            certificate_pem=certificate.public_bytes(serialization.Encoding.PEM).decode(),
            key_alias=alias,
            not_valid_before=certificate.not_valid_before_utc,
            not_valid_after=certificate.not_valid_after_utc,
        )
        self._issued[certificate.serial_number] = credential
        return credential

    def list_credentials(self) -> Dict[int, SigningCredential]:
        return dict(self._issued)
