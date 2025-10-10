package br.com.cartoriodigital.issuance.domain;

/**
 * Etapas do ciclo de vida do pedido de emissão.
 */
public enum CertificateOrderStatus {
    REQUESTED,
    QUEUED,
    SIGNING,
    SIGNED,
    FAILED,
    REVOKED;

    public boolean canTransitionTo(CertificateOrderStatus target) {
        return switch (this) {
            case REQUESTED -> target == QUEUED || target == FAILED;
            case QUEUED -> target == SIGNING || target == FAILED;
            case SIGNING -> target == SIGNED || target == FAILED;
            case SIGNED -> target == REVOKED;
            case FAILED, REVOKED -> false;
        };
    }

    public boolean isTerminal() {
        return this == SIGNED || this == FAILED || this == REVOKED;
    }
}
