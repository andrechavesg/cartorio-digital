#!/bin/bash
#
# Script de apoio para o módulo 1.
# Gera chaves RSA e EC (curva P-256) e salva os arquivos no diretório atual.

set -euo pipefail

echo "Gerando chave RSA 2048 bits..."
openssl genpkey -algorithm RSA -out rsa_private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in rsa_private.pem -out rsa_public.pem

echo "Gerando chave EC (P-256)..."
openssl genpkey -algorithm EC -out ec_private.pem -pkeyopt ec_paramgen_curve:P-256
openssl pkey -pubout -in ec_private.pem -out ec_public.pem

echo "Chaves geradas:"
ls -l *.pem
