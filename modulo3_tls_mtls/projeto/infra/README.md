# Infraestrutura — Módulo 3 (TLS e mTLS)

Esta pasta disponibiliza duas abordagens para habilitar mTLS reutilizando a
autoridade raiz publicada no módulo 2:

- **Scripts AWS CLI** em `infra/aws`, para execução direta no terminal.
- **Manifests Kubernetes** em `infra/k8s`, que criam um `Job` responsável por
  gerar o truststore, solicitar o certificado ACM e configurar o mapeamento do
  domínio customizado (compatível com Minikube).

## Execução direta com AWS CLI (`infra/aws`)

### Pré-requisitos
- AWS CLI configurado
- Parâmetros do módulo 2 disponíveis no SSM/Secrets Manager

### Passos

```bash
cd infra/aws
./01-enable-mtls.sh
```

Customize com `DOMAIN_NAME`, `API_ID`, `TRUST_BUCKET` e `CERT_ARN` conforme
necessário.

## Execução via Kubernetes/Minikube (`infra/k8s`)

### Pré-requisitos
- Cluster Kubernetes com acesso à AWS
- Permissões IAM para ACM, API Gateway, S3 e SSM

### Passos

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod3-enable-mtls` pode ser reexecutado removendo-o e aplicando
novamente. Ajuste as configurações de domínio no `ConfigMap`
`cartorio-mod3-settings` antes da execução.
