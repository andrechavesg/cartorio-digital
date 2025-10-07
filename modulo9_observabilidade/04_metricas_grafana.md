# Métricas no Grafana: Tornando a Saúde do Cartório Visível

Quando o time percebeu que a expiração de um certificado estava próxima e ninguém havia sido alertado, entendemos que métricas isoladas não bastam. Precisávamos contar a história do cartório digital por painéis claros e inspiradores.

## Conceito: Observabilidade Quantitativa
Antes de construir qualquer dashboard, revisitamos o conceito de **observabilidade quantitativa**: transformar eventos invisíveis em indicadores contínuos. A coleta iniciada no módulo 8 com Prometheus precisa agora ser traduzida em visualizações que conectem segurança e operações.

## Integração com o Projeto
- **Fonte de dados:** Prometheus já configurado para coletar métricas de certificados e OCSP, com scraping definido em `modulo8_cloud_cicd/prometheus/prometheus.yml`.
- **Indicadores-chave:** tempo até expiração (`cert_expiry_seconds`), latência de OCSP (`ocsp_latency_seconds`) e contagem de alertas (`cartorio_tls_alerts_total`).
- **Histórico versionado:** o JSON de painel fica armazenado em `modulo9_observabilidade/dashboards/` para ser aplicado automaticamente pelos pipelines de entrega contínua.

## Exemplo Guiado: Construindo o Painel no Grafana
Com o conceito em mente, abrimos o Grafana e importamos um JSON de dashboard preparado pela equipe. Só depois de entender que cada gráfico representa um ponto da jornada do certificado executamos o comando de provisionamento.

```bash
cat <<'DASH' > /etc/grafana/provisioning/dashboards/cartorio-observabilidade.json
{
  "title": "Cartório Digital – Observabilidade PKI",
  "panels": [
    {"type": "gauge", "title": "Dias até expiração", "targets": [{"expr": "(cert_expiry_seconds/86400)"}]},
    {"type": "graph", "title": "Latência do OCSP", "targets": [{"expr": "ocsp_latency_seconds"}]},
    {"type": "stat", "title": "Alertas ativos", "targets": [{"expr": "cartorio_tls_alerts_total"}]}
  ]
}
DASH
systemctl restart grafana-server
```

- **Mensagem transmitida pelo painel:** cada colaborador vê, em tempo real, quanto tempo temos até a expiração e se existe algum alerta pendente. A barra de gauge alimenta a SLO semanal definida pela diretoria.
- **Impacto cultural:** dashboards tornam-se rituais diários, reforçando a missão de entregar atos notariais totalmente confiáveis e orientando retrospectivas técnicas.

## Visualizações como História Viva
Esse painel fecha o ciclo dos módulos anteriores mostrando, de forma tangível, como nossas decisões de PKI, automação e infraestrutura garantem confiança contínua.
