package br.com.cartoriodigital.issuance.domain;

/**
 * Eventos relevantes para rastrear o ciclo de vida dos pedidos de emissão.
 */
public enum IssuanceEventType {
    ORDER_RECEIVED,
    ORDER_QUEUED,
    SIGNING_STARTED,
    SIGNING_COMPLETED,
    ORDER_FAILED,
    ORDER_REVOKED;
}
