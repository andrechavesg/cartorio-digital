# OCSP Monitorado: Do Medo de Revogação à Confiança Contínua

Durante uma auditoria de rotina, descobrimos que a consulta OCSP do cartório digital estava respondendo de forma intermitente. A equipe temia que, em um momento crítico, uma certidão pudesse ser considerada inválida. Transformamos esse receio em motivação para implementar um monitoramento contínuo e inspirador.

## Conceito: OCSP e Confirmação de Validade
Antes de aplicar qualquer comando, revisitamos o propósito do **Online Certificate Status Protocol (OCSP)**: fornecer a terceiros a garantia de que um certificado não foi revogado. Quando ativamos **OCSP stapling** no módulo 3, demos um salto em desempenho; agora precisamos garantir disponibilidade e latência adequadas.

## Passos Conectados ao Cartório Digital
1. **Configurar OCSP stapling** no servidor web oficial (`nginx` presente em `modulo3_tls_mtls/config/nginx.conf`).
2. **Monitorar respostas** com métricas claras para nosso Prometheus interno (configurado no módulo 8).
3. **Criar alertas proativos** no Alertmanager para que a central de plantão seja acionada em caso de indisponibilidade, como detalhado no capítulo 5.

Somente após compreender esses pilares partimos para a configuração prática.

```nginx
# Trecho do nginx.conf com OCSP stapling
ssl_stapling on;
ssl_stapling_verify on;
resolver 8.8.8.8 1.1.1.1 valid=300s;
resolver_timeout 5s;
ssl_trusted_certificate /etc/nginx/certs/chain.pem;
```

Esse trecho garante que o servidor inclua a resposta OCSP assinada a cada handshake TLS, reduzindo chamadas feitas pelos clientes e registrando eventos de stapling no log `modulo3_tls_mtls/logs/ocsp.log`, que alimenta o coletor de métricas.

## Exemplo Guiado de Monitoramento com Prometheus
Com o conceito em mente, coletamos métricas sobre a latência da consulta OCSP usando um exporter dedicado no namespace `observabilidade`.

```bash
# Por que: incluir o alvo OCSP no Prometheus sem reiniciar o serviço.
curl -fsSL https://raw.githubusercontent.com/cartorio-digital/scripts/main/ocsp_exporter.yml \
  | tee /etc/prometheus/file_sd/ocsp_exporter.yml
# Por que: recarregar a configuração e ativar a coleta imediatamente.
systemctl reload prometheus
```

- **Por que executamos:** o arquivo de descoberta adiciona o endpoint `https://ocsp.cartorio.digital.gov.br/status` ao Prometheus já utilizado no módulo 8.
- **Qual insight geramos:** o dashboard mostra tempo de resposta e códigos de erro, garantindo ação pró-ativa caso o stapling falhe.

## Cultura de Vigilância
Monitorar OCSP é mais do que evitar incidentes; é reforçar diariamente a promessa do cartório digital de manter validade incontestável para cada certificado.
