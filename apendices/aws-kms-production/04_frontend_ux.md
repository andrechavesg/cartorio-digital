# Capítulo 4 – Frontends Seguros & Experiências de Usuário

Este capítulo descreve a camada de experiência do usuário da plataforma Cartório Digital, cobrindo os portais web, controles de autenticação/autorização e requisitos de acessibilidade em conformidade com a ICP-Brasil. O desenho está alinhado às personas definidas na Fase 1 e integra os serviços backend apresentados no [Capítulo 3](03_backend_implementacao.md).

## 4.1 Stack tecnológica e princípios

- **Framework**: Next.js 14 (App Router) com TypeScript, estilização via Tailwind CSS, componentes acessíveis (Radix UI) e Storybook para documentação viva.
- **Design System**: biblioteca `CartorioDS` com tokens (cores, tipografia, espaçamento) versionados no Figma → exportados via Style Dictionary.
- **Internacionalização**: i18n nativo (pt-BR, en-US) com namespaces por domínio (`ra`, `auditor`, `admin`).
- **Distribuição**: hospedagem em AWS Amplify Hosting (dev/stage) e CloudFront + S3 (prod), com build pipeline GitHub Actions → CodeBuild → CodePipeline.
- **Observabilidade**: captura de métricas de UX (LCP, FID, CLS) via AWS CloudWatch RUM e logs estruturados em CloudWatch Logs/Datadog.
- **Segurança por padrão**: CSP restritiva, Strict-Transport-Security, SameSite cookies, uso de Web Crypto API para operações locais (assinaturas).
- **Compliance**: aderência ao eMAG (Modelo de Acessibilidade do Governo Federal), WCAG 2.1 AA, e requisitos ICP-Brasil para fluxos de RA (registro de evidências, dupla custódia).

## 4.2 Portais e personas

| Portal | Persona(s) | Principais capacidades | Links backend |
| --- | --- | --- | --- |
| **RA Portal** | Agentes RA, Operadores Master | Onboarding presencial/remoto, gerenciamento de evidências, aprovação dual | Identity Proofing API, Enrollment Service, Evidence Vault |
| **Operator Console** | Administradores de AC, operadores técnicos | Gestão de perfis de certificados, emissão manual, revogação, dashboards operacionais | Issuance API, Revocation Workflow, Metrics Service |
| **Auditor Workspace** | Auditores internos/OAT | Consulta de trilhas, geração de relatórios, export assinados | Audit Collector, Evidence Exporter |
| **Cliente/Empresas** | Subscritores, integradores | Requisição/renovação via portal, APIs self-service, estado de certificados | Issuance API (REST/ACME), Validation Service |
| **Portal de Incidentes** | Administradores de segurança | Runbooks, comunicação com partes interessadas, gatilho de revogação emergencial | Revocation Workflow, SNS, Incident runbooks |

Todos os portais compartilham authenticação base via AWS Cognito federado ao AWS SSO (SAML/OIDC) com MFA obrigatório, exceto o portal público de clientes que oferece login com certificado cliente (mTLS) e Gov.br (OAuth 2.0) opcional.

## 4.3 Arquitetura de autenticação e autorização

```
┌───────────────┐     1. Redirect       ┌──────────────────┐
│ Frontend (RA) │ ─────────────────────▶│ AWS SSO (SAML2)  │
└──────▲────────┘                       └───────┬──────────┘
       │ 4. Token (OIDC)                         │2. MFA
       │                                         ▼
       │                                ┌──────────────────┐
       │                                │ Cognito User Pool │
       │                                └──────┬───────────┘
       │   5. Access Token / ID Token          │3. Assertion + claims
       ▼                                       ▼
┌────────────────────┐       6. exchange   ┌──────────────────┐
│ BFF (Next.js API   │────────────────────▶│ Backend Services │
│ routes / Edge      │<────────────────────┤ (mTLS + RBAC)    │
│ Middleware)        │    7. mTLS + JWT    └──────────────────┘
└────────────────────┘
```

- **Backends for Frontend (BFF)**: rotas API do Next.js no modo Server Actions convertem tokens Cognito em JWT assinado internamente (KMS) contendo `persona`, `role`, `scopes`.
- **Autorização fina**: RBAC + ABAC com base em atributos (`agencyId`, `region`, `certProfile`). Policies definidas no AWS Verified Permissions (Cedar) com cache de autorização em DynamoDB.
- **Sessões sensíveis**: RA Portal exige reautenticação para ações críticas (dupla custódia). Assinaturas digitais dos agentes são capturadas via WebAuthn + KMS Sign.

## 4.4 Fluxos críticos

### 4.4.1 Onboarding de subscritor

```
1. Subscritor acessa portal → login com Gov.br + MFA.
2. Preenche dados (CSR opcional) → validações client-side (CPF, CNPJ, OIDs).
3. Upload de documentos → criptografia client-side (Web Crypto AES-GCM).
4. Frontend envia payload ao BFF → Evidence Vault via Signed URL.
5. RA recebe tarefa → telas de evidência, video-call (WebRTC), checklist DOC-ICP-05.
6. Aprovação dual → UI mostra co-assinatura digital, registra audit trail (hash exibido na tela).
7. Emissão automática disparada → status atualizado em tempo real (WebSockets via API Gateway).
```

### 4.4.2 Revogação emergencial

```
1. Operador abre incidente → seleciona certificado/CA.
2. Frontend valida permissão (necessita role `SECURITY_ADMIN` + WebAuthn token).
3. Solicitação enviada ao Revocation Workflow → UI mostra contagem regressiva.
4. Segundo aprovador recebe notificação (SNS → SMS/Email) → painel de aprovação.
5. Após dupla aprovação, UI apresenta hash da CRL e link verificação.
6. Logs e justificativas assinadas ficam disponíveis para auditor na mesma interface.
```

### 4.4.3 Auditoria sob demanda

```
1. Auditor filtra por período/evento → frontend chama API GraphQL `/audit/reports`.
2. Resultado paginado com pré-visualização (JSON/CSV) e resumo (kpis, gráficos D3).
3. Botão “Exportar com assinatura” dispara job assíncrono → notifica via SNS quando pronto.
4. Download oferece pacote ZIP contendo relatório, assinatura detached e hash.
```

## 4.5 Acessibilidade e usabilidade

- **Acessibilidade**:
  - Contraste AA validado com axe-core em CI.
  - Navegação 100% via teclado, foco visível, skip links.
  - Suporte a leitores de tela (ARIA labels) e legendas para vídeo-chamadas (AWS Transcribe).
  - Testes com usuários finais (personas) registrados em relatórios para auditoria DOC-ICP-04.
- **Usabilidade**:
  - Protótipos validados junto a RA/Auditores (Design Sprints).
  - Feedback em tempo real (toasts, indicadores de progresso).
  - Modo offline (PWA) para coleta de evidências em áreas com baixa conectividade (sincronização posterior).
  - Guia contextual e tooltips com referências normativas (ex.: DOC-ICP-05 seção 4.5).

## 4.6 Telemetria e UX analytics

- CloudWatch RUM + Datadog RUM recolhem métricas Core Web Vitals e erros JS.
- Eventos de UX relevantes (tempo aprovação RA, taxa de abandono) enviados para Kinesis Firehose → S3 → QuickSight dashboards (correlacionados com métricas backend).
- Heatmaps e gravações (FullStory/Hotjar) habilitados apenas em ambientes de homologação com anonimização (LGPD compliance).
- Alertas automáticos: se `RA_APPROVAL_TIME_P95 > 20min` → SNS notifica equipe de operação; se `OCSP_LATENCY_UI > 300ms` → abre ticket Jira.

## 4.7 Testes e qualidade de frontends

| Nível | Ferramentas | Objetivo | Frequência |
| --- | --- | --- | --- |
| Unit | Jest, React Testing Library | Componentes isolados | Ao commitar |
| Integrado (BFF) | Cypress Component Testing | Fluxos críticos com backend mockado | Pull Request |
| E2E | Playwright (multi-browser) | Percurso completo (onboarding, revogação, auditoria) | Nightly + pré-release |
| Acessibilidade | axe-core, pa11y | Checagem automatizada | CI + mensal manual |
| Segurança | OWASP ZAP (DAST), SonarLint | Scans frontend e BFF | Mensal |
| Performance | Lighthouse CI, WebPageTest API | Garantir CWV (LCP<2.5s) | Weekly |

Resultados são publicados no Grafana OnCall; falhas críticas bloqueiam deploy via branch protection. Feature flags só são liberadas para produção após `Green` em testes E2E e aprovação manual de Product Security.

## 4.8 Próximos passos

- Lançar **Design Tokens** automatizados para consumo em apps mobile futuros.
- Implementar **Self-Service Playground** para desenvolvedores externos (API docs interativos com mock server).
- Adotar **Signed Exchanges** (SXG) para portais públicos, reduzindo latência.
- Integrar **Self-Sovereign Identity** (OpenID4VCI) para prova de credenciais descentralizadas.

---

→ Próximo capítulo retoma a infraestrutura de CAs gerenciadas: [AWS Private CA](03_aws_private_ca.md) complementa os fluxos descritos aqui com o provisionamento das chaves e certificados.
