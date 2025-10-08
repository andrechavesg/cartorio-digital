# Infraestrutura — Módulo 9 (Observabilidade)

Disponibiliza scripts AWS CLI e manifests Kubernetes para habilitar CloudWatch
Logs, alarmes e X-Ray.

## Execução

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-enable-observability.sh
```

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod9-enable-observability` cria os recursos de monitoramento.
