#!/usr/bin/env bash
set -euo pipefail
REGION="${AWS_REGION:-us-east-1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"

collect() {
  local name="$1" query="$2"
  aws cloudformation describe-stacks \
    --region "${REGION}" --stack-name "$name" \
    --query "$query" --output json 2>/dev/null || echo '[]'
}

aws ssm put-parameter --region "${REGION}" \
  --name "/cartorio/${ENVIRONMENT}/summary" \
  --type String \
  --overwrite \
  --value "$(python - <<'PY'
import json, os
summary = {
  "module1": json.loads(os.environ.get("MOD1", "[]")),
  "module4": json.loads(os.environ.get("MOD4", "[]"))
}
print(json.dumps(summary))
PY
)" \
  MOD1="$(collect "cartorio-mod1-${ENVIRONMENT}" "Stacks[0].Outputs")" \
  MOD4="$(collect "cartorio-mod4-acme-${ENVIRONMENT}" "Stacks[0].Outputs")"

echo "Resumo publicado em /cartorio/${ENVIRONMENT}/summary"
