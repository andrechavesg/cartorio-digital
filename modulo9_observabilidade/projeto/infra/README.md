# Infraestrutura — Módulo 9 (Observabilidade)

Job Kubernetes que habilita CloudWatch Logs, alarmes e X-Ray.

## Execução

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod9-enable-observability` cria os recursos de monitoramento.
