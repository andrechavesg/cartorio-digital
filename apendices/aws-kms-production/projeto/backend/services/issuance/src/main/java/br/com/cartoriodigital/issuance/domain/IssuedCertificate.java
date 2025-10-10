package br.com.cartoriodigital.issuance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Dados retornados após sucesso na emissão.
 */
@Embeddable
public class IssuedCertificate {

    @Column(name = "certificate_pem", length = 8192)
    private String certificatePem;

    @Column(name = "certificate_chain_pem", length = 8192)
    private String certificateChainPem;

    @Column(name = "pkcs12_object_uri", length = 512)
    private String pkcs12ObjectUri;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    protected IssuedCertificate() {
        // JPA
    }

    private IssuedCertificate(String certificatePem,
                              String certificateChainPem,
                              String pkcs12ObjectUri,
                              OffsetDateTime issuedAt) {
        this.certificatePem = Objects.requireNonNull(certificatePem, "certificatePem");
        this.certificateChainPem = certificateChainPem;
        this.pkcs12ObjectUri = pkcs12ObjectUri;
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt");
    }

    public static IssuedCertificate of(String certificatePem,
                                       String certificateChainPem,
                                       String pkcs12ObjectUri,
                                       OffsetDateTime issuedAt) {
        return new IssuedCertificate(certificatePem, certificateChainPem, pkcs12ObjectUri, issuedAt);
    }

    public String getCertificatePem() {
        return certificatePem;
    }

    public String getCertificateChainPem() {
        return certificateChainPem;
    }

    public String getPkcs12ObjectUri() {
        return pkcs12ObjectUri;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }
}
