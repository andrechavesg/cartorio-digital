# Infraestrutura — Módulo 8 (Cloud e CI/CD)

Disponibiliza scripts AWS CLI e manifests Kubernetes que criam bucket de
artefatos, roles IAM e a pipeline CodePipeline/CodeBuild do módulo.

## Execução

## Execução direta com AWS CLI (`infra/aws`)

```bash
cd infra/aws
./01-bootstrap-pipeline.sh
```

Configure `GITHUB_OWNER`, `GITHUB_REPO`, `GITHUB_BRANCH`, `GITHUB_TOKEN` e
`CODEBUILD_REPO_URL` conforme necessário.

## Execução via Kubernetes/Minikube (`infra/k8s`)

```bash
kubectl apply -k infra/k8s
```

Atualize `Owner`, `Repo`, `Branch` e `OAuthToken` no `ConfigMap` antes da
execução.
