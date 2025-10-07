# Desafio Operacional: Orquestrando Alertas que Mobilizam o Cartório

Após conquistar a confiança do público, enfrentamos um novo desafio: como garantir que, em plena madrugada, alguém responda a um alerta de expiração crítica? Essa inquietação nos levou a propor um exercício coletivo que consolida tudo o que aprendemos.

## Conceito: Alertas Orientados a Ação
Antes de configurar qualquer notificação, reforçamos o conceito de que **alertas efetivos são aqueles que disparam ações concretas**. Não basta enviar e-mails; precisamos de playbooks que conectem o aviso à resposta.

## Conexão com os Módulos Anteriores
- Do módulo 2 trazemos o inventário de certificados.
- Do módulo 3 herdamos a infraestrutura TLS com OCSP stapling.
- Do módulo 8 utilizamos o pipeline de CI/CD na nuvem para aplicar configurações automaticamente.

## Exercício Guiado: Criando Alertas no Alertmanager
Com a filosofia alinhada, configuramos um alerta que dispara quando restarem menos de 15 dias para um certificado expirar e outro quando a latência do OCSP exceder 2 segundos.

```bash
cat <<'ALERTS' > /etc/prometheus/alert_rules/cartorio_certificados.yml
groups:
  - name: cartorio-certificados
    rules:
      - alert: CertificadoProximoExpirar
        expr: (cert_expiry_seconds / 86400) < 15
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Certificado próximo de expirar"
          description: "{{ $labels.instance }} expira em {{ $value | printf "%.1f" }} dias"
      - alert: LatenciaOCSPElevada
        expr: ocsp_latency_seconds > 2
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Latência do OCSP acima do esperado"
          description: "{{ $labels.instance }} respondeu em {{ $value }} segundos"
ALERTS
systemctl reload prometheus
```

## Plano de Ação Inspirador
1. **Alertmanager envia mensagem** para o canal `#plantao-cartorio` no Slack.
2. **Runbook automatizado** (pipeline do módulo 8) aplica renovação ou reinicia o serviço com OCSP.
3. **Debrief matinal** registra aprendizados e ajusta SLOs.

Ao final, transformamos alerta em oportunidade de superação, garantindo que o cartório digital esteja pronto para servir com excelência ininterrupta.
