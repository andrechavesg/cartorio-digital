package br.com.cartoriodigital.enrollment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ProfileRequirement {

    @Column(name = "requirement_code", nullable = false, length = 64)
    private String code;

    @Column(name = "requirement_description", nullable = false, length = 256)
    private String description;

    @Column(name = "requirement_mandatory", nullable = false)
    private boolean mandatory;

    protected ProfileRequirement() {
        // JPA
    }

    private ProfileRequirement(String code, String description, boolean mandatory) {
        this.code = Objects.requireNonNull(code, "code");
        this.description = Objects.requireNonNull(description, "description");
        this.mandatory = mandatory;
    }

    public static ProfileRequirement of(String code, String description, boolean mandatory) {
        return new ProfileRequirement(code, description, mandatory);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isMandatory() {
        return mandatory;
    }
}
