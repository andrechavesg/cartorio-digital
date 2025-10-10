# Backend – Microserviços da AC ICP-Brasil

Este módulo reúne os microserviços necessários para operação da AC em conformidade com os controles DOC-ICP. Cada serviço segue padrões de arquitetura limpa, integrações seguras com HSM/KMS e contratos descritos em docs/architecture.

## Serviços

- identity – Gestão de agentes de registro e prova de identidade.
- enrollment – Orquestração de dossiês, coleta de evidências e aprovação.
- issuance – Emissão de certificados, integração com AWS CloudHSM/KMS e perfis DOC-ICP-08.
- revocation – Workflow de revogação dual-control e publicação de CRL.
- validation – OCSP e TSA.
- audit – Trilha imutável, assinaturas digitais, exportações para auditoria.
- publisher – Publicação em repositórios oficiais, CRL/OCSP responders, relatórios.

Cada serviço possui um README com detalhes de endpoints, modelo de domínio, testes e instruções de deploy.

## Tecnologias

- Java 21 + Quarkus 3.x, Kotlin para camadas específicas.
- Mensageria: Amazon SQS/SNS + Kafka MSK para eventos críticos.
- Banco: Amazon Aurora PostgreSQL (com replicação cross-region) e DynamoDB para logs imutáveis.
- Integração com CloudHSM por PKCS#11.

## Padrões de Código

- Testes unitários com JUnit 5 e Mockito.
- Testes de integração via Testcontainers.
- Contratos de API expostos via OpenAPI 3.
- Observabilidade: OpenTelemetry, métricas Prometheus.

## Como executar localmente

Utilize o arquivo docker-compose.yml na raiz do projeto, com Localstack e um mock de HSM. Cada serviço expõe scripts ./mvnw para build e testes. Consulte o README específico de cada serviço para comandos detalhados.

CI/CD automatiza lint, testes, análise SAST/DAST e deploy via pipelines descritos em devsecops.
