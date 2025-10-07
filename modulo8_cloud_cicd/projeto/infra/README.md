# Infraestrutura — Módulo 8 (Cloud e CI/CD)

Manifests Kubernetes que executam um `Job` para criar bucket de artefatos, roles
IAM e pipeline CodePipeline/CodeBuild.

## Execução

```bash
kubectl apply -k infra/k8s
```

Configure `Owner`, `Repo`, `Branch` e `OAuthToken` no `ConfigMap` de configurações
antes da execução.
