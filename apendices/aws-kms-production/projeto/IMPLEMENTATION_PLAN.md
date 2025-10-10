# Implementation Plan – aws-kms-production

Este documento controla a execução prática do programa descrito em PLAN.md, mapeando cada entrega concreta (código, infraestrutura, automações e evidências) necessária para colocar a AC em produção conforme ICP-Brasil.

## Visão Geral

- Responsável: Codex (agente de implementação)
- Local do projeto: apendices/aws-kms-production/projeto
- Princípios: rastreabilidade total, conformidade com DOC-ICP, segurança por padrão, automação end-to-end.

## Status por Fase

| Fase | Objetivo | Entrega Chave | Status | Evidências |
| --- | --- | --- | --- | --- |
| Fase 3 – Backend | Implementar microserviços e integrações CloudHSM/KMS | Serviços em backend/services com builds, testes e pipelines | Em andamento | evidences/backend/2024-06-02-domain-modeling.md; evidences/backend/2024-06-02-quarkus-bootstrap.md; evidences/backend/2024-06-02-identity-api.md; evidences/backend/2024-06-02-enrollment-api.md; evidences/backend/2024-06-02-issuance-api.md; evidences/backend/2024-06-02-docker-compose.md |
| Fase 4 – Frontend | Portais Next.js com autenticação forte e UX regulatória | Aplicações em frontend/ com testes Playwright | Não iniciado | A preencher |
| Fase 5 – Infraestrutura | Terraform, Helm, ambientes e observabilidade | Infraestrutura em infrastructure/ com validações | Em andamento | A preencher |
| Fase 6 – DevSecOps | Pipelines, scans e evidências | DevSecOps e workflows GitHub Actions | Em andamento | A preencher |
| Fase 7 – Documentação | Runbooks, cerimônias, políticas, onboarding | Artefatos em docs/, devsecops/checklists | Não iniciado | A preencher |
| Fase 8 – Validação | Testes integrados e go-live | Suites em tests/, relatórios em evidences/ | Não iniciado | A preencher |

## Backlog Estruturado

### Fase 3 – Backend
- [x] Modelar domínios conforme 03_backend_implementacao.md para cada serviço (`backend/services/*/src/main/java/br/com/cartoriodigital/**/domain`).
- [x] Gerar projetos Quarkus com configurações de segurança, OpenAPI e observabilidade (`backend/services/*/src/main/resources/application.properties`, `backend/services/*/src/main/java/**/config/OpenApiConfig.java`).
- [x] Implementar endpoints do serviço Identity com dual-control e validações (`backend/services/identity/src/main/java/br/com/cartoriodigital/identity/infrastructure/rest/ApplicantResource.java`).
- [x] Implementar fluxo REST do Enrollment com evidências, revisão e aprovações (`backend/services/enrollment/src/main/java/br/com/cartoriodigital/enrollment/infrastructure/rest/EnrollmentResource.java`).
- [x] Implementar ciclo completo do Issuance Service com integrações a HSM/KMS (`backend/services/issuance/src/main/java/br/com/cartoriodigital/issuance/infrastructure/rest/CertificateOrderResource.java`).
- [ ] Implementar integrações com CloudHSM/KMS, incluindo failover e rotação de chaves.
- [ ] Criar pipelines de build/test com Testcontainers, PACT e cobertura mínima 80%.
- [ ] Documentar contratos em docs/architecture e publicar no portal operador.

### Fase 4 – Frontend
- [ ] Inicializar Turborepo com workspaces para cada portal.
- [ ] Implementar autenticação mTLS + WebAuthn + MFA conforme 04_frontend_ux.md.
- [ ] Construir fluxos críticos (aprovação emissão, onboarding RA, auditoria) com rastreabilidade.
- [ ] Configurar testes E2E (Playwright) e acessibilidade (axe-core) em CI.

### Fase 5 – Infraestrutura
- [ ] Completar módulos Terraform com parâmetros, outputs e testes Terratest.
- [ ] Definir pipelines de plan/apply com approvals manuais e storage remoto seguro.
- [ ] Criar Helm charts e manifests adicionais com políticas de segurança.
- [ ] Provisionar dashboards e alertas básicos em observability/.
- [ ] Registrar execuções terraform e evidências em evidences/iac/.

### Fase 6 – DevSecOps
- [ ] Popular policies/ e scripts/ com configurações das ferramentas (SonarQube, Checkov, Trivy, OWASP ZAP).
- [ ] Ajustar scripts de CI em scripts/ci para execução real e coleta de artefatos.
- [ ] Configurar gatilhos de coleta de evidências e assinatura digital dos relatórios.
- [ ] Integrar varreduras automáticas às pipelines definidas em .github/workflows.

### Fase 7 – Documentação e Onboarding
- [ ] Produzir runbooks operacionais e planos de treinamento alinhados a 07_documentacao_onboarding.md.
- [ ] Elaborar documentação de cerimônias de chave com checklists dual-control.
- [ ] Compilar matriz de conformidade DOC-ICP em docs/compliance.
- [ ] Criar pacotes de onboarding para RA, operadores e auditores.

### Fase 8 – Validação e Go-live
- [ ] Preparar suites de performance e caos em tests/performance.
- [ ] Automatizar testes de disponibilidade, DR e fallback.
- [ ] Registrar relatórios de go-live, post-mortem simulados e métricas em evidences/.
- [ ] Consolidar sign-off jurídico e de compliance com evidências.

## Checkpoints de Evidência

1. Cada execução de pipeline deve produzir artefatos em evidences/ com hash e assinatura digital.
2. Mudanças de Terraform devem ser registrados com planos aprovados e logs anexados.
3. Cerimônias de chave precisam de relatórios e fotos assinadas, seguindo 06_seguranca_conformidade.md.
4. Testes E2E e de performance devem exportar relatórios legíveis (JUnit, HTML, JSON) para auditoria ITI.

## Governança de Branches e Deploys

- main: linha base auditada, somente merge com aprovação dupla.
- release/*: branches temporárias para cutover e hotfixes.
- feature/*: desenvolvimento isolado com coleta de evidências local e integração contínua.

Deploys utilizam GitHub Actions com aprovações manuais (change management) e rolagem gradual em produção.

## Protocolo de Atualização

Após cada avanço:
1. Atualizar checkboxes acima com referência a commits, pipelines e evidências.
2. Registrar sumário da entrega e comandos executados.
3. Anexar provas em evidences/ e atualizar PLAN.md se necessário.

## Bloqueios e Mitigações

- Execução de `mvn -f backend/pom.xml test` falhou porque o ambiente não possui Maven instalado. Mitigação: adicionar Maven Wrapper ao repositório (`mvn -N io.takari:maven:wrapper`) ou disponibilizar Maven no ambiente antes da próxima rodada de testes.
- `docker compose up --build identity-service enrollment-service` não pôde ser executado devido à falta de permissão para acessar o daemon Docker no ambiente sandbox. Mitigação: solicitar habilitação do Docker ou executar os comandos em ambiente com acesso ao daemon.

## Prompt de Continuidade

Copie e utilize o prompt abaixo para retomar a execução deste plano de implementação:

Você é Codex, agente encarregado de implementar integralmente o plano técnico localizado em apendices/aws-kms-production/PLAN.md e acompanhar o progresso em apendices/aws-kms-production/projeto/IMPLEMENTATION_PLAN.md. Em cada iteração:

1. Revise IMPLEMENTATION_PLAN.md e identifique a próxima ação pendente, mantendo rastreabilidade com PLAN.md.
2. Execute as implementações necessárias (código, infraestrutura, pipelines, documentação, evidências) no diretório projeto/ seguindo os padrões definidos.
3. Valide resultados com testes automatizados ou manuais, registrando provas em evidences/.
4. Atualize IMPLEMENTATION_PLAN.md, arquivos alterados e referências a logs/artefatos garantindo conformidade ICP-Brasil.
5. Documente bloqueios ou dependências externas e proponha mitigação.

Objetivo: entregar a plataforma pronta para deploy, continuamente em conformidade operacional e regulatória.
