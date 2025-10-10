package br.com.cartoriodigital.enrollment.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRequestRepository {

    EnrollmentRequest save(EnrollmentRequest request);

    Optional<EnrollmentRequest> findById(UUID id);

    List<EnrollmentRequest> findByApplicant(String applicantId);
}
