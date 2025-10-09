#!/usr/bin/env bash
set -euo pipefail
REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
ARTIFACT_BUCKET="${ARTIFACT_BUCKET:-cartorio-${ENVIRONMENT}-signed-artifacts}"

aws kms create-alias \
  --region "${REGION}" \
  --alias-name "alias/cartorio/${ENVIRONMENT}/code-signing" \
  --target-key-id "$(aws kms create-key \
    --region "${REGION}" \
    --description "Cartorio Digital (${ENVIRONMENT}) code signing" \
    --key-usage SIGN_VERIFY \
    --origin AWS_KMS \
    --query KeyMetadata.KeyId --output text)"

if [[ "${REGION}" == "us-east-1" ]]; then
  aws s3api create-bucket --bucket "${ARTIFACT_BUCKET}" --region "${REGION}" || true
else
  aws s3api create-bucket --bucket "${ARTIFACT_BUCKET}" --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}" || true
fi
aws s3api put-bucket-versioning --bucket "${ARTIFACT_BUCKET}" --versioning-configuration Status=Enabled

aws dynamodb create-table \
  --region "${REGION}" \
  --table-name "cartorio-${ENVIRONMENT}-signed-artifacts" \
  --billing-mode PAY_PER_REQUEST \
  --attribute-definitions AttributeName=artifactId,AttributeType=S \
  --key-schema AttributeName=artifactId,KeyType=HASH || true

aws ssm put-parameter --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/signing/artifact_bucket" \
  --type String \
  --value "${ARTIFACT_BUCKET}" \
  --overwrite
