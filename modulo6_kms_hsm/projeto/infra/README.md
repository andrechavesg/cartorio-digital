# Infraestrutura — Módulo 6 (KMS/HSM)

Disponibiliza scripts AWS CLI e manifests Kubernetes para provisionar as chaves
KMS necessárias ao módulo.

## Execução

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-create-kms.sh
```

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod6-create-kms` cria a chave de assinatura e publica o ARN no
SSM Parameter Store.
