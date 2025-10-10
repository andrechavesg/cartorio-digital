package br.com.cartoriodigital.audit.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditDomainTest {

    @Test
    void digestRequiresSignature() {
        AuditDigest digest = AuditDigest.create(OffsetDateTime.now().minusHours(1), OffsetDateTime.now(), "abc123");
        digest.sign("kms:signature", OffsetDateTime.now());
        assertTrue(digest.isSigned());
    }

    @Test
    void exportTransitionsToCompleted() {
        AuditExportRequest request = AuditExportRequest.create(AuditExportRequest.ExportFormat.CSV, "auditor-1", OffsetDateTime.now());
        request.markCompleted("s3://audit/export.csv");
        assertEquals("COMPLETED", request.getStatus());
    }
}
