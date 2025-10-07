# Infraestrutura — Módulo 10 (Projeto Final Integrado)

Manifests Kubernetes que executam um `Job` para consolidar saídas de stacks
anteriores em um parâmetro SSM.

## Execução

```bash
kubectl apply -k infra/k8s
```

Atualize o `ConfigMap cartorio-mod10-settings` com a lista de stacks a serem
coletadas antes da execução.
