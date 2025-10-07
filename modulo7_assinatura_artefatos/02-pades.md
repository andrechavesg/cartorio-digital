# 02 — PAdES: Assinando PDF + Timestamp

> **PAdES** (ETSI EN 319 142‑1) é o padrão para **PDF Advanced Electronic Signatures**.

## Caminhos de implementação
1. **Ferramenta CLI/GUI** (ex.: **JSignPdf**, **DSS CLI** ou equivalente) para assinar PDF/A.
2. **Linguagens/bibliotecas** (Java/.NET) com suporte PAdES.
3. **Serviço externo** (fornecedor QTSP) — útil em produção.

## Parâmetros essenciais
- **Certificado** (arquivo `.p12`/`.pfx`, token, smartcard, ou HSM).
- **Algoritmo**: SHA‑256+RSA/ECDSA.
- **PAdES-LT**/**LTA** (ideal com **timestamp** e cadeia/OCSP/CRL embutidos).
- **TSA (RFC 3161)**: URL, política (OID) e autenticação (se houver).

## Exemplo (script com JSignPdf — referência)
Quando lançamos o piloto do **Cartório Digital**, o time percebeu que autenticar as certidões emitidas no módulo 2 em PDF exigia mais do que confiança verbal: precisávamos de um fluxo repetível que blindasse o documento contra fraudes. O comando do **JSignPdf** se tornou o motor dessa virada, pois automatiza a aplicação da assinatura PAdES com carimbo de tempo, garantindo que cada certidão saia do cartório já validada e com evidências criptográficas dignas da nossa ambição digital.

Arquivo: `scripts/pades/sign_pdf_jsignpdf.sh`
```bash
#!/usr/bin/env bash
set -euo pipefail

PDF_IN=${1:?Informe o PDF de entrada}
PDF_OUT=${2:-signed.pdf}
P12=${P12_PATH:?Defina P12_PATH para o certificado .p12}
P12_PASS=${P12_PASS:?Defina P12_PASS para a senha}
TSA_URL=${TSA_URL:?Defina TSA_URL RFC 3161}

java -jar JSignPdf.jar   --visible false   --in "$PDF_IN"   --out "$PDF_OUT"   --ks-type PKCS12   --ks "$P12"   --ks-pass "$P12_PASS"   --tsa-url "$TSA_URL"   --digest SHA256   --pades true
```

> Ajuste ao executor real do projeto. Em produção, prefira **HSM** e política PAdES **LTA**.