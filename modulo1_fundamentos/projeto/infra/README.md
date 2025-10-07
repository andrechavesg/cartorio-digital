# Infraestrutura — Módulo 1 (Fundamentos)

Esta pasta disponibiliza manifests Kubernetes que encapsulam o provisionamento da
infraestrutura mínima do módulo utilizando _Jobs_ executados com a imagem
oficial do AWS CLI. Cada `Job` monta os scripts necessários a partir de um
`ConfigMap`, evitando a necessidade de scripts `.sh` versionados separadamente.

## Pré-requisitos
- Cluster Kubernetes funcional com `kubectl` configurado
- StorageClass padrão para provisionar o PVC `frontend-dist`
- Permissões IAM (via `ServiceAccount`, `IRSA` ou outro mecanismo) capazes de
  executar os comandos AWS CLI descritos
- Build do frontend publicado no volume associado ao PVC `frontend-dist`

## Aplicando os manifests

```bash
kubectl apply -k infra/k8s
```

### Executando o provisionamento

1. Aguarde o `Job/cartorio-mod1-deploy-core` finalizar com sucesso; ele cria a
   stack CloudFormation, o bucket e as permissões necessárias.
2. Após disponibilizar os artefatos estáticos no PVC, execute novamente o apply
   para disparar o `Job/cartorio-mod1-sync-frontend`, responsável por sincronizar
   os arquivos com o bucket S3 configurado.

Os Jobs são definidos com `restartPolicy: Never`; reexecute-os removendo o
`Job` existente (`kubectl delete job ...`) e aplicando novamente quando desejar
provisionar/atualizar a infraestrutura.
