package br.com.cartoriodigital.publisher.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PublicationBatchTest {

    @Test
    void completesWithArtifacts() {
        PublicationBatch batch = PublicationBatch.schedule(
                "revocation-event",
                OffsetDateTime.now(),
                List.of(DistributionEndpoint.of("S3", "s3://cartorio/crl/current.crl", true))
        );

        batch.addArtifact(PublishedArtifact.of("CRL", "s3://cartorio/crl/current.crl", "hash-1"));
        batch.start();
        batch.complete();

        assertEquals(PublicationStatus.COMPLETED, batch.getStatus());
    }

    @Test
    void cannotCompleteWithoutArtifact() {
        PublicationBatch batch = PublicationBatch.schedule(
                "revocation-event",
                OffsetDateTime.now(),
                List.of(DistributionEndpoint.of("S3", "s3://cartorio/crl/current.crl", true))
        );
        batch.start();
        assertThrows(IllegalStateException.class, batch::complete);
    }
}
