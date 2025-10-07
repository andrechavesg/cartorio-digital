# Timestamp RFC 3161

## Introdução

Ao registrar uma ata digital, o cartório aplicou um carimbo de tempo RFC 3161 fornecido por uma TSA confiável. Meses depois, uma auditoria solicitou comprovação de data e hora; bastou apresentar o arquivo carimbado e a verificação independente confirmou a autenticidade. A confiança foi reforçada.

## Conceitos Fundamentais

- **Timestamp Authority (TSA):** entidade que assina um hash com marca temporal.
- **RFC 3161:** padrão que define o protocolo para solicitação e verificação de timestamps.
- **Integridade temporal:** garante que o documento existia na data informada.
- **Integração com fluxos de assinatura:** timestamps complementam assinaturas digitais.

## Práticas Reais

1. Gere o hash de um documento e solicite carimbo de tempo usando `openssl ts` ou ferramentas equivalentes.
2. Armazene o arquivo de resposta (`.tsr`) junto ao documento original.
3. Verifique o carimbo periodicamente para garantir que a cadeia de confiança permanece válida.
4. Documente fornecedores de TSA e procedimentos de renovação.

## Próximos passos

Com timestamps dominados, avançaremos para uma prática guiada que combina assinatura e validação em fluxo completo. Na Introdução do próximo capítulo você será conduzido passo a passo.
