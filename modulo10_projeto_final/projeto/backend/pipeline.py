"""CI/CD orchestration utilities."""
from __future__ import annotations

import base64
import hashlib
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Dict, List

from code_signing import CodeSigningCA, SigningCredential
from kms import MockKMS
from signing import ArtifactSigner, SignedArtifact


@dataclass
class PipelineStep:
    name: str
    status: str
    detail: str
    completed_at: datetime


@dataclass
class PipelineExecution:
    pipeline_id: str
    application: str
    environment: str
    credential: SigningCredential
    artifact: SignedArtifact
    steps: List[PipelineStep] = field(default_factory=list)


class PipelineOrchestrator:
    def __init__(self, kms: MockKMS, ca: CodeSigningCA, signer: ArtifactSigner) -> None:
        self.kms = kms
        self.ca = ca
        self.signer = signer
        self._executions: Dict[str, PipelineExecution] = {}

    def run_pipeline(self, application: str, environment: str, payload: str) -> PipelineExecution:
        pipeline_id = f"pipeline-{len(self._executions)+1}"
        credential = self.ca.issue_signing_certificate(f"{application}-{environment}")
        steps: List[PipelineStep] = []

        # Build step
        digest = hashlib.sha256(payload.encode()).digest()
        steps.append(
            PipelineStep(
                name="build",
                status="success",
                detail=f"SHA256={base64.b64encode(digest).decode()}",
                completed_at=datetime.now(timezone.utc),
            )
        )

        # Sign step
        artifact = self.signer.sign(
            artifact_id=f"{application}:{environment}:{pipeline_id}",
            key_alias=credential.key_alias,
            payload=payload.encode(),
            metadata={"environment": environment},
        )
        steps.append(
            PipelineStep(
                name="sign",
                status="success",
                detail=f"Signed with {credential.key_alias}",
                completed_at=datetime.now(timezone.utc),
            )
        )

        # Deploy step
        steps.append(
            PipelineStep(
                name="deploy",
                status="success",
                detail=f"Deployed to {environment}",
                completed_at=datetime.now(timezone.utc),
            )
        )

        execution = PipelineExecution(
            pipeline_id=pipeline_id,
            application=application,
            environment=environment,
            credential=credential,
            artifact=artifact,
            steps=steps,
        )
        self._executions[pipeline_id] = execution
        return execution

    def list_executions(self) -> Dict[str, PipelineExecution]:
        return dict(self._executions)
