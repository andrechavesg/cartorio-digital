package br.com.cartoriodigital.issuance.infrastructure.persistence;

import br.com.cartoriodigital.issuance.domain.CertificateOrder;
import br.com.cartoriodigital.issuance.domain.CertificateOrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheCertificateOrderRepository implements CertificateOrderRepository, PanacheRepositoryBase<CertificateOrder, UUID> {

    @Override
    public CertificateOrder save(CertificateOrder order) {
        if (isPersistent(order)) {
            getEntityManager().merge(order);
        } else {
            persist(order);
        }
        flush();
        return order;
    }

    @Override
    public Optional<CertificateOrder> findById(UUID id) {
        return findByIdOptional(id);
    }

    @Override
    public List<CertificateOrder> findByEnrollmentId(String enrollmentId) {
        return list("enrollmentId", enrollmentId);
    }
}
