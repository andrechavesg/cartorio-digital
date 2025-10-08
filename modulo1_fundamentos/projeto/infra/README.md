# Infraestrutura — Módulo 1 (Fundamentos)

Esta pasta disponibiliza duas abordagens complementares para provisionar a
infraestrutura mínima do módulo:

- **Scripts AWS CLI** em `infra/aws`, pensados para execução direta a partir do
  seu terminal (por exemplo, em uma pipeline ou máquina de desenvolvedor) com
  credenciais da AWS configuradas.
- **Manifests Kubernetes** em `infra/k8s`, que empacotam os mesmos comandos em
  _Jobs_ executados dentro de um cluster (incluindo Minikube), ideais para
  ambientes onde se deseja orquestrar o provisionamento via Kubernetes.

## Execução direta com AWS CLI (`infra/aws`)

### Pré-requisitos
- AWS CLI configurado com credenciais válidas
- Docker Image do backend publicada no ECR (ou informe `BACKEND_IMAGE_URI`)
- Artefatos estáticos do frontend disponíveis em `../frontend/dist`

### Passos

```bash
cd infra/aws
./01-deploy-core.sh   # Cria stack CloudFormation, tabelas, buckets e API
./02-sync-frontend.sh # Sincroniza os artefatos estáticos para o bucket
```

Use as variáveis `AWS_REGION`, `ENVIRONMENT`, `BACKEND_IMAGE_URI` e
`FRONTEND_BUCKET` para customizar o comportamento.

## Execução via Kubernetes/Minikube (`infra/k8s`)

### Pré-requisitos
- Cluster Kubernetes funcional com `kubectl` configurado (Minikube incluso)
- StorageClass padrão para provisionar o PVC `frontend-dist`
- Permissões IAM associadas ao `ServiceAccount` capaz de executar os comandos
- Build do frontend publicado no volume associado ao PVC `frontend-dist`

### Passos

```bash
kubectl apply -k infra/k8s
```

1. Aguarde o `Job/cartorio-mod1-deploy-core` finalizar com sucesso; ele cria a
   stack CloudFormation, o bucket e as permissões necessárias.
2. Após disponibilizar os artefatos estáticos no PVC, reaplique os manifests ou
   recrie o `Job/cartorio-mod1-sync-frontend` para sincronizar o frontend.

Os Jobs usam `restartPolicy: Never`; delete-os (`kubectl delete job ...`) antes
de reaplicar quando quiser reprovisionar ou atualizar recursos.
