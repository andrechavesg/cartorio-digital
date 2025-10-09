# Infraestrutura — Módulo 4 (Automação ACME)

Esta pasta oferece duas alternativas para aplicar a stack CloudFormation com
DynamoDB, SQS e Step Functions utilizadas no fluxo ACME:

- **Scripts AWS CLI** em `infra/aws` para execução direta no terminal.
- **Manifests Kubernetes** em `infra/k8s`, que disparam um `Job` compatível com
  clusters gerenciados ou Minikube.

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-deploy-acme.sh
```

Use `AWS_REGION` e `ENVIRONMENT` para ajustar nomes.

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

O template é montado via `ConfigMap` e consumido pelo `Job/cartorio-mod4-deploy-acme`.
Atualize o `ConfigMap cartorio-mod4-settings` para alterar região ou ambiente
antes de reaplicar.
