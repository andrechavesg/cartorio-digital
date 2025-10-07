# Implementando o KMS do Cartório

## Introdução

O cartório decidiu centralizar todas as chaves em uma arquitetura híbrida: HSM on-premises para chaves raiz e KMS em nuvem para operações diárias. Durante a inauguração do novo centro de dados, a equipe mostrou como as aplicações solicitavam chaves via API, recebiam respostas assinadas e mantinham registros detalhados. O sentimento foi de missão cumprida.

## Conceitos Fundamentais

- **Segmentação por ambientes:** separar chaves de produção, homologação e testes.
- **Integração com PKI:** KMS assina certificados, distribui chaves simétricas e registra uso.
- **Automação:** APIs e lambdas/funcões automatizam rotação e políticas.
- **Monitoramento:** métricas e logs acompanham desempenho e segurança.

## Práticas Reais

1. Desenhe um diagrama que mostre HSM, KMS, aplicações e fluxos de auditoria.
2. Configure políticas que garantam dupla aprovação para operações sensíveis (dual control).
3. Automate rotação de chaves com funções serverless ou cron jobs documentados.
4. Integre o KMS a sistemas de emissão de certificados, assinatura de documentos e criptografia de banco de dados.

## Próximos passos

Com a arquitetura operacionalizada, precisamos entender os diferenciais do hardware criptográfico dedicado. Na Introdução do próximo capítulo analisaremos como HSMs garantem proteção física e lógica incomparável.
