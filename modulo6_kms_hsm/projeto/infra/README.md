# Infraestrutura — Módulo 6 (KMS/HSM)

Provisionamento de chaves KMS por meio de um `Job` Kubernetes com AWS CLI.

## Execução

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod6-create-kms` cria a chave de assinatura e publica o ARN no
SSM Parameter Store.
