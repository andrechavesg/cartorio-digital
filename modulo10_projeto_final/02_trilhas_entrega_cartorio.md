# Trilhas de Entrega do Cartório Digital

## Problema vivido pelo cartório digital

O time precisa orquestrar uma entrega final impecável, mas enfrenta atrasos na publicação de novas features, ambientes inconsistentes e falta de confiança no processo de deploy.

## Conexão inspiradora com os módulos anteriores

A infraestrutura automatizada do **Módulo 4**, os pipelines seguros do **Módulo 8** e a governança regulatória do **Módulo 5** foram construídos justamente para garantir que a última milha seja previsível. Some a isso as práticas de proteção de chaves do **Módulo 6** e as assinaturas qualificadas do **Módulo 7** para assegurar que cada release seja legítima e rastreável.

## Ferramentas e comandos como solução

- **Trilha de deploy final em múltiplos estágios**
  ```bash
  terraform workspace select producao
  terraform apply -auto-approve
  helm upgrade --install cartorio charts/cartorio -n cartorio --values values/producao.yaml
  ```
- **Pipeline de promoção assistida**
  ```yaml
  stages:
    - build
    - assinatura
    - deploy
  deploy:
    script:
      - flux reconcile kustomization cartorio-prod --with-source
  ```
- **Checklist de pré-deploy automatizado**
  ```bash
  ./scripts/verificar-compliance.sh && ./scripts/testes-finais.sh
  ```
- **Registro do fluxo de assinatura de release**
  ```bash
  cosign sign --key kms://cartorio-kms chave-deploy@latest
  ```
