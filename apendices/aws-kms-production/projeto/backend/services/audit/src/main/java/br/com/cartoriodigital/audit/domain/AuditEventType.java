package br.com.cartoriodigital.audit.domain;

/**
 * Tipos de eventos auditáveis alinhados com matriz DOC-ICP-04.
 */
public enum AuditEventType {
    IDENTITY_VERIFICATION,
    ENROLLMENT_UPDATE,
    CERTIFICATE_ISSUANCE,
    CERTIFICATE_REVOCATION,
    ACCESS_CONTROL,
    SECURITY_ALERT,
    CONFIGURATION_CHANGE;
}
