# 02 · Pipelines do cartório digital

Neste capítulo, transformamos a visão estratégica em uma esteira viva que entrega valor diariamente. A missão do cartório digital é liberar certidões e atos com rapidez, sem abrir mão da segurança jurídica. Para isso, construímos pipelines que unem testes automatizados, assinatura de artefatos e implantação contínua.

## Cenário

Os repositórios do cartório concentram microserviços responsáveis por autenticação, registros e integrações externas. Cada alteração precisa ser validada quanto a conformidade, performance e segurança. A equipe deseja uma solução que reduza erros manuais e garanta rastreabilidade.

## Abordagem com GitHub Actions

Adotamos GitHub Actions por sua integração nativa com GitHub e por permitir workflows sofisticados com gates de aprovação. A seguir, apresentamos um pipeline que compila, testa, assina e implanta componentes do cartório.

### Configuração passo a passo

1. **Definir variáveis sensíveis**: No repositório principal, adicionamos `ACME_ACCOUNT_KEY`, `AWS_ROLE_ARN` e `SIGNING_KEY_ID` como *secrets*.
2. **Estruturar jobs em estágios**: Criamos estágios de `build`, `security`, `sign` e `deploy`, com dependências claras.
3. **Registrar artefatos**: Utilizamos GitHub Packages e Amazon ECR para armazenar imagens e assinaturas.

### Exemplo guiado

```yaml
# .github/workflows/cartorio-delivery.yml
name: Cartorio Delivery Pipeline

on:
  workflow_dispatch:
  push:
    branches: ["main"]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Instalar dependências
        run: npm ci
      - name: Executar testes unitários
        run: npm test
      - name: Publicar relatórios
        uses: actions/upload-artifact@v4
        with:
          name: relatorios-testes
          path: coverage/

  security:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Análise SCA
        uses: github/codeql-action/analyze@v3
      - name: Escanear contêiner
        run: |
          trivy image --exit-code 1 ghcr.io/cartorio-digital/api:latest

  sign:
    needs: security
    runs-on: ubuntu-latest
    steps:
      - name: Baixar imagem
        run: docker pull ghcr.io/cartorio-digital/api:latest
      - name: Assinar artefato com Sigstore
        run: cosign sign --key ${{ secrets.SIGNING_KEY_ID }} ghcr.io/cartorio-digital/api:latest

  deploy:
    needs: sign
    runs-on: ubuntu-latest
    environment:
      name: producao
      url: https://api.cartorio.digital
    steps:
      - name: Configurar credenciais AWS
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: us-east-1
      - name: Implantar com CDK
        run: npx cdk deploy CartorioStack --require-approval never
```

Cada bloco é precedido por revisões manuais quando necessário, reforçando governança. O pipeline produz trilhas auditáveis que alimentam a área de conformidade, conectando com os requisitos vistos no `modulo5_regulatorio`.

## Relacionamento com o projeto principal

As mesmas etapas acima alimentam o *blueprint* do `modulo10_projeto_final`, onde consolidaremos a automação ponta a ponta — do commit à emissão de certidões em ambiente multirregional.
