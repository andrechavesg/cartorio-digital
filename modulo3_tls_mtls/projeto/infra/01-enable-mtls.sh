#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
DOMAIN_NAME="${DOMAIN_NAME:-api.${ENVIRONMENT}.cartorio.example}"
TRUST_BUCKET="${TRUST_BUCKET:-cartorio-${ENVIRONMENT}-truststore-$(date +%s)}"
API_ID="${API_ID:-$(aws apigatewayv2 get-apis --region "${REGION}" \
  --query "Items[?Name=='cartorio-${ENVIRONMENT}-api'].ApiId" --output text)}"
ROOT_CERT_PATH="$(mktemp)"

SECRET_ARN="$(aws ssm get-parameter --region "${REGION}" --name "/cartorio/${ENVIRONMENT}/pki/root_secret_arn" --query Parameter.Value --output text)"
aws secretsmanager get-secret-value --region "${REGION}" --secret-id "${SECRET_ARN}" \
  --query SecretString --output text | python - <<'PY'
import json, sys, pathlib
payload = json.loads(sys.stdin.read())
pathlib.Path("${ROOT_CERT_PATH}").write_text(payload["certificate_pem"])
PY

if [[ "${REGION}" == "us-east-1" ]]; then
  aws s3api create-bucket --bucket "${TRUST_BUCKET}" --region "${REGION}" || true
else
  aws s3api create-bucket --bucket "${TRUST_BUCKET}" --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}" || true
fi

aws s3 cp "${ROOT_CERT_PATH}" "s3://${TRUST_BUCKET}/root-ca.pem"

CERT_ARN="${CERT_ARN:-$(aws acm request-certificate \
  --region "${REGION}" \
  --domain-name "${DOMAIN_NAME}" \
  --validation-method DNS \
  --query CertificateArn --output text)}"

echo "Certificado solicitado (${CERT_ARN}). Valide o domínio no ACM antes de mapear o custom domain."

aws apigatewayv2 create-domain-name \
  --region "${REGION}" \
  --domain-name "${DOMAIN_NAME}" \
  --domain-name-configurations "CertificateArn=${CERT_ARN},EndpointType=REGIONAL,SecurityPolicy=TLS_1_2" \
  --mutual-tls-authentication "TruststoreUri=s3://${TRUST_BUCKET}/root-ca.pem,TruststoreVersion=$(date +%s)" || true

aws apigatewayv2 create-api-mapping \
  --region "${REGION}" \
  --domain-name "${DOMAIN_NAME}" \
  --api-id "${API_ID}" \
  --stage "prod" \
  --api-mapping-key "" || true
