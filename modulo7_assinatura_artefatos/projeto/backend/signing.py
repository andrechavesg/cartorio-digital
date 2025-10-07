"""Artifact signing workflow using the mock KMS."""
from __future__ import annotations

import base64
import hashlib
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Dict

from kms import MockKMS


@dataclass
class SignedArtifact:
    artifact_id: str
    digest: str
    signature: str
    key_alias: str
    signed_at: datetime
    metadata: Dict[str, str]


class ArtifactSigner:
    def __init__(self, kms: MockKMS) -> None:
        self.kms = kms
        self._signed: Dict[str, SignedArtifact] = {}

    def sign(self, artifact_id: str, key_alias: str, payload: bytes, metadata: Dict[str, str]) -> SignedArtifact:
        digest_bytes = hashlib.sha256(payload).digest()
        signature = self.kms.sign(key_alias, digest_bytes)
        artifact = SignedArtifact(
            artifact_id=artifact_id,
            digest=base64.b64encode(digest_bytes).decode(),
            signature=signature,
            key_alias=key_alias,
            signed_at=datetime.now(timezone.utc),
            metadata=metadata,
        )
        self._signed[artifact_id] = artifact
        return artifact

    def list_signed(self) -> Dict[str, SignedArtifact]:
        return dict(self._signed)
