package br.com.cartoriodigital.identity.domain;

/**
 * Resultado de uma sessão de verificação de identidade.
 */
public enum VerificationResult {
    APPROVED,
    REJECTED,
    INCONCLUSIVE;

    public boolean isFinal() {
        return this != INCONCLUSIVE;
    }
}
