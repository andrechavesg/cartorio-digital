# Orquestração Tecnológica do Cartório Digital

## Problema vivido pelo cartório digital

A plataforma enfrenta fricções entre times de aplicação, segurança e infraestrutura: pipelines concorrentes, configurações duplicadas e ausência de um maestro tecnológico que alinhe infraestrutura como código, segurança e compliance.

## Conexão inspiradora com os módulos anteriores

Os manifestos automatizados do **Módulo 4**, as políticas de chaves do **Módulo 6** e o pipeline cloud-native do **Módulo 8** compõem a base para um arranjo harmonioso. O aprendizado em assinatura de artefatos do **Módulo 7** e as métricas do **Módulo 9** garantem rastreabilidade e feedback contínuo.

## Ferramentas e comandos como solução

- **Orquestração central com GitOps**
  ```bash
  flux bootstrap github \
    --owner cartorio-digital \
    --repository infraestrutura \
    --branch main \
    --path clusters/producao
  ```
- **Automação de políticas com Open Policy Agent**
  ```bash
  conftest test manifests/ --policy policies/
  ```
- **Coordenação de secrets e HSM virtual**
  ```bash
  kmsctl sync --config configs/kms/producao.yaml
  softhsm2-util --show-slots
  ```
- **Plano de capacidade e escalonamento**
  ```bash
  kubectl describe hpa cartorio-api -n cartorio
  k6 run testes/performance/producao.js
  ```
