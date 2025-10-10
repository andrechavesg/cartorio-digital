package br.com.cartoriodigital.revocation.domain;

/**
 * Estados do workflow de revogação conforme política dual-control.
 */
public enum RevocationStatus {
    PENDING_REVIEW,
    AWAITING_SECURITY_APPROVAL,
    APPROVED,
    REJECTED,
    COMPLETED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED || this == COMPLETED;
    }
}
