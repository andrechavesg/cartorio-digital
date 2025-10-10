# Evidência – API do Serviço de Identidade (Fase 3)

## Artefatos
- Repositório JPA/Quarkus para solicitantes (`backend/services/identity/src/main/java/br/com/cartoriodigital/identity/infrastructure/persistence/PanacheApplicantRepository.java`).
- Serviço de aplicação com regras de dual-control, verificação e consentimento (`backend/services/identity/src/main/java/br/com/cartoriodigital/identity/application/ApplicantService.java`).
- API RESTFUL mTLS/OIDC com endpoints `/v1/applicants` alinhados ao PLAN (`backend/services/identity/src/main/java/br/com/cartoriodigital/identity/infrastructure/rest/ApplicantResource.java`).
- Tratamento padronizado de erros (`backend/services/identity/src/main/java/br/com/cartoriodigital/identity/infrastructure/rest/error`).
- Testes unitários para o serviço (`backend/services/identity/src/test/java/br/com/cartoriodigital/identity/application/ApplicantServiceTest.java`).

## Comandos/Validações
- Tentativa de executar `mvn -f backend/pom.xml test` permanece bloqueada pela ausência do Maven no ambiente (ver bloqueio registrado em evidences/backend/2024-06-02-domain-modeling.md). Testes escritos aguardam Maven Wrapper ou instalação do Maven para validação.

## Observações de Conformidade
- Endpoints garantem dupla custódia (RA Agent + Security Officer) conforme DOC-ICP-03/05.
- Todas as rotas exigem mTLS e autenticação OIDC via propriedades Quarkus.
- Logs estruturados e OpenAPI atualizados automaticamente via configuração SmallRye.
