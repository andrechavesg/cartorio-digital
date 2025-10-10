package br.com.cartoriodigital.validation.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationDomainTest {

    @Test
    void ocspEntryTracksRevocation() {
        OffsetDateTime now = OffsetDateTime.now();
        OcspCacheEntry entry = OcspCacheEntry.good("00AA", now, now.plusMinutes(5));

        entry.markRevoked("KEY_COMPROMISE", now.plusMinutes(1), now.plusMinutes(1), now.plusMinutes(6));

        assertEquals(OcspStatusCode.REVOKED, entry.getStatus());
        assertEquals("KEY_COMPROMISE", entry.getRevocationReason());
    }

    @Test
    void tsaSequenceIsMonotonic() {
        TsaSequence sequence = TsaSequence.bootstrap("tsa-br", OffsetDateTime.now());
        long first = sequence.nextValue();
        long second = sequence.nextValue();
        assertEquals(first + 1, second);
    }
}
