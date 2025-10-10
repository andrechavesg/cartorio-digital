# DevSecOps e Compliance Contínua

Diretório para pipelines, políticas e scripts que garantem o atendimento aos controles ICP-Brasil e práticas de DevSecOps.

## Estrutura

- policies – Templates de políticas YAML/JSON para ferramentas como Checkov, Trivy e SonarQube.
- checklists – Checklists automatizados (DOC-ICP-05, DOC-ICP-08, DOC-ICP-15) executados via pipelines.
- scripts – Automação de coleta de evidências, geração de relatórios e integrações com SIEM.

## Pipelines

Workflows GitHub Actions residem em .github/workflows e cobrem:

1. Build e testes dos microserviços backend e frontend.
2. Scans SAST, DAST, IaC e containers.
3. Provisionamento automatizado com validações de segurança e aprovação manual para produção.
4. Coleta de evidências e publicação em evidences/.

Para detalhes consulte 06_seguranca_conformidade.md e 08_go_live_validacao.md.
