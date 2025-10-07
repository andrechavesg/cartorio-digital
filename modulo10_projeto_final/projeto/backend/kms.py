"""Mock KMS/HSM integration layer."""
from __future__ import annotations

import base64
from dataclasses import dataclass
from typing import Dict

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa


@dataclass
class KeyMetadata:
    alias: str
    public_key_pem: str


class MockKMS:
    def __init__(self) -> None:
        self._keys: Dict[str, rsa.RSAPrivateKey] = {}

    def create_key(self, alias: str) -> KeyMetadata:
        private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
        self._keys[alias] = private_key
        public_pem = (
            private_key.public_key()
            .public_bytes(
                serialization.Encoding.PEM,
                serialization.PublicFormat.SubjectPublicKeyInfo,
            )
            .decode()
        )
        return KeyMetadata(alias=alias, public_key_pem=public_pem)

    def sign(self, alias: str, data: bytes) -> str:
        key = self._keys[alias]
        signature = key.sign(
            data,
            padding.PSS(mgf=padding.MGF1(hashes.SHA256()), salt_length=padding.PSS.MAX_LENGTH),
            hashes.SHA256(),
        )
        return base64.b64encode(signature).decode()

    def list_keys(self) -> Dict[str, int]:
        return {alias: key.key_size for alias, key in self._keys.items()}
