# Infraestrutura — Módulo 2 (PKI e Certificados)

Esta pasta oferece duas formas de executar o bootstrap da autoridade
certificadora raiz utilizada no módulo:

- **Scripts AWS CLI** em `infra/aws`, para execução direta via terminal ou
  pipelines.
- **Manifests Kubernetes** em `infra/k8s`, que encapsulam os mesmos comandos em
  um _Job_ dentro do cluster (incluindo Minikube).

## Execução direta com AWS CLI (`infra/aws`)

### Pré-requisitos
- AWS CLI configurado com credenciais válidas
- OpenSSL instalado localmente

### Passos

```bash
cd infra/aws
./01-bootstrap-pki.sh
```

Customize via `AWS_REGION`, `ENVIRONMENT`, `SECRET_NAME` e `CRL_BUCKET` conforme
necessário. Os artefatos gerados ficam em `infra/aws/out`.

## Execução via Kubernetes/Minikube (`infra/k8s`)

### Pré-requisitos
- Cluster Kubernetes com acesso à AWS via permissões IAM
- `kubectl`/`kustomize`

### Passos

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod2-bootstrap-pki` gera a CA, publica a CRL em um bucket S3 e
persiste as referências no Secrets Manager e no Parameter Store. Para
reexecutá-lo, remova o Job (`kubectl delete job cartorio-mod2-bootstrap-pki -n cartorio-mod2`)
e aplique novamente os manifests.
