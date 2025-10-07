# Módulo 9 – Observabilidade e Logs de Transparência

A jornada do cartório digital percorreu fundamentos, certificação, automação e nuvem. Este módulo fecha o ciclo mostrando como observabilidade transforma todas essas escolhas em confiança contínua. Ao monitorar certificados, validar revogações e contar histórias por métricas visíveis, o cidadão percebe que cada ato notarial permanece íntegro mesmo após a emissão.

## Sumário Inspirador
- [Visão Inspiradora de Observabilidade para o Cartório Digital](01_visao_observabilidade_cartorio.md) – apresenta o problema de transparência pública e mostra CT logs como solução.
- [Transparência dos Certificados como Vitrine Pública](02_transparencia_certificados.md) – guia a publicação de emissões em logs e cataloga fingerprints do módulo 2.
- [OCSP Monitorado: Do Medo de Revogação à Confiança Contínua](03_ocsp_monitoramento.md) – revisita o OCSP stapling do módulo 3 e conecta a métricas coletadas no módulo 8.
- [Métricas no Grafana: Tornando a Saúde do Cartório Visível](04_metricas_grafana.md) – transforma métricas em dashboards que contam a história operacional.
- [Desafio Operacional: Orquestrando Alertas que Mobilizam o Cartório](05_desafio_operacional.md) – propõe alertas e runbooks que garantem resposta imediata.

## Objetivos de aprendizagem

- Entender o funcionamento dos logs de transparência (RFC 6962) e como eles aumentam a confiança pública;
- Configurar e monitorar **OCSP stapling** em serviços do cartório;
- Coletar métricas de expiração de certificados e criar dashboards de alerta;
- Integrar o sistema com ferramentas como Prometheus, Grafana e alertmanager.

## Entrega prática

1. Publicar certificados emitidos no módulo 2 em logs de transparência públicos (como o Google Argon) e verificar a inclusão usando `crt.sh`;
2. Ativar OCSP stapling no servidor web configurado no módulo 3 e monitorar o tempo de resposta do OCSP;
3. Criar um dashboard em Grafana ou outro visualizador com métricas de expiração e falhas de revogação;
4. Configurar alertas via e‑mail ou Slack quando um certificado estiver próximo de expirar ou for revogado.

## Referências recomendadas

- **RFC 6962** – Certificate Transparency;
- Documentação do [crt.sh](https://crt.sh/) e dos logs públicos de CT;
- Tutoriais de monitoramento de TLS em Nginx e Apache;
- Guias de uso do Prometheus e Grafana para certificados.
