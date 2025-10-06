# 1. Objetivo e Inspiração

Neste capítulo inicial, vamos explorar por que a criptografia é a base de todo o projeto de cartório digital. O objetivo é inspirar você a enxergar além dos algoritmos e perceber como o uso correto da criptografia permite transformar serviços públicos, garantindo **segurança**, **confidencialidade**, **autenticidade** e **integridade**. Sem esses pilares, um cartório digital seria apenas um site sem valor jurídico.

## Por que estudar criptografia?

- **Confidencialidade:** Imagine que todos os documentos dos cartórios fossem transmitidos na internet sem proteção. Qualquer pessoa poderia interceptá‑los. A criptografia protege o conteúdo para que somente os participantes legítimos possam lê‑lo.
- **Autenticidade:** É necessário saber quem gerou ou assinou um documento. Usamos criptografia assimétrica e certificados digitais para garantir que somente o titular da chave privada possa assinar.
- **Integridade:** Mesmo que um documento seja confidencial, ele pode ser alterado por terceiros. Funções hash e assinaturas digitais permitem detectar qualquer modificação.

## Conectando com o projeto do cartório digital

Ao longo do curso construiremos um cartório digital completo. Neste módulo, você aprenderá a manipular chaves e entender como os algoritmos funcionam na prática. Nos próximos capítulos, utilizaremos esses conhecimentos para emitir certificados X.509, proteger conexões (TLS/mTLS), automatizar emissões (ACME) e assinar documentos.

## Exemplo inicial

Para começar, verifique a versão do OpenSSL instalada no seu ambiente:

```bash
openssl version
```

Se não tiver o OpenSSL instalado, consulte a documentação de instalação para o seu sistema operativo. Esse utilitário será usado em todos os capítulos.

Outra ação prática é inspecionar o certificado do seu próprio repositório GitHub. No navegador, clique no cadeado da barra de endereço e examine as informações do certificado TLS — repare na cadeia de confiança e na assinatura digital.

Agora que você sabe por que está aqui, vamos mergulhar nas técnicas!
