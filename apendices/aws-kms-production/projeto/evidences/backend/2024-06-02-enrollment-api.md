# Evidência – API do Enrollment Service (Fase 3)

## Artefatos
- Modelagem persistente Panache (`backend/services/enrollment/src/main/java/br/com/cartoriodigital/enrollment/infrastructure/persistence/PanacheEnrollmentRequestRepository.java`).
- Serviço de aplicação com fluxo completo de matrícula, evidências e aprovações (`backend/services/enrollment/src/main/java/br/com/cartoriodigital/enrollment/application/EnrollmentService.java`).
- Endpoints REST protegidos com mTLS/OIDC em `/v1/enrollments` (`backend/services/enrollment/src/main/java/br/com/cartoriodigital/enrollment/infrastructure/rest/EnrollmentResource.java`).
- Tratamento padronizado de exceções (`backend/services/enrollment/src/main/java/br/com/cartoriodigital/enrollment/infrastructure/rest/error`).
- Testes unitários de regras críticas (`backend/services/enrollment/src/test/java/br/com/cartoriodigital/enrollment/application/EnrollmentServiceTest.java`).

## Validação
- Execução planejada: `mvn -f backend/pom.xml test` – ainda bloqueada pela ausência do Maven no ambiente (mesmo contexto registrado em evidences/backend/2024-06-02-domain-modeling.md).

## Conformidade
- Fluxo garante exigência de evidências obrigatórias antes da submissão (DOC-ICP-05).
- Aprovação dual-control exige papéis RA e Security Officer conforme DOC-ICP-03.
- OpenAPI atualizado automaticamente pelo SmallRye e retorno padronizado para auditoria.
