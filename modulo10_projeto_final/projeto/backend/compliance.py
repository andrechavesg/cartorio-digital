"""Simple compliance checks used to emulate requisitos regulatórios."""
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import List

from crypto import IssuedCertificate


@dataclass
class CitizenRecord:
    name: str
    email: str
    document_type: str
    document_id: str


@dataclass
class ComplianceResult:
    rule_id: str
    passed: bool
    detail: str


class ComplianceEngine:
    allowed_documents = {"RG", "CPF", "CNH"}

    def evaluate(self, citizen: CitizenRecord, certificate: IssuedCertificate) -> List[ComplianceResult]:
        results: List[ComplianceResult] = []
        results.append(self._check_document_type(citizen))
        results.append(self._check_document_length(citizen))
        results.append(self._check_certificate_validity(certificate))
        return results

    def _check_document_type(self, citizen: CitizenRecord) -> ComplianceResult:
        passed = citizen.document_type in self.allowed_documents
        detail = (
            "Documento permitido" if passed else "Tipo de documento não reconhecido"
        )
        return ComplianceResult("DOC_TYPE", passed, detail)

    def _check_document_length(self, citizen: CitizenRecord) -> ComplianceResult:
        passed = len(citizen.document_id) >= 5
        detail = "Documento válido" if passed else "Documento muito curto"
        return ComplianceResult("DOC_LENGTH", passed, detail)

    def _check_certificate_validity(self, certificate: IssuedCertificate) -> ComplianceResult:
        remaining = certificate.not_valid_after - datetime.now(timezone.utc)
        passed = remaining.days >= 30
        detail = f"Validade restante: {remaining.days} dias"
        return ComplianceResult("CERT_VALIDITY", passed, detail)
