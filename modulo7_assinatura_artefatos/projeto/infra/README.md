# Infraestrutura — Módulo 7 (Assinatura de Artefatos)

Provisiona chave de assinatura dedicada, bucket versionado e tabela DynamoDB
via `Job` Kubernetes.

## Execução

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod7-code-signing` adiciona o alias de assinatura, habilita
versionamento no bucket e registra os parâmetros necessários.
