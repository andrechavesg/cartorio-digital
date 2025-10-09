# Infraestrutura — Módulo 5 (Requisitos Regulatórios)

Disponibiliza scripts AWS CLI e manifests Kubernetes para aplicar os recursos
de auditoria necessários ao módulo.

## Uso

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-provision-compliance.sh
```

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod5-provision-compliance` criará a tabela DynamoDB, o grupo de
logs e publicará os parâmetros no SSM Parameter Store.
