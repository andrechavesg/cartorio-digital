# Infraestrutura — Módulo 2 (PKI e Certificados)

Os manifests Kubernetes deste módulo executam um _Job_ que gera a autoridade
certificadora raiz com `openssl`, publica a CRL em um bucket S3 e persiste as
informações sensíveis no AWS Secrets Manager e no Parameter Store. Todo o fluxo
ocorre dentro de um contêiner `amazon/aws-cli`, garantindo que as operações
utilizem ferramentas reais.

## Pré-requisitos
- Cluster Kubernetes com acesso à AWS via permissões IAM
- `kubectl`/`kustomize`
- binários `openssl` e `python3` disponíveis na imagem (já fornecidos pelo
  contêiner utilizado)

## Execução

```bash
kubectl apply -k infra/k8s
```

O `Job/cartorio-mod2-bootstrap-pki` é criado com `restartPolicy: Never`. Para
reexecutá-lo, remova o Job (`kubectl delete job cartorio-mod2-bootstrap-pki -n cartorio-mod2`)
e aplique novamente os manifests.
