# Módulo 9 – Observabilidade e Logs de Transparência

Para garantir a confiabilidade do cartório digital, é necessário monitorar certificados, verificar status de revogação e publicar eventos relevantes.  Este módulo aborda técnicas de observabilidade, como o uso de **Certificate Transparency (CT) logs**, OCSP stapling e métricas de expiração.

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
