#!/usr/bin/env bash
set -euo pipefail
REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"

KEY_ID="$(aws kms create-key \
  --region "${REGION}" \
  --description "Cartorio Digital (${ENVIRONMENT}) citizen credentials" \
  --key-usage SIGN_VERIFY \
  --origin AWS_KMS \
  --query KeyMetadata.KeyId --output text)"

aws kms create-alias \
  --region "${REGION}" \
  --alias-name "alias/cartorio/${ENVIRONMENT}/citizen" \
  --target-key-id "${KEY_ID}"

aws ssm put-parameter \
  --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/kms/citizen_key_arn" \
  --type String \
  --value "$(aws kms describe-key --region "${REGION}" --key-id "${KEY_ID}" --query KeyMetadata.Arn --output text)" \
  --overwrite
