# Infraestrutura — Módulo 7 (Assinatura de Artefatos)

Disponibiliza scripts AWS CLI e manifests Kubernetes para provisionar a chave de
assinatura dedicada, o bucket versionado e a tabela DynamoDB do módulo.

## Execução

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-code-signing.sh
```

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod7-code-signing` adiciona o alias de assinatura, habilita
versionamento no bucket e registra os parâmetros necessários.
