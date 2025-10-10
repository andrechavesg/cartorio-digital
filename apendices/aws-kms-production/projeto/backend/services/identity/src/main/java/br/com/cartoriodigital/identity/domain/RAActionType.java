package br.com.cartoriodigital.identity.domain;

/**
 * Ações relevantes para rastreabilidade da RA.
 */
public enum RAActionType {
    REGISTRATION_CREATED,
    VERIFICATION_STARTED,
    VERIFICATION_COMPLETED,
    APPROVAL_GRANTED,
    APPROVAL_REJECTED,
    EVIDENCE_REQUESTED;
}
