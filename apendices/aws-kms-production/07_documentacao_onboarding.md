# Capítulo 7 – Documentação, Políticas e Onboarding

Este capítulo consolida os artefatos regulatórios e operacionais necessários para colocar a Autoridade Certificadora do Cartório Digital em produção e prepará-la para auditorias do ITI. Ele se apoia nas definições dos capítulos anteriores e atende aos requisitos presentes em DOC-ICP-01/02/03/04/05/10, bem como boas práticas ISO/IEC 27001.

## 7.1 Conjunto de Documentos Oficiais

| Documento | Formato | Responsável | Frequência de revisão | Observações |
| --- | --- | --- | --- | --- |
| **CPS (Certification Practice Statement)** | Markdown → PDF assinado | Comitê de Segurança | Semestral ou ad-hoc | Template baseado em RFC 3647; inclui escopo de serviços, políticas de revogação e auditoria |
| **CP (Certificate Policy)** | Markdown → PDF assinado | Comitê de Governança | Anual | Define perfis DOC-ICP-05/08, algoritmos e extensões obrigatórias |
| **Policy & Procedure Manual** | Confluence export / PDF | Operações PKI | Trimestral | Checklists operacionais (key ceremony, incident response) |
| **Manual de Conformidade** | Excel (matrix.xlsx) + PDF | Compliance | Trimestral | Mapeia requisitos DOC-ICP às evidências automatizadas (Cap. 6) |
| **Termos & NDA** | DOCX/PDF | Jurídico | Conforme mudanças regulatórias | Termos de aceitação para RA, operadores e auditores |

Automação: repositório `docs/` contém fontes em Markdown/PlantUML; pipeline `docs-publish.yml` gera PDFs, assina com certificado institucional (via AWS Signer) e publica em S3 (`s3://cartorio-docs/official/`).

## 7.2 Publicação e Versionamento

- **GitOps para documentação**: branches `docs/*` obrigam revisão dupla (Compliance + Jurídico). Merge aciona pipeline que:
  1. Converte Markdown → PDF (Pandoc).
  2. Adiciona assinatura digital (script `scripts/docs/sign-pdf.sh`).
  3. Atualiza índice `docs/index.json` com metadados (versão, hash, data).
  4. Envia notificação SNS `docs-updates` para stakeholders (RA, auditor interno).
- **Tags semânticas**: releases `docs-vYYYY.MM` no Git marcam versões formais submetidas ao ITI.

## 7.3 Guias de Deployment e Operação

| Guia | Conteúdo | Local | Automação |
| --- | --- | --- | --- |
| **`runbooks/deployment.md`** | Passo-a-passo de deploy multi-stage, checklists de pré/pós, rollback | Repositório `runbooks/` | Validado em pipeline `deploy-checklist` (CI) |
| **`runbooks/operations.md`** | Rotinas diárias (monitoramento, backups, verificação de CRL/OCSP) | Confluence + Git | Reminders automáticos (AWS EventBridge Scheduler) |
| **`runbooks/drill.md`** | Simulações de DR e incidentes | Git | Integra com scripts de Cap. 5 (`scripts/dr/run-drill.sh`) |
| **`standard-operating-procedures/`** | SOPs por persona (RA, auditor, segurança) | SharePoint / Git | Assinatura eletrônica via DocuSign |

Todos os runbooks possuem seções “Evidências geradas” e “Controles relacionados” para facilitar auditorias.

## 7.4 Treinamento e Onboarding

### Personas e trilhas

| Persona | Módulos obrigatórios | Avaliação | Recertificação |
| --- | --- | --- | --- |
| **Agente RA** | PKI Fundamentals, Sistema RA Portal, Procedimentos Doc-ICP-05 | Prova teórica + simulação | 12 meses |
| **Operador de Emissão** | Fluxos EJBCA, Segurança Operacional, Runbooks de Incidente | Exam + exercício prático (sandbox) | 18 meses |
| **Administrador de Segurança** | Key Ceremony, Hardening AWS, GuardDuty | Lab supervisionado | 12 meses |
| **Auditor Interno/OAT** | Evidence Lake, Relatórios QuickSight, Compliance Matrix | Workshop + checklist | 24 meses |

### Automação de treinamento

- Plataforma LMS (TalentLMS) integrada ao AWS SSO para registro centralizado.
- Webhooks disparam Lambda `training-tracker` que atualiza DynamoDB (`training_records`).
- Grafana/QuickSight exibe status de certificações; alertas SNS → RH se recertificação estiver próxima (90 dias).
- Kits offline (PDF + vídeos) versionados em S3; hashes comparados em auditorias.

## 7.5 Preparação para Auditorias ITI

Fluxo anual automatizado:

```
EventBridge (cron) ──▶ Lambda audit-prep
                 │        ├─ Gera checklist (matrix.xlsx → PDF)
                 │        ├─ Agrega evidências (Cap. 6)
                 │        └─ Cria ticket Jira "Auditoria YYYY"
                 │
                 └─▶ SNS "audit-committee" (convoca reunião)
```

Checklist principal (`docs/auditoria/checklist-final.md`) cobre:

- Validação de cadeia de certificados (Root → Intermediate → end-entity).
- Verificação de logs imutáveis (CloudTrail digest, S3 Object Lock).
- Conferência de axxx (sig.), comparações com baseline Doc-ICP-05.
- Testes de contingência (DR drills, revogação emergencial).

Resultados são apresentados usando QuickSight “Audit Readiness Dashboard”, contendo:

- Score de conformidade por domínio (Gestão de Chaves, RA, Infra, Aplicação).
- Métricas de incidentes e tempos de resposta.
- Estado das políticas (CPS/CP) e respectivas evidências.

## 7.6 Gestão do Conhecimento

- **Base de Conhecimento**: Confluence espaço `Cartorio-PKI` sincronizado com Git (`docs/wiki/`). Job noturno compara alterações e cria PR se divergências existirem.
- **FAQ dinâmico**: chatbot interno (Amazon Lex) responde dúvidas comuns (renovação, evidências); treinado com documents indexados (Kendra).
- **Comunicações oficiais**: boletins mensais via AWS SES + Slack #cartorio-updates com resumo de mudanças, incidentes e métricas.
- **Liability tracking**: para cada mudança relevante, Lambda `change-archive` gera snapshot (config + documentação correlata) e armazena em S3 `change-history/`.

## 7.7 Próximos Passos

1. Integrar a matriz de conformidade com evidências LGPD e auditorias de proteção de dados.
2. Criar simulação de auditoria com OAT externo (tabletop exercise) usando Step Functions.
3. Expandir kit de onboarding com laboratórios hands-on em sandbox AWS controlado.
4. Automatizar tradução (PT ↔ EN) das políticas para parceiros internacionais.

---

→ O Capítulo 8 abordará o plano de Go-live, testes integrados e validações finais antes de operação oficial.
