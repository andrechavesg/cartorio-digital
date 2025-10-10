package br.com.cartoriodigital.publisher.domain;

/**
 * Estados de publicação de CRL/OCSP.
 */
public enum PublicationStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    FAILED;
}
