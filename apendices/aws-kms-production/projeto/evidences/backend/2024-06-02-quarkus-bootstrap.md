# Evidência – Bootstrap Quarkus dos Serviços Backend

## Artefatos Entregues
- Criação de `application.properties` com mTLS obrigatório, OIDC, OTLP e parâmetros de banco para cada microserviço (`backend/services/*/src/main/resources/application.properties`).
- Definição de classes `OpenApiConfig` por serviço com `@OpenAPIDefinition` e `SecurityScheme` alinhados às URLs mapeadas (`backend/services/*/src/main/java/**/config/OpenApiConfig.java`).
- Padronização das portas expostas (8081–8087) e dos endpoints de telemetria, conforme Capítulo 3 – Implementação do Backend.

## Consultas/Comandos
- Comando planejado para validação: `mvn -f backend/pom.xml test` (não executado por ausência do Maven no ambiente; vide bloqueio registrado em evidência anterior).

## Observações de Conformidade
- As propriedades reforçam mTLS e exigência de client-auth, conforme DOC-ICP-04/05.
- OpenAPI fica documentado com contato de compliance e segurança OIDC integrada, atendendo requisitos de rastreabilidade do PLAN.md.
