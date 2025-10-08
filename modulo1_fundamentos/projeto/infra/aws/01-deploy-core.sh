#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
STACK_NAME="cartorio-mod1-${ENVIRONMENT}"
ACCOUNT_ID="$(aws sts get-caller-identity --query Account --output text)"
IMAGE_URI="${BACKEND_IMAGE_URI:-${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/cartorio-mod1-backend:latest}"
FRONTEND_BUCKET="${FRONTEND_BUCKET:-${STACK_NAME}-frontend-${ACCOUNT_ID}-${REGION}}"

if ! aws s3api head-bucket --bucket "${FRONTEND_BUCKET}" >/dev/null 2>&1; then
  if [[ "${REGION}" == "us-east-1" ]]; then
    aws s3api create-bucket --bucket "${FRONTEND_BUCKET}" --region "${REGION}"
  else
    aws s3api create-bucket --bucket "${FRONTEND_BUCKET}" --region "${REGION}" \
      --create-bucket-configuration "LocationConstraint=${REGION}"
  fi
fi

aws s3 website "s3://${FRONTEND_BUCKET}" --index-document index.html --error-document index.html

aws cloudformation deploy \
  --region "${REGION}" \
  --stack-name "${STACK_NAME}" \
  --template-file "$(dirname "$0")/templates/core.yaml" \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides \
      EnvironmentName="${ENVIRONMENT}" \
      BackendImageUri="${IMAGE_URI}" \
      FrontendBucketName="${FRONTEND_BUCKET}"

aws cloudformation describe-stacks \
  --stack-name "${STACK_NAME}" \
  --query "Stacks[0].Outputs" \
  --output table
