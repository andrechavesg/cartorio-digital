package br.com.cartoriodigital.validation.domain;

/**
 * Situações possíveis em respostas OCSP.
 */
public enum OcspStatusCode {
    GOOD,
    REVOKED,
    UNKNOWN;
}
