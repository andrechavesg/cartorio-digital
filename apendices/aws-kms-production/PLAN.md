# ITI-Compliant CA Platform Plan

# Phase 1 – Regulatory Baseline & Scope *(completed)*
1. **Catalogue ICP-Brasil controls** ✅  
   - DOC-ICP-01/02/03/04/05/08/10/15, resoluções complementares  
   - Identificar requisitos técnicos (HSM homologado, CPS, logs) e administrativos (segregação, DR)
2. **Definir personas e processos** ✅  
   - RA agents, operadores, administradores de segurança, auditores, subscritores  
   - Mapear fluxos (onboarding, emissão, revogação, auditoria)
3. **Planejar escopo funcional** ✅ *(documentado em `01_arquitetura_producao.md`, seção “Alinhamento com Fase 1”)*  
   - Emissão/renovação/revogação, OCSP, TSA, CRL publishing, evidências, reporting  
   - Critérios de aceitação e métricas (SLA/SLO, RPO/RTO, retention)

## Phase 2 – System Architecture & Infrastructure Design *(completed – ver `01_arquitetura_producao.md`, seção “Fase 2”)*
1. **Arquitetura lógica** ✅  
   - Microserviços: Identity Proofing, Enrollment, Issuance, Validation (OCSP/TSA), Audit, Publisher  
   - Fluxos de dados, contratos de API, tópicos de mensageria (SNS/SQS/Kafka)
2. **Arquitetura física/cloud** ✅  
   - AWS landing zone multi-AZ: VPC, sub-redes, security groups, WAF, bastion
   - Integração com AWS KMS, CloudHSM, Secrets Manager, S3, RDS/Aurora, OpenSearch
3. **Modelos de dados e storage** ✅  
   - Metadados de certificados, logs imutáveis, evidências, backups cross-region
4. **Diagramas e documentação** ✅  
   - Diagramas C4 (Context, Container, Component, Code)  
   - Mapas de rede, fluxos de identidade, key ceremony blueprint

## Phase 3 – Backend Implementation *(completed – ver `03_backend_implementacao.md`)*
1. **Fundação** ✅  
   - Monorepo ou multi-repo estruturado, padrões (DDD, Hexagonal, CQRS se necessário)
   - Escolha tecnológica (Java/Quarkus + Kotlin/Go) com guidelines de código, testes
2. **Serviço de Identidade & RA** ✅  
   - APIs de cadastro, validação presencial/remota, armazenamento de evidências  
   - Integração com serviços externos (gov.br, biometria)
3. **Serviço de Emissão** ✅  
   - CMP/SCEP/ACME/REST endpoints, integração com HSM/KMS  
   - Gestão de perfis de certificado (DOC-ICP-08), emissão, renovação
4. **Serviço de Revogação & CRL Publisher** ✅  
   - Workflow de revogação, gerador de CRL, publicação em repositórios
5. **Serviço de Validação (OCSP/TSA)** ✅  
   - OCSP assinado, carimbo do tempo securitizado
6. **Audit & Compliance Service** ✅  
   - Registro imutável (Append-only), exportação, relatórios de auditoria
7. **Testes automatizados** ✅  
   - Unit, integration, contract, load/security tests (Zap/Burp pipelines)

## Phase 4 – Frontend & UX *(completed – ver `04_frontend_ux.md`)*
1. **Portais seguros** ✅  
   - React/Next.js com TypeScript, design system, i18n (apenas EN/PT se necessário)  
   - Fluxos para RA, operadores, auditores, clientes
2. **Autenticação & Autorização** ✅  
   - mTLS, MFA (TOTP/WebAuthn), RBAC, ABAC conforme persona
3. **Fluxos críticos** ✅  
   - Aprovação de emissão, key ceremony dashboards, incident response, reporting
4. **Testes de UI e acessibilidade** ✅  
   - Cypress/Playwright, axe-core, testes de usabilidade

## Phase 5 – Infrastructure as Code & Environments *(completed – ver `05_iac_terraform.md`)*
1. **Terraform modules** ✅  
   - VPC, sub-redes, SG, AWS KMS/CloudHSM, RDS, S3, CloudFront, WAF, Secrets Manager, OpenSearch, CloudWatch, SNS  
   - Modularização e versionamento
2. **Kubernetes / ECS orchestration** ✅  
   - Helm charts, service mesh (App Mesh/Istio), sidecars para observabilidade
3. **Docker Compose para dev/local** ✅  
   - Simulação de serviços (localstack, mock HSM)
4. **Pipelines CI/CD** ✅  
   - GitHub Actions/CodePipeline, stages (lint/test/build/scan/deploy), promotion manual com approvals
5. **Observabilidade & DR** ✅  
   - Dashboards CloudWatch/Grafana, alarmes, scripts de backup/restore, testes de DR automatizados

## Phase 6 – DevSecOps & Compliance Automation *(completed – ver `06_seguranca_conformidade.md`)*
1. **Security tooling** ✅  
   - SAST (SonarQube), DAST (OWASP ZAP), dependency scanning (OWASP Dependency-Check, Snyk), IaC scanning (Checkov), container scanning (Trivy)
2. **Key ceremony & operations automation** ✅  
   - Scripts/documentos para cerimônia, logs assinados, controle dual  
   - Rotina de backup/restore com validação automática
3. **Evidence collection** ✅  
   - Export de CloudTrail, AWS Config snapshots, inventory, relatórios mensais  
   - Automação de checklist DOC-ICP (GitOps + pipelines)
4. **Runbooks de incidentes** ✅  
   - Segurança, disponibilidade, revogação emergencial, comprometed key

## Phase 7 – Documentation & Onboarding *(completed – ver `07_documentacao_onboarding.md`)*
1. **CPS/CP e políticas** ✅  
   - Templates RFC 3647, políticas de segurança, termos de uso, manual de operação
2. **Guias de deployment e operação** ✅  
   - Procedimentos diários, liberando atualizações, rotação de chaves, DR drills
3. **Treinamento e onboarding** ✅  
   - Materiais para RA/Operadores/Auditores, planos de treinamento, certificações
4. **Preparação para auditoria ITI** ✅  
   - Checklist final, pacotes de evidência, simulação de auditoria, comunicação com OAT

## Phase 8 – Go-live Preparation & Validation *(completed – ver `08_go_live_validacao.md`)*
1. **Testes integrados e ensaios** ✅  
   - Performance, alta disponibilidade, fallback (chaos engineering)
2. **Verificação de conformidade final** ✅  
   - Revisão completa com matriz de controles, sign-off jurídico e de compliance
3. **Plano de cutover** ✅  
   - Janela de ativação, comunicação, rollback
4. **Monitoramento pós-go-live** ✅  
   - Indicadores-chave (emissões, incidentes, SLA), plano de melhoria contínua

---

# To-dos
- [x] Fase 1 – requisitos: consolidar controles e escopo funcional *(entregue no Cap. 1)*
- [x] Fase 2 – arquitetura e infraestrutura lógica/física *(entregue no Cap. 1)*
- [x] Fase 3 – implementação backend (serviços CA, testes)
- [x] Fase 4 – frontends seguros para RA/operadores/auditores
- [x] Fase 5 – IaC, orquestração, pipelines e observabilidade
- [x] Fase 6 – automação DevSecOps e conformidade
- [x] Fase 7 – documentação, políticas e onboarding
- [x] Fase 8 – validação final e preparação go-live

---

## Prompt de Continuidade

Copie e cole o prompt abaixo em qualquer etapa do projeto para transformar este plano em realidade, retomando exatamente do ponto onde foi interrompido:

```
Você é Codex, agente responsável por implementar integralmente o plano `PLAN.md` do apêndice `aws-kms-production`, mantendo a solução 100% em conformidade com os requisitos de uma Autoridade Certificadora ICP-Brasil. Para cada execução:

1. Leia `PLAN.md` e identifique a próxima fase ou tarefa pendente; não repita itens já concluídos.
2. Consulte os capítulos associados (arquivos `0X_*.md`, runbooks, scripts, documentos) para entender os requisitos técnicos, normativos e de automação.
3. Planeje e execute as ações necessárias (código, infraestrutura, pipelines, documentação, evidências, testes) até entregar o resultado concreto descrito no plano.
4. Valide o que foi feito (testes automatizados/manual, geração de evidências, verificações de conformidade) e registre provas conforme instruções dos capítulos.
5. Atualize `PLAN.md`, os arquivos modificados e quaisquer dashboards/evidências para refletir o novo estado, incluindo referências aos artefatos criados.
6. Repita o ciclo até que não existam tarefas pendentes, documentando bloqueios ou dependências externas e sugerindo planos de mitigação quando necessário.

Diretrizes:
- Preserve e respeite automações, pipelines, controles de segurança, evidências e padrões já definidos.
- Priorize rastreabilidade: cada alteração deve apontar para artefatos ou logs que comprovem sua execução.
- Ao final de cada ciclo, relate claramente o que foi implementado e quais verificações foram realizadas.

Objetivo final: garantir que todo o plano seja executado de ponta a ponta e que a AC permaneça continuamente em conformidade operacional e regulatória.
```
