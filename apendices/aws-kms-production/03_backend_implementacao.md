# Capítulo 3 – Implementação do Backend da Plataforma de AC

Este capítulo detalha a implementação dos serviços backend responsáveis por dar vida aos fluxos da Autoridade Certificadora (AC) do Cartório Digital. Os componentes aqui descritos estão alinhados com os controles regulatórios da ICP-Brasil (DOC-ICP-02/03/04/05/08/10/15) e com a arquitetura de referência apresentada no [Capítulo 1](01_arquitetura_producao.md).

## 3.1 Fundamentos de Engenharia

- **Organização de código**: monorepo Git com workspaces (`backend/identity`, `backend/issuance`, etc.) padronizados por linguagem (Java/Quarkus) e utilitários em Kotlin (coroutines) e Go (lambdas).
- **Padrões arquiteturais**: DDD + Hexagonal Architecture para isolamento de domínios, CQRS aplicado onde leitura ≠ escrita (ex.: relatórios vs. workflow de emissão).
- **Pipelines CI/CD**: GitHub Actions executa lint → testes → análise estática (SonarQube/Snyk) → build de container → testes de contrato → deploy automático em `dev/stage`, com *manual approval* para `prod` (CodePipeline).
- **Feature flags**: LaunchDarkly (ou AWS AppConfig) controla ativações graduais de fluxos sensíveis (ex.: emissão automática).
- **Configuração**: parâmetros sensíveis em AWS Secrets Manager (`/cartorio/<env>/<service>/`) e parâmetros públicos no AWS SSM Parameter Store com versionamento.
- **Observabilidade by design**: logs estruturados JSON (Elastic ECS format), métricas Prometheus scraping via App Mesh, tracing distribuído com AWS X-Ray.

## 3.2 Mapa de Serviços Backend

| Serviço | Propósito | Principais integrações | Domínio DDD |
| --- | --- | --- | --- |
| **Identity Proofing API** | Onboarding e verificação de identidade | Gov.br, biometria, Evidence Vault | `IdentityVerification` |
| **Enrollment Service** | Gerencia entidades finais e perfis | Evidence Vault, Issuance API | `Enrollment` |
| **Certificate Issuance API** | Emissão/renovação de certificados | EJBCA (ECS), CloudHSM, AWS KMS | `Issuance` |
| **Signing Orchestrator** | Orquestra fluxos CMP/SCEP/ACME/REST | Issuance API, EventBridge | `Issuance` |
| **Revocation Workflow** | Processa pedidos de revogação | RA Portal, CRL Publisher, OCSP | `Revocation` |
| **CRL Publisher** | Gera e publica CRL/Delta CRL | S3, CloudFront, Secrets Manager | `Publishing` |
| **Validation Service (OCSP/TSA)** | Disponibiliza OCSP/TSA assinados | KMS/CloudHSM, Redis cache, API Gateway | `Validation` |
| **Audit Collector** | Armazena trilha imutável e exporta evidências | CloudTrail, OpenSearch, S3 Glacier | `Compliance` |
| **Evidence Vault** | Guarda evidências criptografadas | S3 (Object Lock), DynamoDB, KMS | `Evidence` |
| **Metrics & Alerts** | SLA/SLO, dashboards, alertas | CloudWatch, Grafana, SNS | `Observability` |

```
┌──────────────┐     ┌──────────────────────┐     ┌──────────────────┐
│ RA Portal /  │ --> │ Identity Proofing API│ --> │ Enrollment Service│
│ External APIs│     │ (mTLS + JWT)         │     │ (DDD Aggregates) │
└──────────────┘     └─────────┬────────────┘     └───────┬──────────┘
                    Evidence   │                           │
                     hashes ┌──▼────────────┐  Profiles ┌──▼──────────────┐
                             │Evidence Vault│──────────>│Certificate      │
                             │ (S3+KMS)     │           │ Issuance API    │
                             └──▲───────────┘           └──┬──────────────┘
                                │                          │
                                │ events                   │ mTLS CMP/ACME
                                │                          │
                       ┌────────┴──────────┐    CRL/OCSP   │
                       │Revocation Workflow│--------------▶│EJBCA Cluster │
                       └──────┬────────────┘    updates    │(ECS + KMS/HSM)
                              │                           └────┬──────────┘
                              │ audit logs                     │
                              ▼                                ▼
                       ┌─────────────┐                ┌──────────────────┐
                       │Audit Collector│<────────────▶│Validation Service │
                       └─────────────┘   metrics/logs └──────────────────┘
```

## 3.3 Identity Proofing & RA Service

### Requisitos-chave

- Suporta onboarding **presencial** e **remoto assistido**, com captura de presencialidade (fotos, vídeos) e armazenamento da evidência criptografada.
- Implementa dupla custódia: decisões críticas exigem co-aprovação de um Operador Master (mapeado em IAM + RBAC interno).
- Integra com `Evidence Vault` via API assinada (AWS SigV4) para persistência de artefatos.
- Mantém trilha de auditoria imutável em DynamoDB Streams → Kinesis Firehose → S3 (`audit/evidence/`).

### Modelo de dados (simplificado)

| Entidade | Campos principais | Observações |
| --- | --- | --- |
| `Applicant` | `id`, `personType`, `documentId`, `status`, `createdAt`, `updatedAt` | `status` ∈ {`PENDING_VERIFICATION`, `APPROVED`, `REJECTED`} |
| `VerificationSession` | `sessionId`, `applicantId`, `method`, `evidenceUri`, `verifierId`, `result`, `score` | `method` ∈ {`IN_PERSON`, `REMOTE_ASSISTED`} |
| `RAActionLog` | `actionId`, `actorId`, `action`, `reason`, `timestamp`, `signature` | `signature` = assinatura digital do agente RA (KMS CMK) |

### Endpoints REST

| Método | Endpoint | Descrição | Autorização |
| --- | --- | --- | --- |
| `POST` | `/v1/applicants` | Cria solicitante | OAuth2 client credentials (`ra-portal`) |
| `POST` | `/v1/applicants/{id}/verification` | Inicia verificação (define método) | RA Agent com MFA |
| `PATCH` | `/v1/applicants/{id}/decision` | Aprova/Rejeita | Dupla custódia (workflow + assinatura digital) |
| `GET` | `/v1/applicants/{id}/evidence` | Obtém referência para download | Auditor (read-only) |

### Compliance

- Captura de consentimento (DOC-ICP-05) armazenada e assinada.
- Logs assinados (KMS Sign) garantem não repúdio (DOC-ICP-04).
- SLA: verificação ≤ 30 min (95º percentil) monitorado por CloudWatch Synthetics.

## 3.4 Serviços de Emissão

### Certificate Issuance API

- Exposta via API Gateway (internal) com mTLS obrigatório (certificados emitidos pelo próprio cartório).
- Implementa fluxos `REST`, `CMP`, `SCEP` e `ACME`. Cada fluxo converte requisições em comandos do dominio `Issuance`.
- Conecta-se ao cluster EJBCA (ECS) via gRPC assinado (mTLS + SigV4) para criação/gestão de end-entities.
- Respeita perfis de certificados (DOC-ICP-08) carregados do `Profile Manager` (configurações versionadas no DynamoDB).
- Suporte a **pre-approval**: RA pode pré-aprovar emissões para acelerar resposta automática.

#### Fluxo REST (`/v1/certificates`)

1. Recebe `CSR` + metadados (perfil, CA target, validade).
2. Valida CSR (algoritmo, key usage, SAN) usando biblioteca AWS PQ SDK (quando aplicável).
3. Busca políticas do perfil (ex.: `TLS-Server-Profile`, `TSA-Profile`).
4. Envia `IssueCertificateCommand` ao `Signing Orchestrator` (EventBridge bus `ac-issuance`).
5. Orchestrator interage com EJBCA + CloudHSM/KMS, obtém certificado, encapsula com TSA opcional.
6. Retorna certificado (`PEM` + `P12` opcional) e atualiza metadata no Aurora PostgreSQL.
7. Publica evento `CertificateIssued` (SNS) para notificações e atualiza CRL delta.

### Renovação automática

- Lambda `cert-renewal-scheduler` roda diariamente: busca certificados com expiração < 30 dias.
- Para certificados ACME/SCEP, utiliza desafios automatizados (DNS-01/HTTP-01) via Route 53 + ALB.
- Registra falhas em `issuance_failures` (Aurora) e dispara alerta SNS `SECURITY-INCIDENT` se taxa de falha > 5%.

## 3.5 Revogação & Publicação

### Revocation Workflow

- Interface gRPC + REST para receber pedidos de revogação (usuário, RA, auditor).
- Implementa política de aprovação dual (RA + Admin de Segurança) para revogações de CA intermediária (DOC-ICP-03).
- Mantém fila SQS `revocation-requests` com mensagens S3 (payload criptografado).
- Lambda `revocation-verifier` valida identidade, atualiza status e envia comando para EJBCA.

### CRL Publisher

- Executa a cada 30 minutos (configurável) ou sob demanda após revogação crítica.
- Gera CRL e Delta CRL via EJBCA CLI, armazena em S3 (`crl/current.crl`, `crl/delta.crl`) com `Object Lock`.
- Publica metadados em DynamoDB (`crl_history`) com hash SHA-256 e timestamp.
- Invalida cache CloudFront (`/crl/*`) e atualiza endpoint de OCSP (Lambda `ocsp-cache-invalidator`).

### Fluxo resumido

```
1. Pedido recebido (REST/Portal) → gravação em SQS
2. Verificação dual → assinatura digital → comando EJBCA
3. Atualização de CRL → upload S3 → CloudFront Invalidation
4. Notificação SNS para subscritores + Auditoria
```

## 3.6 Serviços de Validação (OCSP/TSA)

- **OCSP Responder**: construído em Go para baixa latência, hospedado em AWS Lambda com provisioned concurrency (p99 < 100ms). Cache Redis (ElastiCache) com TTL 5 min, fallback para EJBCA OCSP se miss.
- **TSA Service**: roda em ECS Fargate, utiliza HSM (CloudHSM) para assinatura RFC 3161, sincronização NTP via Amazon Time Sync. Mantém contador monotônico em DynamoDB (`tsa_sequence`) com transações que garantem unicidade (DOC-ICP-15).
- **Hardening**: respostas assinadas em SHA-256 com carimbo de tempo adicional (`signatureTimeStampToken`), politicas de revogação para tokens comprometidos.
- **Monitoramento**: CloudWatch Synthetics testa OCSP e TSA a cada 5 minutos; alarmes se taxa de erro > 1% ou latência > 200 ms.

## 3.7 Audit & Compliance Service

- Pipeline imutável: serviços emitem eventos `AuditEvent` para Kinesis Data Stream (`audit-log-stream`). Firehose entrega em S3 (`audit/raw/AAAA/MM/DD/HH/`) com checksum e `Object Lock`.
- Lambda `audit-digest-generator` gera hash encadeado (Merkle Tree) por hora e armazena em DynamoDB (`audit_digest`). Hash raiz é assinado com KMS (`kms:Sign`) e armazenado em AWS Signer.
- **Exportação para auditoria**: APIs GraphQL (`/audit/reports`) permitem seleção por período, CA, tipo de evento. Exporta CSV/JSON assinados digitalmente e registra entrega em `audit_exports`.
- Integra com **AWS Config** e **Security Hub** para compliance contínua (DOC-ICP-04). Findings críticos geram ticket automático no Jira Service Management (webhook).

## 3.8 Estratégia de Testes Automatizados

| Camada | Ferramentas | Escopo | Critérios de aprovação |
| --- | --- | --- | --- |
| Unit | JUnit 5, Testcontainers | Domínio e adaptadores | Cobertura > 80% domínios críticos |
| Integração | Testcontainers (EJBCA, PostgreSQL), LocalStack | Fluxos REST/CMP/SCEP/ACME | Todos cenários happy path + erros comuns |
| Contrato | Pact (REST), Buf (gRPC) | Contratos internos/externos | Verificações rodando em pipeline `verify-contract` |
| Carga | k6, Gatling | OCSP/TSA, Issuance API | > 500 req/s com p99 < 200 ms |
| Segurança | OWASP ZAP, Burp automation | APIs expostas | Zero findings High/Critical antes de `prod` |
| Chaos/Resiliência | AWS Fault Injection | Revogação, TSA | RTO 1h, RPO 15 min comprovados |

- Pipelines integram com AWS CodeBuild para testes de carga pontuais (gatilho manual).
- Resultados de testes são enviados ao Grafana OnCall; falhas críticas bloqueiam promoção para `prod` (Policy-as-code no Open Policy Agent).

## 3.9 Backlog Técnico & Próximos Passos

- Implementar **SDK clientes** (Java/Node) com roteamento automático para fluxos REST/CMP.
- Automatizar **key ceremony rehearsal** com scripts Ansible + Step Functions (integração com Capítulo 6).
- Expandir OpenAPI/AsyncAPI com exemplos assinados e esquemas JSON Schema validados (publicar em `docs/api`).

---

→ Próximo capítulo: [AWS Private CA](03_aws_private_ca.md) descreve o provisionamento e a operação das CAs intermediárias em conjunto com os serviços aqui definidos.
