package br.com.cartoriodigital.issuance.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateOrderRepository {

    CertificateOrder save(CertificateOrder order);

    Optional<CertificateOrder> findById(UUID id);

    List<CertificateOrder> findByEnrollmentId(String enrollmentId);
}
