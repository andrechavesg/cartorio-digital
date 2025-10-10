package br.com.cartoriodigital.enrollment.infrastructure.persistence;

import br.com.cartoriodigital.enrollment.domain.EnrollmentRequest;
import br.com.cartoriodigital.enrollment.domain.EnrollmentRequestRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheEnrollmentRequestRepository implements EnrollmentRequestRepository, PanacheRepositoryBase<EnrollmentRequest, UUID> {

    @Override
    public EnrollmentRequest save(EnrollmentRequest request) {
        if (isPersistent(request)) {
            getEntityManager().merge(request);
        } else {
            persist(request);
        }
        flush();
        return request;
    }

    @Override
    public Optional<EnrollmentRequest> findById(UUID id) {
        return findByIdOptional(id);
    }

    @Override
    public List<EnrollmentRequest> findByApplicant(String applicantId) {
        return list("applicantId", applicantId);
    }
}
