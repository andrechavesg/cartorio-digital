# Apêndice – Getting Production Ready with AWS KMS

Este apêndice apresenta o roteiro completo para levar a Autoridade Certificadora (AC) do **Cartório Digital** à produção na AWS. O material está organizado em fases e capítulos que cobrem arquitetura, serviços, infraestrutura, DevSecOps, documentação e validação final, assegurando aderência contínua à regulamentação do ITI/ICP-Brasil.

## Como navegar

| Fase | Objetivo | Capítulos principais |
| --- | --- | --- |
| **Fase 1 & 2** | Entender requisitos ICP-Brasil e definir arquitetura lógica/física | [01_arquitetura_producao.md](01_arquitetura_producao.md), [09_requisitos_iti.md](09_requisitos_iti.md) |
| **Fase 3** | Implementar serviços backend e integrações com HSM/KMS | [03_backend_implementacao.md](03_backend_implementacao.md) |
| **Fase 4** | Projetar portais seguros, autenticação e UX acessível | [04_frontend_ux.md](04_frontend_ux.md) |
| **Fase 5** | Provisionar infraestrutura com Terraform, observabilidade e DR | [05_iac_terraform.md](05_iac_terraform.md) |
| **Fase 6** | Automatizar DevSecOps, compliance e runbooks de incidentes | [06_seguranca_conformidade.md](06_seguranca_conformidade.md) |
| **Fase 7** | Produzir documentação oficial, treinamentos e preparação para auditorias | [07_documentacao_onboarding.md](07_documentacao_onboarding.md) |
| **Fase 8** | Concluir testes integrados, plano de cutover e monitoramento pós go-live | [08_go_live_validacao.md](08_go_live_validacao.md) |

> Dica: consulte [PLAN.md](PLAN.md) para acompanhar o progresso das fases e ações pendentes. Cada capítulo indica os próximos passos e referências cruzadas.

## Estrutura do repositório

- `PLAN.md`: roteiro mestre com todas as fases e um prompt reutilizável para continuar a implementação.
- `01_…` a `08_…`: capítulos temáticos com instruções detalhadas, scripts de automação e checklists.
- `09_requisitos_iti.md`: consolidação dos normativos ICP-Brasil e mapeamento de controles técnicos.
- `docs/`, `runbooks/`, `scripts/` (referenciados nos capítulos): diretórios de apoio para geração de documentos, pipelines e automações.

## Fluxo recomendado

1. **Leia os requisitos (Cap. 9)** para entender controles obrigatórios do ITI.
2. **Projete arquitetura (Cap. 1)** e avance para implementação backend/front (Caps. 3 & 4).
3. **Provisionamento (Cap. 5)** garante ambientes consistentes com Terraform, observabilidade e DR.
4. **Camada de segurança (Cap. 6)** torna DevSecOps parte da operação diária.
5. **Documente e prepare treinamentos (Cap. 7)** garantindo prontidão para auditorias.
6. **Execute testes finais e cutover (Cap. 8)**, monitorando indicadores críticos após o go-live.

Cada capítulo inclui:

- **Automação**: scripts (`scripts/`) e pipelines (GitHub Actions, Step Functions) para reduzir esforço manual.
- **Evidências**: instruções para gerar artefatos assinados e armazená-los com Object Lock.
- **Runbooks**: procedimentos operacionais com foco em segurança, disponibilidade e conformidade.
- **Próximos passos**: backlog imediato para evolução contínua.

## Mantendo conformidade contínua

Os capítulos 6, 7 e 8 detalham mecanismos para:

- Automatizar varreduras SAST/DAST, scans de dependência e monitoramento em runtime.
- Gerar evidências e relatórios para auditorias do ITI (checklists, matriz de conformidade, dashboards).
- Executar treinamentos recorrentes e registrar recertificações por persona.
- Planejar cutover seguro e acompanhar métricas operacionais pós go-live.

Reutilize o prompt ao final de `PLAN.md` sempre que precisar retomar o trabalho: ele garante que a implementação continue do ponto em que parou e que os controles permaneçam em conformidade com uma Autoridade Certificadora de forma contínua.
