# Capítulo 8 – Validação Final & Preparação para Go-live

Este capítulo cobre os testes integrados, verificações de conformidade e plano de ativação contínua da Autoridade Certificadora (AC) Cartório Digital. O objetivo é assegurar que todos os controles técnicos e regulatórios estejam atendidos antes, durante e após o go-live, mantendo conformidade com o ITI em regime permanente.

## 8.1 Testes Integrados e Ensaios

### 8.1.1 Testes funcionais end-to-end

| Cenário | Descrição | Ferramentas | Evidência |
| --- | --- | --- | --- |
| Emissão completa (RA → certificado) | Onboarding, aprovação dual, emissão, entrega do certificado | Playwright E2E + Postman | Vídeo + logs auditados (S3 `evidence/tests/e2e/`) |
| Revogação emergencial | Gatilho manual, aprovação dupla, atualização CRL/OCSP | Step Functions + Cypress | Relatório JSON assinado |
| Carimbo do tempo (TSA) | Geração, validação externa | k6 (latência) + script OpenSSL | Relatório k6 + CRL/OCSP check |
| Failover ACME | Renovação automática de certificado TLS | k6 + Lambda integration tests | CloudWatch logs + métricas |

### 8.1.2 Ensaios de resiliência

- **Chaos engineering**: AWS Fault Injection Simulator interrompe tarefas ECS, failover RDS, indisponibiliza AZ. Métricas monitoradas (SLA, RTO/RPO).
- **Backup/restore**: execução do runbook `runbooks/drill.md` restaurando aurora + S3. Evidência assinada e comparada com baseline.
- **Security drills**: simulação de comprometimento de chave (Cap. 6) com validação de cutover para nova CA intermediária.

### 8.1.3 Performance & escalabilidade

- k6/Gatling nos endpoints públicos (RA Portal, ACME, OCSP, TSA) com cargas pico (500 req/s). Critérios: p99 < 200 ms, erro < 0.5%.
- Teste de carga do Evidence Lake (Athena + QuickSight) para relatórios simultâneos.

Relatórios consolidados ficam em `s3://cartorio-prod-reports/go-live-tests/AAAA-MM/`.

## 8.2 Verificação de Conformidade Final

Checklist “go/no-go” (automatizado via Lambda `go-live-validator`):

1. **Documentação**: CPS/CP e políticas assinadas e publicadas (Cap. 7).
2. **Infraestrutura**: Terraform state aplicado (Cap. 5), drift check (`terraform plan -detailed-exitcode` = 0).
3. **Segurança**: pipelines SAST/DAST (Cap. 6) sem findings críticos; GuardDuty/Inspector sem alerts abertos.
4. **RA & Operações**: treinamentos válidos (Cap. 7), auditorias internas concluídas.
5. **Planos de DR**: última simulação < 90 dias (Cap. 5).
6. **Matriz de conformidade**: `compliance/matrix.xlsx` com status ≥ 95% e sem gaps críticos.

O Lambda gera relatório `go-live-summary.json`, assinado digitalmente e anexado ao Jira change record.

## 8.3 Plano de Cutover

### 8.3.1 Sequência de ativação

1. **Congelamento**: janela acordada com stakeholders; pipeline CI em modo “review only”.
2. **Pré-check**: execução do `go-live-validator`; confirmação de readiness.
3. **Ativação**:
   - Promover DNS (Route 53) para ambientes `prod`.
   - Habilitar ALB listener público e certificados ACM.
   - Iniciar replicação de CRL/OCSP para S3/CloudFront.
   - Habilitar integrações externas (gov.br, Receita Federal).
4. **Smoke tests**: script `scripts/post-cutover-smoke.sh` verifica endpoints e métricas.
5. **Comunicação**: Slack + e-mail + publicação no portal do cartório com note oficial.

### 8.3.2 Rollback

- Guardar snapshots/Terraform plan pré-cutover.
- Runbook `cutover-rollback.md` (aprovado dual) que restaura DNS, stop ALB e difere integrações.
- Evidências e justificativas registradas em S3 `cutover-history/`.

## 8.4 Monitoramento Pós-Go-live

| Indicador | Meta | Ferramenta | Ação quando em alerta |
| --- | --- | --- | --- |
| Emissões/dia (por CA) | ≥ baseline planejado | QuickSight + CloudWatch | Investigação RA se queda > 20% |
| OCSP/TSA latência p95 | < 150 ms | CloudWatch Synthetics | Escalonar para operações |
| Incidentes de segurança | 0 críticos, 0 high | GuardDuty/Security Hub | Runbooks de incidentes (Cap. 6) |
| SLA disponibilidade | ≥ 99.9% | CloudWatch/Datadog | Acionamento de DR se brecha > 30 min |
| Compliance score | ≥ 95% | Compliance matrix | Plano de ação automático |

Relatórios semanais enviados ao comitê de governança; dashboards abertos a auditores sob credenciais read-only.

## 8.5 Continuidade e Melhoria Contínua

- **Retro pós-go-live**: reunião com RA, segurança, DevOps, compliance para avaliar lições aprendidas; backlog atualizado no Jira.
- **Roadmap evolutivo**: integrar PQC (algoritmos pós-quânticos), suportar certificação de dispositivos IoT e expandir para GCP se necessário.
- **Auditoria contínua**: manter pipelines de evidências (Cap. 6) e treinamentos recorrentes (Cap. 7). Lambda `continuous-compliance` verifica semanalmente itens pendentes e abre tickets automáticos.

## 8.6 Próximos Passos

1. Formalizar change record de go-live com ata assinada e anexos (políticas, testes, relatórios).
2. Agendar auditoria independente de conformidade (pré-ITI) para reforçar readiness.
3. Monitorar métricas iniciais nas primeiras 72 horas e ajustar thresholds conforme comportamento real.
4. Comunicar resultados ao ITI, parceiros e clientes-chave, disponibilizando transparência total.

---

Go-live concluído significa início do ciclo de operação contínua. As rotinas descritas garantem que a AC permaneça em conformidade permanente, permitindo evolução incremental sem perder rastreabilidade ou segurança.
