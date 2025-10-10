# Capítulo 6 – DevSecOps & Automação de Conformidade

Este capítulo consolida a estratégia de segurança contínua e compliance para o Cartório Digital em produção. Os controles aqui descritos derivam dos normativos ICP-Brasil (DOC-ICP-01/02/03/04/05/10/15) e integram os demais capítulos: arquitetura (Cap. 1), backend (Cap. 3), frontends (Cap. 4) e infraestrutura (Cap. 5).

## 6.1 Pipeline de Segurança Automatizada

| Estágio | Ferramentas | Objetivo | Resultados esperados |
| --- | --- | --- | --- |
| **Code Quality & SAST** | SonarQube, Semgrep | Detectar vulnerabilidades e smells em tempo de commit | Cobertura SAST ≥ 80% dos repositórios, zero findings *Critical* antes de merge |
| **Dependency Scanning** | OWASP Dependency-Check, Snyk, Trivy | Identificar CVEs em bibliotecas, containers e IaC | Filtro automático com política de severidade (block se ≥ High) |
| **IaC Scanning** | Checkov, tfsec | Garantir baseline de segurança AWS/Terraform | Fails quando detectar recursos sem encryption/logging ou expostos |
| **DAST** | OWASP ZAP, Burp (automated) | Testar endpoints expostos (REST/GraphQL/Web UI) | Execução em PR críticos e nightly; relatório assinado (PDF) |
| **Container Security** | Trivy, Anchore | Scan de imagens ECR antes de deploy | Bloqueio no pipeline se imagem em produção tiver CVE ≥ High |
| **Runtime Protection** | AWS Inspector, GuardDuty, Falco (EKS) | Monitorar execução para comportamentos anômalos | Alertas SNS/PagerDuty com SLA < 15 min |

Workflow GitHub Actions (exemplo):

```yaml
name: secure-ci
on:
  pull_request:
    branches: [main]

jobs:
  sast:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - name: Run SonarQube
        run: ./gradlew sonar --info
      - name: Run Semgrep
        run: semgrep --config=auto

  dependency-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: OWASP Dependency-Check
        run: ./gradlew dependencyCheckAnalyze
      - name: Trivy Image Scan
        run: trivy image ghcr.io/zapsign/cartorio-backend:${{ github.sha }}
```

Relatórios são agregados no Security Hub e enviados semanalmente ao time de Compliance via SNS + e-mail assinado (Cap. 7).

## 6.2 Cerimônia de Chaves & Operações Seguras

### Orquestração automatizada

- **Planejamento**: Step Functions `key-ceremony-orchestrator` dispara checklists (create HSM partitions, gerar material, validação cruzada) com aprovação dupla (AWS Signer para ata digital).
- **Execução**: scripts Ansible/SSM Documents executam no bastion criado no Capítulo 5. Estados registrados no DynamoDB `key_ceremony_log` com hash encadeado.
- **Evidências**: cada etapa produz JSON assinado com KMS (`kms:Sign`). Cópias vão para S3 (`evidence/key-ceremony/AAAA/MM/DD`) com Object Lock.
- **Validação**: Lambda `key-ceremony-auditor` recalcula hashes e compara com ata assinada para detectar adulteração.

### Rotina

1. Aprovação do Comitê de Segurança (workflow ServiceNow).
2. Execução Step Functions → SSM → CloudHSM / AWS KMS.
3. Coleta de logs CloudTrail + gravação de vídeo (aplicativo Signal).
4. Geração de relatório final (`PDF + JSON`) assinado digitalmente e arquivado por 20 anos (DOC-ICP-02).

## 6.3 Coleta de Evidências & Auditoria Contínua

- **Evidence Lake**: bucket S3 `cartorio-prod-evidence` com camadas `raw`, `verified`, `reports`. Object Lock em modo compliance (10 anos). Todas as cargas possuem hash SHA-256 e assinatura digital.
- **Evidence Collector**: Lambda programada (cron) exporta:
  - CloudTrail & Config (últimas 24h)
  - Resultados de SAST/DAST (último ciclo)
  - Logs de RA (Cap. 3) e portais (Cap. 4)
  - Snapshots Terraform state (checksums)
- **Indexação**: dados indexados no OpenSearch `audit-evidence` com retenção 13 meses (consulta rápida) + replicação para S3 Glacier Deep Archive.
- **Painéis de conformidade**: QuickSight dashboards exibem maturidade de controles e *compliance score* (≥ 95%). Relatórios alimentam preparativos para auditoria ITI (Cap. 7).

## 6.4 Runbooks de Incidentes

| Tipo | Runbook | Automação | Métricas |
| --- | --- | --- | --- |
| Segurança | `rb-security-compromised-key.md` | Step Functions executa revogação emergencial + rotação KMS | MTTR < 30 min |
| Disponibilidade | `rb-availability-ejbca.md` | Script SSM reinicia série ECS + failover Aurora | MTTR < 15 min |
| Compliance | `rb-compliance-evidence-gap.md` | Lambda cria ticket + reprocessa evidence collector | SLA resposta 4h |
| Incident Response | `rb-incident-communication.md` | Integração PagerDuty + Slack + e-mail templated | Comunicação ≤ 15 min |

Runbooks utilizam Playbooks do AWS Systems Manager Automation com aprovação dual para ações destrutivas. Logs e resultados são anexados ao Evidence Lake automaticamente.

## 6.5 Gestão de Vulnerabilidades e Patching

- **AWS Inspector** monitora EC2/ECS/ECR; findings replicados para Security Hub.
- Patching semestral obrigatório: SSM Patch Manager aplica patches nos bastions e hosts gerenciados (Cap. 5). Patches emergenciais (< 48h) quando severity Critical.
- SLA de tratamento:
  - Critical: 24h
  - High: 72h
  - Medium: 30 dias
  - Low: backlog

Automação via Jira Service Management: findings geram tickets automaticamente com severidade e tags de compliance.

## 6.6 Política de Segredos e Configurações

- Secrets Manager com rotação automatizada (90 dias) e verificação via Lambda (`secrets-rotation-validator`).
- Parameter Store (SecureString) para configs menos sensíveis, versionamento e alarmes se alterados sem change request.
- HashiCorp Vault opcional on-prem replicada com secrets críticos.
- Detector `aws-config` custom verifica se segredos foram acessados fora de janela aprovada (dispara SNS + runbook).

## 6.7 Matriz de Conformidade Automática

Mapa de controles (resumo):

| Normativo | Controle automatizado | Evidência |
| --- | --- | --- |
| DOC-ICP-03 | Least privilege, segredos rotacionados | IAM Access Analyzer + relatórios Secrets Manager |
| DOC-ICP-04 | Trilhas imutáveis, runbooks assinados | S3 Object Lock, Audit Collector |
| DOC-ICP-05 | Fluxos RA com dupla custódia | Logs RA (Cap. 3) + assinaturas WebAuthn |
| DOC-ICP-10 | Hardening + patching | Inspector, Patch Manager |
| DOC-ICP-15 | TSA/OCSP monitorados | Métricas CloudWatch + relatórios TSA |

A matriz completa vive em `compliance/matrix.xlsx` e é atualizada automaticamente por pipeline (`scripts/compliance/generate-matrix.py`) após cada deploy.

## 6.8 Próximos Passos

- Adicionar suporte a **OpenSSF Scorecard** em todos os repositórios.
- Implementar **Continuous Controls Monitoring** (CCM) com Evidently para detectar desvios em tempo real.
- Pilotar **confidential computing** (Nitro Enclaves) para assinaturas sensíveis.
- Integrar com **LGPD Data Mapping** para rastrear dados pessoais em microserviços.

---

→ Os capítulos seguintes continuam a jornada com documentação e onboarding (Cap. 7) e preparação para Go-live (Cap. 8).
