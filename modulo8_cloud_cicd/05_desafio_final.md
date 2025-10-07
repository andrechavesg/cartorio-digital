# 05 · Desafio final do módulo

Para coroar o módulo de Cloud e CI/CD, propomos um desafio que conecta cada peça construída até aqui. É a oportunidade de demonstrar que o cartório digital está pronto para operar em escala nacional, com pipelines confiáveis, infraestrutura resiliente e observabilidade ativa.

## Objetivo do desafio

Construir uma release completa do cartório digital, desde o commit até a publicação em ambiente de produção, incluindo monitoramento pós-implantação e documentação das decisões. O resultado deve provar que o time domina automações seguras e pode responder rapidamente a incidentes.

## Entregáveis esperados

1. **Pipeline orquestrado** que inclua build, testes, segurança, assinatura, provisionamento e deploy.
2. **Infraestrutura como código** com Terraform ou AWS CDK versionado e acompanhado de *pipelines* de `plan` e `apply`.
3. **Stack de observabilidade** com alertas de expiração de certificados e falhas de deploy.
4. **Relatório executivo** apresentando ganhos de velocidade, confiabilidade e conformidade.

## Roteiro sugerido

1. **Preparar o repositório**
   - Revisar *secrets*, variáveis e branch protection rules.
   - Conectar repositórios de módulos anteriores para reutilizar componentes (scripts de assinatura, templates Terraform, dashboards).
2. **Executar o pipeline completo**
   - Disparar o workflow `Cartorio Delivery Pipeline` com uma alteração real (ex.: atualização de schema de certidão).
   - Registrar evidências de cada etapa (logs, artefatos, prints de dashboards).
3. **Validar operação em produção**
   - Usar scripts do `modulo3_tls_mtls` para confirmar que certificados foram renovados.
   - Monitorar alertas no Grafana e responder a uma simulação de incidente.
4. **Apresentar lições aprendidas**
   - Documentar como os controles do `modulo5_regulatorio` foram atendidos.
   - Sugerir melhorias para o `modulo10_projeto_final`.

## Inspiração para a apresentação final

Encerre com uma demo onde o pipeline é acionado ao vivo, o Terraform cria recursos e o dashboard confirma a saúde do ambiente. Mostre como cada módulo contribuiu para um ecossistema confiável, reforçando que o cartório digital está apto a proteger a cidadania digital do país.
