# Sumário do Módulo – Assinatura de Artefatos

## Introdução

Ao preparar a primeira release do sistema do cartório, a equipe percebeu que cada componente — documentos, APIs, aplicativos — precisava de assinatura digital para garantir confiança. Eles mapearam todos os artefatos e criaram um plano integrado. A clareza desse sumário guiou o restante do módulo.

## Conceitos Fundamentais

- **Tipos de assinatura:** PADES, CADES, XAdES, assinaturas de código e containers.
- **Fluxos de timestamp e relatórios:** garantem validade temporal e rastreabilidade.
- **Integrações via API:** permitem que sistemas consumam assinaturas de forma automatizada.
- **Testes e desafios:** asseguram que as implementações funcionem em cenários reais.

## Práticas Reais

1. Faça um inventário de todos os artefatos que o cartório produz (PDFs, XMLs, binários, contêineres).
2. Associe cada artefato a um tipo de assinatura apropriado.
3. Defina responsáveis por emissão, validação e distribuição dos artefatos assinados.
4. Monte um cronograma para implantação gradual do módulo.

## Gancho para o Próximo Capítulo

Com a visão geral em mãos, vamos iniciar pelo entendimento dos tipos de assinatura disponíveis. Na Introdução do próximo capítulo entenderemos como escolher o formato certo para cada necessidade.
