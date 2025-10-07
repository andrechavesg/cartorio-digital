# Infraestrutura — Módulo 3 (TLS e mTLS)

Os manifests Kubernetes deste módulo criam um `Job` que reutiliza a autoridade
raiz publicada no módulo 2, gera um bucket truststore com o certificado e
configura mTLS no API Gateway, além de solicitar/associar o certificado ACM a um
domínio customizado.

## Uso

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod3-enable-mtls` pode ser reexecutado conforme necessário
removendo-o e aplicando novamente. Ajuste as configurações de domínio no
`ConfigMap` `cartorio-mod3-settings` antes da execução.
