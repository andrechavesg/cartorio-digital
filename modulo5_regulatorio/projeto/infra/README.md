# Infraestrutura — Módulo 5 (Requisitos Regulatórios)

Aplica recursos de auditoria via `Job` Kubernetes executando AWS CLI.

## Uso

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod5-provision-compliance` criará a tabela DynamoDB, o grupo de
logs e publicará os parâmetros no SSM Parameter Store.
