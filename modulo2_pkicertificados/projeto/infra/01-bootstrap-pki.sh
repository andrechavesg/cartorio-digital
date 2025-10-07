#!/usr/bin/env bash
set -euo pipefail

REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
WORKDIR="$(dirname "$0")/out"
SECRET_NAME="${SECRET_NAME:-cartorio/${ENVIRONMENT}/pki/root}"
CRL_BUCKET="${CRL_BUCKET:-cartorio-${ENVIRONMENT}-crl-$(date +%s)}"
mkdir -p "${WORKDIR}"

ROOT_KEY="${WORKDIR}/root-ca.key.pem"
ROOT_CERT="${WORKDIR}/root-ca.cert.pem"
CRL_FILE="${WORKDIR}/root-ca.crl.pem"

if [[ ! -f "${ROOT_KEY}" ]]; then
  openssl genrsa -out "${ROOT_KEY}" 4096
fi

if [[ ! -f "${ROOT_CERT}" ]]; then
  openssl req -x509 -new -key "${ROOT_KEY}" \
    -out "${ROOT_CERT}" \
    -days 3650 -sha256 \
    -subj "/C=BR/ST=SP/L=São Paulo/O=Cartorio Digital/OU=Root CA/CN=Cartorio Digital Root (${ENVIRONMENT})"
fi

cat > "${WORKDIR}/openssl.cnf" <<'CNF'
[ ca ]
default_ca = CA_default
[ CA_default ]
dir = .
database = $dir/index.txt
serial = $dir/serial
crlnumber = $dir/crlnumber
default_md = sha256
default_days = 365
default_crl_days = 7
policy = policy_loose
[ policy_loose ]
commonName = supplied
[ crl_ext ]
authorityKeyIdentifier=keyid:always
CNF

touch "${WORKDIR}/index.txt"
[[ -f "${WORKDIR}/serial" ]] || echo 01 > "${WORKDIR}/serial"
[[ -f "${WORKDIR}/crlnumber" ]] || echo 01 > "${WORKDIR}/crlnumber"

openssl ca -config "${WORKDIR}/openssl.cnf" -gencrl \
  -keyfile "${ROOT_KEY}" -cert "${ROOT_CERT}" \
  -out "${CRL_FILE}"

if ! aws secretsmanager describe-secret --region "${REGION}" --secret-id "${SECRET_NAME}" >/dev/null 2>&1; then
  aws secretsmanager create-secret \
    --region "${REGION}" \
    --name "${SECRET_NAME}" \
    --description "PKI root material for Cartorio Digital (${ENVIRONMENT})" \
    --secret-string "$(python - <<'PY'
import json, pathlib
root = pathlib.Path("${ROOT_KEY}").read_text()
cert = pathlib.Path("${ROOT_CERT}").read_text()
print(json.dumps({"private_key_pem": root, "certificate_pem": cert}))
PY
)"
else
  aws secretsmanager put-secret-value \
    --region "${REGION}" \
    --secret-id "${SECRET_NAME}" \
    --secret-string "$(python - <<'PY'
import json, pathlib
root = pathlib.Path("${ROOT_KEY}").read_text()
cert = pathlib.Path("${ROOT_CERT}").read_text()
print(json.dumps({"private_key_pem": root, "certificate_pem": cert}))
PY
)"
fi

if [[ "${REGION}" == "us-east-1" ]]; then
  aws s3api create-bucket --bucket "${CRL_BUCKET}" --region "${REGION}" || true
else
  aws s3api create-bucket --bucket "${CRL_BUCKET}" --region "${REGION}" \
    --create-bucket-configuration "LocationConstraint=${REGION}" || true
fi

aws s3 cp "${CRL_FILE}" "s3://${CRL_BUCKET}/crl.pem"

SECRET_ARN="$(aws secretsmanager describe-secret --region "${REGION}" --secret-id "${SECRET_NAME}" --query ARN --output text)"

aws ssm put-parameter --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/pki/root_secret_arn" \
  --type String \
  --value "${SECRET_ARN}" \
  --overwrite

aws ssm put-parameter --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/pki/crl_bucket" \
  --type String \
  --value "${CRL_BUCKET}" \
  --overwrite
