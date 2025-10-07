#!/usr/bin/env bash
# scripts/timestamp/ts_request.sh
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <ARQUIVO>"
  exit 1
fi

ARQ="$1"
: "${TSA_URL:?Defina TSA_URL}"
POLICY_OID="${POLICY_OID:-}"

openssl ts -query -data "$ARQ" -sha256 ${POLICY_OID:+-policy $POLICY_OID} -cert -out request.tsq
curl -s -S -H "Content-Type: application/timestamp-query" --data-binary @request.tsq "$TSA_URL" -o response.tsr
openssl ts -reply -in response.tsr -text > response.txt || true

echo "TSR salva em response.tsr. Detalhes: response.txt"