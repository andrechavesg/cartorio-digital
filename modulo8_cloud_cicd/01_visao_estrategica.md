# 01 · Visão estratégica da jornada em nuvem

O cartório digital percebeu que cada equipe construía pipelines e ambientes em nuvens distintas, resultando em entregas lentas e falta de confiança nas liberações. Para virar esse jogo, apresentamos GitHub Actions como motor unificado da automação e colocamos a infraestrutura como código no centro da estratégia — dois pilares que inspiram a jornada rumo a uma operação resiliente.

## Desafio

Como levar o serviço de emissão de certidões, autenticações e selos digitais para a nuvem com ciclos de entrega contínua e controles robustos? Precisamos garantir que cada push de código possa seguir um fluxo seguro até a produção, mantendo criptografia forte e auditabilidade em cada etapa.

## Estratégia orientadora

1. **Padronizar pipelines**: Definimos uma cadência única para construção, testes e implantação usando GitHub Actions, facilitando o reuso entre squads.
2. **Infraestrutura como código**: Representamos a topologia do cartório digital com Terraform para que ambientes sejam criados em minutos, e não em dias.
3. **Observabilidade integrada**: Conectamos logs, métricas e alertas para reagir rápido a qualquer ameaça à disponibilidade.

## Conceito aplicado antes da automação

Antes de automatizar, revisitamos como os módulos anteriores alimentam esta fase e validamos a arquitetura do projeto principal (`modulo10_projeto_final`):

- As práticas de certificação de `modulo2` e segurança de transporte de `modulo3` são insumos do pipeline.
- As integrações regulatórias de `modulo5` e a governança de chaves de `modulo6` definem políticas de aprovação.
- A assinatura de artefatos de `modulo7` garante confiabilidade nas imagens Docker liberadas pelo pipeline.

Somente depois de alinhar esses conceitos é que criamos o primeiro fluxo automatizado.

## Exemplo inspirador com GitHub Actions

```yaml
# .github/workflows/cartorio-ci.yml
name: Cartorio Digital CI

on:
  push:
    branches: [ "main" ]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Configurar versão do Node
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      - name: Executar testes
        run: npm test
```

Essa base permite que cada mudança de código percorra uma trilha confiável, reforçando o compromisso do cartório digital com entregas frequentes e seguras.

## Conexão com o projeto final

Ao dominar essa visão estratégica, o time consegue desenhar a esteira que sustentará a implantação completa apresentada no `modulo10_projeto_final`. Todo o ecossistema passa a falar a mesma linguagem de automação, habilitando auditorias rápidas e confiança do cidadão digital.
