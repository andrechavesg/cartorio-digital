# Observabilidade e Alertas

## Introdução

Um pico de acessos quase derrubou a API do cartório. Graças a dashboards em Grafana, logs estruturados e alertas no PagerDuty, a equipe escalou recursos e resolveu o problema antes que os cidadãos percebessem. O episódio consolidou a importância da observabilidade.

## Conceitos Fundamentais

- **Logs, métricas, traces:** pilares da observabilidade.
- **Alertas acionáveis:** notificações com contexto para resposta rápida.
- **SLOs e SLAs:** metas mensuráveis de disponibilidade e desempenho.
- **Integração com segurança:** correlaciona eventos para detectar incidentes.

## Práticas Reais

1. Configure Prometheus/Grafana ou serviços gerenciados para monitorar pipelines e aplicações.
2. Crie alertas para expiração de certificados, falhas de deploy e aumento de latência.
3. Defina SLOs alinhados às expectativas dos cidadãos e acompanhe os resultados.
4. Registre cada incidente em post-mortems com ações preventivas.

## Próximos passos

Com observabilidade em ação, vamos encarar o desafio final do módulo: construir uma entrega completa de ponta a ponta. Na Introdução do próximo capítulo uniremos todos os elementos em uma jornada prática.
