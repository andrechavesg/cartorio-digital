package br.com.cartoriodigital.revocation.domain;

/**
 * Motivos de revogação conforme RFC 5280.
 */
public enum RevocationReason {
    KEY_COMPROMISE,
    CA_COMPROMISE,
    AFFILIATION_CHANGED,
    SUPERSEDED,
    CESSATION_OF_OPERATION,
    CERTIFICATE_HOLD,
    PRIVILEGE_WITHDRAWN,
    A_A_COMPROMISE;
}
