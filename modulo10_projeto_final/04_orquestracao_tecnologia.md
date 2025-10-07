# Orquestração Tecnológica do Cartório Digital

## Problema vivido pelo cartório digital

A plataforma enfrenta fricções entre times de aplicação, segurança e infraestrutura: pipelines concorrentes, configurações duplicadas e ausência de um maestro tecnológico que alinhe infraestrutura como código, segurança e compliance.

## Conexão inspiradora com os módulos anteriores

Os manifestos automatizados do **Módulo 4**, as políticas de chaves do **Módulo 6** e o pipeline cloud-native do **Módulo 8** compõem a base para um arranjo harmonioso. O aprendizado em assinatura de artefatos do **Módulo 7** e as métricas do **Módulo 9** garantem rastreabilidade e feedback contínuo.

## Ferramentas e comandos como solução

- **Orquestração central com GitOps** – para pacificar disputas entre configurações divergentes, mostre como o bootstrap consolida repositório, branch e caminho do cluster do projeto principal.
  ```bash
  # Por que: instalar o controlador GitOps usando o repositório institucional aprovado.
  flux bootstrap github \
    --owner cartorio-digital \
    --repository infraestrutura \
    --branch main \
    --path clusters/producao
  ```
- **Automação de políticas com Open Policy Agent** – quando surgir uma mudança crítica em manifests, execute os testes de políticas antes do merge e evidencie que segurança e compliance caminham juntos.
  ```bash
  # Por que: validar que cada manifesto respeita as políticas definidas pelos módulos regulatórios.
  conftest test manifests/ --policy policies/
  ```
- **Coordenação de secrets e HSM virtual** – para garantir que a rotação de chaves siga o orquestrador central, sincronize as configurações do KMS e valide os slots do HSM utilizado nos módulos de assinatura.
  ```bash
  # Por que: alinhar o inventário de chaves com a configuração centralizada do KMS.
  kmsctl sync --config configs/kms/producao.yaml
  # Por que: confirmar que os slots do HSM estão disponíveis para novas emissões.
  softhsm2-util --show-slots
  ```
- **Plano de capacidade e escalonamento** – em reuniões de preparação para picos de demanda, use os comandos de HPA e testes de carga para embasar a tomada de decisão.
  ```bash
  # Por que: inspecionar regras de escalonamento e comprovar que protegem o atendimento ao cidadão.
  kubectl describe hpa cartorio-api -n cartorio
  # Por que: exercitar o ambiente com o mesmo perfil de carga exigido pela corregedoria.
  k6 run testes/performance/producao.js
  ```
