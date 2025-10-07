# 03 — Assinando Artefatos de Software (JAR/EXE)

## JAR — `jarsigner` (Java)
Arquivo: `scripts/jarsigner/sign_jar.sh`
```bash
#!/usr/bin/env bash
set -euo pipefail

JAR_IN=${1:?Informe o JAR}
P12=${P12_PATH:?Defina P12_PATH}
P12_PASS=${P12_PASS:?Defina P12_PASS}
ALIAS=${ALIAS_NAME:-"sign"}
TSA_URL=${TSA_URL:?Informe TSA_URL RFC 3161}

# Importa o .p12 para um keystore temporário JKS (opcional, se necessário)
keytool -importkeystore -srckeystore "$P12" -srcstoretype pkcs12 -srcstorepass "$P12_PASS"   -destkeystore keystore.jks -deststoretype JKS -deststorepass changeit -alias "$ALIAS"

jarsigner -keystore keystore.jks -storepass changeit   -tsa "$TSA_URL" -digestalg SHA-256 -sigalg SHA256withRSA   "$JAR_IN" "$ALIAS"

jarsigner -verify -verbose -certs "$JAR_IN"
```

## EXE — `signtool` (Windows)
Arquivo: `scripts/signtool/sign_exe.ps1`
```powershell
param(
  [Parameter(Mandatory=$true)][string]$ExePath,
  [Parameter(Mandatory=$true)][string]$PfxPath,
  [Parameter(Mandatory=$true)][string]$PfxPass,
  [Parameter(Mandatory=$true)][string]$TimestampUrl
)

signtool sign /f $PfxPath /p $PfxPass /fd SHA256 /tr $TimestampUrl /td SHA256 $ExePath
signtool verify /pa /v $ExePath
```