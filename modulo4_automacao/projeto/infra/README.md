# Infraestrutura — Módulo 4 (Automação ACME)

Este módulo usa manifests Kubernetes para acionar um `Job` que aplica a stack
CloudFormation responsável por DynamoDB, SQS e Step Functions utilizados no
fluxo ACME.

## Execução

```bash
kubectl apply -k infra/k8s
```

O template é montado via `ConfigMap` e consumido pelo `Job/cartorio-mod4-deploy-acme`.
Atualize o `ConfigMap cartorio-mod4-settings` para alterar região ou ambiente
antes de reaplicar.
