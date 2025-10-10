package br.com.cartoriodigital.identity.domain;

/**
 * Estados possíveis do solicitante durante a prova de identidade.
 * Mantido em conformidade com o modelo definido no capítulo 3 do plano técnico.
 */
public enum ApplicantStatus {
    PENDING_VERIFICATION,
    APPROVED,
    REJECTED;

    public boolean isTerminal() {
        return this == APPROVED || this == REJECTED;
    }
}
