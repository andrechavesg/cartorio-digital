# Assinatura de Artefatos Digitais

## Introdução

Antes de publicar uma nova versão do sistema de protocolo, o cartório assinou todos os pacotes e contêineres. Ao executar a validação nos servidores de produção, perceberam que qualquer alteração indevida seria detectada imediatamente. O lançamento ocorreu com tranquilidade e confiança.

## Conceitos Fundamentais

- **Assinatura de código:** garante autenticidade de executáveis e bibliotecas.
- **Assinatura de contêiner:** envolve manifestos (Cosign, Notary) e verificação de imagens.
- **Hash e manifesto:** listas de arquivos e digests evitam alterações não autorizadas.
- **Distribuição segura:** repositórios devem exigir verificação de assinatura antes do deploy.

## Práticas Reais

1. Assine um pacote ou binário com GPG, Codesign ou ferramenta equivalente.
2. Utilize Cosign ou Notary para assinar imagens de contêiner e configure políticas de admissão no Kubernetes.
3. Gere manifestos de hash para diretórios críticos e acompanhe mudanças via pipeline.
4. Documente o processo para que equipes de operações possam validar artefatos antes do deploy.

## Próximos passos

Com os artefatos assinados, precisamos garantir validade temporal. Na Introdução do próximo capítulo exploraremos timestamps RFC 3161, guiados por um caso de preservação de provas digitais.
