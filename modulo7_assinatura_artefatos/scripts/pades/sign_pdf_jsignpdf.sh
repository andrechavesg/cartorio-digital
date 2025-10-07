#!/usr/bin/env bash
# scripts/pades/sign_pdf_jsignpdf.sh
# Requer: Java, JSignPdf.jar disponível no diretório ou no PATH
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <PDF_IN> [PDF_OUT]"
  exit 1
fi

PDF_IN="$1"
PDF_OUT="${2:-signed.pdf}"
: "${P12_PATH:?Defina P12_PATH para o certificado .p12}"
: "${P12_PASS:?Defina P12_PASS}"
: "${TSA_URL:?Defina TSA_URL RFC 3161}"

java -jar JSignPdf.jar   --visible false   --in "$PDF_IN"   --out "$PDF_OUT"   --ks-type PKCS12   --ks "$P12_PATH"   --ks-pass "$P12_PASS"   --tsa-url "$TSA_URL"   --digest SHA256   --pades true

echo "PDF assinado em: $PDF_OUT"