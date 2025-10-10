package br.com.cartoriodigital.identity.domain;

import java.util.Optional;
import java.util.UUID;

public interface ApplicantRepository {

    Applicant save(Applicant applicant);

    Optional<Applicant> findById(UUID id);

    Optional<Applicant> findByDocumentId(String documentId);
}
