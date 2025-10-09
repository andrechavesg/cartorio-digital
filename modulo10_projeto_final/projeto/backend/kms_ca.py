"""Certificate issuance backed by a mock KMS."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone
from typing import Dict

from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID

from kms import MockKMS


@dataclass
class IssuedCredential:
    serial_number: int
    subject: str
    certificate_pem: str
    key_alias: str
    public_key_pem: str
    not_valid_before: datetime
    not_valid_after: datetime


class KmsBackedCA:
    def __init__(self, common_name: str, kms: MockKMS) -> None:
        self.kms = kms
        self.common_name = common_name
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
        self._issued: Dict[int, IssuedCredential] = {}

    @property
    def certificate_pem(self) -> str:
        return self._certificate.public_bytes(serialization.Encoding.PEM).decode()

    def issue_certificate(self, common_name: str, email: str) -> IssuedCredential:
        alias = f"identity/{len(self._issued)+1}"
        key_metadata = self.kms.create_key(alias)
        now = datetime.now(timezone.utc)
        certificate = (
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
            .public_key(
                serialization.load_pem_public_key(key_metadata.public_key_pem.encode())
            )
            .serial_number(x509.random_serial_number())
            .not_valid_before(now)
            .not_valid_after(now + timedelta(days=365))
        ).sign(self._root_key, hashes.SHA256())

        credential = IssuedCredential(
            serial_number=certificate.serial_number,
            subject=certificate.subject.rfc4514_string(),
            certificate_pem=certificate.public_bytes(serialization.Encoding.PEM).decode(),
            key_alias=key_metadata.alias,
            public_key_pem=key_metadata.public_key_pem,
            not_valid_before=certificate.not_valid_before_utc,
            not_valid_after=certificate.not_valid_after_utc,
        )
        self._issued[credential.serial_number] = credential
        return credential

    def list_credentials(self) -> Dict[int, IssuedCredential]:
        return dict(self._issued)
