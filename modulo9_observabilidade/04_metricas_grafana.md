# Métricas e Dashboards no Grafana

## Introdução

O cartório montou um painel em Grafana que mostrava a saúde da PKI: certificados próximos do vencimento, latência do OCSP e uso das APIs. Durante um comitê, a diretoria conseguiu tomar decisões em minutos, graças à visualização clara dos dados.

## Conceitos Fundamentais

- **Datasources:** Prometheus, Loki ou Elasticsearch alimentando o Grafana.
- **Painéis temáticos:** agrupam métricas por contexto (PKI, TLS, automação).
- **Alertas integrados:** Grafana envia notificações baseadas em thresholds definidos.
- **Compartilhamento:** dashboards acessíveis a equipes técnicas e executivas.

## Práticas Reais

1. Conecte o Grafana aos dados coletados (logs, métricas, status de certificados).
2. Crie painéis específicos para expiração, revogação e desempenho.
3. Configure alertas visuais e notificações via e-mail ou chat corporativo.
4. Revise os painéis periodicamente para garantir que continuem relevantes.

## Próximos passos

Com dashboards poderosos, enfrentaremos o desafio operacional final: coordenar respostas a incidentes e manutenções programadas. Na Introdução do próximo capítulo veremos como manter o cartório sempre operacional.
