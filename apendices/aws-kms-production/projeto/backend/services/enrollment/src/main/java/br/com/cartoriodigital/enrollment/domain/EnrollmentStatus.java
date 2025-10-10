package br.com.cartoriodigital.enrollment.domain;

/**
 * Estados do fluxo de matrícula conforme definido na Fase 3.
 */
public enum EnrollmentStatus {
    DRAFT,
    UNDER_REVIEW,
    PENDING_APPROVAL,
    READY_FOR_ISSUANCE,
    REJECTED;

    public boolean isTerminal() {
        return this == READY_FOR_ISSUANCE || this == REJECTED;
    }
}
