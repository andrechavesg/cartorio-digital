#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$ROOT_DIR/apendices/aws-kms-production"
FILE_PATH="$TARGET_DIR/09_requisitos_iti.md"

mkdir -p "$TARGET_DIR"

if [ ! -f "$FILE_PATH" ]; then
cat > "$FILE_PATH" <<'EOF'
# Requisitos para Autoridade Certificadora Conforme ITI/ICP-Brasil
<!-- Arquivo inicial criado por create_aws_kms_docs.sh -->
EOF
fi
