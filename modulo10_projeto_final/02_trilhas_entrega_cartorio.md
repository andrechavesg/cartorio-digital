# Trilhas de Entrega do Cartório Digital

## Problema vivido pelo cartório digital

O time precisa orquestrar uma entrega final impecável, mas enfrenta atrasos na publicação de novas features, ambientes inconsistentes e falta de confiança no processo de deploy.

## Conexão inspiradora com os módulos anteriores

A infraestrutura automatizada do **Módulo 4**, os pipelines seguros do **Módulo 8** e a governança regulatória do **Módulo 5** foram construídos justamente para garantir que a última milha seja previsível. Some a isso as práticas de proteção de chaves do **Módulo 6** e as assinaturas qualificadas do **Módulo 7** para assegurar que cada release seja legítima e rastreável.

## Ferramentas e comandos como solução

- **Trilha de deploy final em múltiplos estágios** – quando a equipe precisar demonstrar que o fluxo de infraestrutura como código está pronto para promover o cartório para produção, execute a sequência completa de implantação.
  ```bash
  # Por que: selecionar o workspace garante isolamento das configurações de produção.
  terraform workspace select producao
  # Por que: aplicar as mudanças aprovadas com registro automático do plano.
  terraform apply -auto-approve
  # Por que: alinhar a camada de aplicação às configurações sancionadas pela diretoria técnica.
  helm upgrade --install cartorio charts/cartorio -n cartorio --values values/producao.yaml
  ```
- **Pipeline de promoção assistida** – para evidenciar que apenas artefatos verificados chegam à produção, apresente a pipeline com estágios explícitos e um comando de reconciliação GitOps inspirado nos módulos de automação.
  ```yaml
  stages:
    - build
    - assinatura
    - deploy
  deploy:
    script:
      - |
        # Por que: alinhar o estado desejado com o repositório Git e registrar a promoção no GitOps.
        flux reconcile kustomization cartorio-prod --with-source
  ```
- **Checklist de pré-deploy automatizado** – antes de apertar o botão final, mostre como os scripts herdados dos módulos anteriores validam compliance e testes com uma única chamada.
  ```bash
  # Por que: reunir evidências regulatórias e sanidade técnica antes da virada.
  ./scripts/verificar-compliance.sh && ./scripts/testes-finais.sh
  ```
- **Registro do fluxo de assinatura de release** – para reforçar a rastreabilidade das versões liberadas, assine a build final usando a mesma raiz criptográfica construída no projeto.
  ```bash
  # Por que: associar a release à cadeia criptográfica reconhecida pelos órgãos reguladores.
  cosign sign --key kms://cartorio-kms chave-deploy@latest
  ```
