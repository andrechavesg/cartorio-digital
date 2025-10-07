# 05 · Desafio final do módulo

Mesmo com automações implementadas, o cartório digital identificou que nem todos os squads conseguiam orquestrar uma entrega completa em nuvem. Para resolver essa lacuna, propomos um desafio final que obriga a recombinar pipelines GitHub Actions, Terraform e observabilidade em uma narrativa única.

## Objetivo do desafio

Construir uma release completa do cartório digital, desde o commit até a publicação em ambiente de produção, incluindo monitoramento pós-implantação e documentação das decisões. O resultado deve provar que o time domina automações seguras e pode responder rapidamente a incidentes.

## Conceito: jornada integrada

Antes de iniciar, o time revisita os capítulos anteriores e o `modulo10_projeto_final` para entender como cada componente se encaixa. O desafio é menos sobre scripts isolados e mais sobre contar uma história coerente de entrega contínua.

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
