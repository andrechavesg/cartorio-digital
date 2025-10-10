package br.com.cartoriodigital.publisher.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/**
 * Destino de publicação ou distribuição dos artefatos.
 */
@Embeddable
public class DistributionEndpoint {

    @Column(name = "endpoint_type", nullable = false, length = 32)
    private String type;

    @Column(name = "endpoint_destination", nullable = false, length = 256)
    private String destination;

    @Column(name = "requires_signature", nullable = false)
    private boolean requiresSignature;

    protected DistributionEndpoint() {
        // JPA
    }

    private DistributionEndpoint(String type, String destination, boolean requiresSignature) {
        this.type = Objects.requireNonNull(type, "type");
        this.destination = Objects.requireNonNull(destination, "destination");
        this.requiresSignature = requiresSignature;
    }

    public static DistributionEndpoint of(String type, String destination, boolean requiresSignature) {
        return new DistributionEndpoint(type, destination, requiresSignature);
    }

    public boolean requiresSignature() {
        return requiresSignature;
    }

    public String getDestination() {
        return destination;
    }
}
