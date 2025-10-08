# Infraestrutura — Módulo 10 (Projeto Final Integrado)

Disponibiliza scripts AWS CLI e manifests Kubernetes que consolidam as saídas
de stacks anteriores em um parâmetro SSM.

## Execução

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-aggregate-outputs.sh
```

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

Atualize o `ConfigMap cartorio-mod10-settings` com a lista de stacks a serem
coletadas antes da execução.
