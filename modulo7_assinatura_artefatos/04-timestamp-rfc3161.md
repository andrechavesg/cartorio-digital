# 04 — Carimbo do Tempo (RFC 3161)

> O **timestamp** prova que um dado/assinatura existia em um momento específico.

Nas primeiras rodadas de auditoria do **Cartório Digital**, percebemos que apenas assinar os documentos não bastava: precisávamos comprovar, anos depois, que aquela assinatura foi realmente aplicada no instante prometido. Sem essa trilha temporal, a confiança nas certidões digitais corria risco. Foi aí que desenhamos um fluxo claro com `openssl ts`, capaz de registrar cada evidência no tempo e dar à nossa equipe a segurança de dizer, com orgulho, que o cartório domina o relógio da confiança.

## Fluxo geral
1. Calcule o **hash** do artefato (ex.: SHA‑256).
2. Envie uma **TSQ** (Time-Stamp Request) para a **TSA** (URL RFC 3161).
3. Receba a **TSR** (Time-Stamp Response) e **armazene** junto às evidências.
4. **Verifique** a TSR periodicamente (cadeia/OCSP/CRL) para longevidade.

## Exemplo com OpenSSL
Arquivo: `scripts/timestamp/ts_request.sh`
```bash
#!/usr/bin/env bash
set -euo pipefail

ARQ=${1:?Informe o arquivo para timestamp}
TSA_URL=${TSA_URL:?Defina TSA_URL}
POLICY_OID=${POLICY_OID:-""}

HASH=$(openssl dgst -sha256 -binary "$ARQ" | openssl base64 -A)
# Cria requisição TSQ a partir do arquivo diretamente
openssl ts -query -data "$ARQ" -sha256 ${POLICY_OID:+-policy $POLICY_OID} -cert -out request.tsq

# Envia para a TSA RFC 3161
curl -s -S -H "Content-Type: application/timestamp-query" --data-binary @request.tsq "$TSA_URL" -o response.tsr

# Verifica a resposta (é necessário ter trust anchors/CA configurados no ambiente)
openssl ts -reply -in response.tsr -text > response.txt || true
echo "TSR salva em response.tsr; detalhes em response.txt"
```

> **Produção**: use TSA confiável (QTSP/eIDAS, ICP‑Brasil, etc.). Em laboratório, é possível usar uma TSA compatível com RFC 3161.