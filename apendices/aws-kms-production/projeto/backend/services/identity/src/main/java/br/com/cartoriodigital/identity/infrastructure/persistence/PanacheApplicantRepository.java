package br.com.cartoriodigital.identity.infrastructure.persistence;

import br.com.cartoriodigital.identity.domain.Applicant;
import br.com.cartoriodigital.identity.domain.ApplicantRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheApplicantRepository implements ApplicantRepository, PanacheRepositoryBase<Applicant, UUID> {

    @Override
    public Applicant save(Applicant applicant) {
        if (isPersistent(applicant)) {
            getEntityManager().merge(applicant);
        } else {
            persist(applicant);
        }
        flush();
        return applicant;
    }

    @Override
    public Optional<Applicant> findById(UUID id) {
        return findByIdOptional(id);
    }

    @Override
    public Optional<Applicant> findByDocumentId(String documentId) {
        return find("documentId", documentId).firstResultOptional();
    }
}
