#!/usr/bin/env bash
set -euo pipefail

ENVIRONMENT="${ENVIRONMENT:-dev}"
STACK_NAME="cartorio-mod1-${ENVIRONMENT}"
FRONTEND_BUCKET="${FRONTEND_BUCKET:-$(aws cloudformation describe-stacks \
  --stack-name "${STACK_NAME}" \
  --query "Stacks[0].Outputs[?OutputKey=='FrontendBucketName'].OutputValue" \
  --output text)}"

aws s3 sync ../frontend/dist "s3://${FRONTEND_BUCKET}" --delete
