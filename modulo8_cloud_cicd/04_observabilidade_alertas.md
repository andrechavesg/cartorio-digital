# 04 · Observabilidade e alertas em pipelines cloud

Com a aplicação do cartório digital processando milhares de transações por dia, enxergar o que acontece sob o capô é vital. Este capítulo mostra como nutrimos uma cultura observável, onde métricas, logs e alertas transformam dados em decisões rápidas.

## Contexto

Certificados expiram, pipelines podem falhar e recursos em nuvem sofrem oscilações. Sem monitoramento proativo, corremos o risco de comprometer a confiança dos cidadãos. Precisamos instrumentar cada etapa para detectar incidentes antes que eles impactem o serviço.

## Estratégia de observabilidade

1. **Coletar métricas do pipeline**: Exportamos dados do GitHub Actions para o Prometheus do cartório.
2. **Monitorar certificados**: Reutilizamos scripts do `modulo3_tls_mtls` para verificar validade e publicar métricas.
3. **Alertas acionáveis**: Definimos gatilhos no Grafana que notificam o time via Slack e PagerDuty.

## Exemplo guiado com Prometheus e Grafana

Primeiro, configuramos um *exporter* que captura resultados dos jobs:

```yaml
# .github/workflows/metrics-export.yml
name: Exportar métricas do pipeline

on:
  workflow_run:
    workflows: ["Cartorio Delivery Pipeline"]
    types:
      - completed

jobs:
  publicar-metricas:
    runs-on: ubuntu-latest
    steps:
      - name: Enviar métricas para Prometheus Pushgateway
        run: |
          echo "pipeline_status{workflow=\"delivery\"} ${{ github.event.workflow_run.conclusion == 'success' && 1 || 0 }}" \
            | curl --data-binary @- https://observabilidade.cartorio.digital/metrics
```

Em seguida, criamos uma regra de alerta no Grafana que dispara quando o pipeline falha três vezes consecutivas:

```hcl
# grafana/provisioning/alerting/cartorio-pipeline.json
{
  "title": "Falhas consecutivas no pipeline",
  "condition": "C",
  "data": [
    {
      "refId": "A",
      "queryType": "timeSeriesQuery",
      "relativeTimeRange": {"from": 900, "to": 0},
      "datasourceUid": "prometheus",
      "model": {
        "expr": "sum_over_time(pipeline_status{workflow=\"delivery\"} == 0 [15m]) >= 3"
      }
    }
  ],
  "noDataState": "OK",
  "execErrState": "Alerting",
  "for": "0m",
  "annotations": {
    "summary": "Pipeline do cartório falhou 3 vezes em 15 minutos"
  },
  "contactPoints": ["slack-cartorio", "pagerduty-certidoes"]
}
```

## Ligação com alertas de certificados

A mesma pilha de observabilidade acompanha os prazos de certificados definidos nos módulos anteriores. Um job adicional lê o `terraform state` para identificar recursos ACM e publica métricas de expiração, reforçando a proteção destacada em `modulo6_kms_hsm`.

Com dados confiáveis e alertas acionáveis, o cartório digital preserva sua reputação e mantém a operação sob controle, mesmo diante de picos de demanda.
