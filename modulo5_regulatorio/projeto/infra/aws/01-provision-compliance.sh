#!/usr/bin/env bash
set -euo pipefail
REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"

aws dynamodb create-table \
  --region "${REGION}" \
  --table-name "cartorio-${ENVIRONMENT}-compliance-audit" \
  --billing-mode PAY_PER_REQUEST \
  --attribute-definitions AttributeName=certificateSerial,AttributeType=S \
  --key-schema AttributeName=certificateSerial,KeyType=HASH || true

aws logs create-log-group \
  --region "${REGION}" \
  --log-group-name "/cartorio/${ENVIRONMENT}/compliance" || true

aws ssm put-parameter \
  --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/compliance/table" \
  --type String \
  --value "cartorio-${ENVIRONMENT}-compliance-audit" \
  --overwrite
