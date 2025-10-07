#!/usr/bin/env bash
# scripts/jarsigner/sign_jar.sh
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <JAR_IN> [ALIAS]"
  exit 1
fi

JAR_IN="$1"
ALIAS="${2:-sign}"
: "${P12_PATH:?Defina P12_PATH}"
: "${P12_PASS:?Defina P12_PASS}"
: "${TSA_URL:?Defina TSA_URL}"

keytool -importkeystore -srckeystore "$P12_PATH" -srcstoretype pkcs12 -srcstorepass "$P12_PASS"   -destkeystore keystore.jks -deststoretype JKS -deststorepass changeit -alias "$ALIAS"

jarsigner -keystore keystore.jks -storepass changeit   -tsa "$TSA_URL" -digestalg SHA-256 -sigalg SHA256withRSA   "$JAR_IN" "$ALIAS"

jarsigner -verify -verbose -certs "$JAR_IN"
echo "JAR assinado: $JAR_IN"