# Governança e Operação Contínua

## Problema vivido pelo cartório digital

Após o go-live, o cartório digital percebe que a confiabilidade operacional não acompanha as exigências legais: faltam evidências consolidadas, os alertas são ruidosos e a auditoria tem dificuldade em seguir o trilho de decisões.

## Conexão inspiradora com os módulos anteriores

Os requisitos regulatórios e de auditoria do **Módulo 5** e a observabilidade estruturada no **Módulo 9** mostraram que governança não é um departamento, mas sim uma prática contínua. Some os controles de identidade do **Módulo 3** e as políticas de certificação do **Módulo 2** para construir um painel que fale a linguagem do auditor e da operação.

## Ferramentas e comandos como solução

- **Exemplo prático de observabilidade inspiradora** – diante de um incidente de latência percebido pelos cidadãos, use a query consolidada e a importação do dashboard para mostrar que o ecossistema possui visibilidade ponta a ponta.
  ```bash
  tempo query '{job="cartorio-api"}' --time-range=1h
  grafana dashboards import dashboards/cartorio-operacao.json
  ```
- **Fluxo de auditoria de assinaturas e selos** – quando a corregedoria requisitar provas das assinaturas emitidas, demonstre como inspecionar rapidamente os certificados associados às certidões digitais.
  ```bash
  step certificate inspect --format json artefatos/assinaturas/certidão-*.pem | jq '.extensions'
  ```
- **Política de rotação de segredos e evidência de execução** – ao revisar o runbook de segurança, prove que a rotação de segredos é automatizada e auditável conforme os padrões definidos nos módulos anteriores.
  ```bash
  vault write -force secret/data/cartorio/api
  vault audit enable file file_path=/var/log/vault/auditoria.log
  ```
- **Gestão de incidentes com livro-razão imutável** – para manter a memória operacional intacta, registre cada evento crítico no ledger e gere o balanço para análise periódica.
  ```bash
  ledger-cli --file registros/incidentes.ledger balanco incidentes
  ```
