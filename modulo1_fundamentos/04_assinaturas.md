# 4. Assinaturas Digitais

A assinatura digital combina criptografia assimétrica e hash para garantir **autenticidade**, **integridade** e **não repúdios**. Ela permite provar que determinada pessoa (ou serviço) assinou um documento e que o documento não foi alterado após a assinatura.

## Como funciona

1. Calcula‑se o hash do documento.
2. O hash é cifrado com a **chave privada** do signatário — isso gera a assinatura.
3. Para verificar, recalcula‑se o hash do documento e decifra‑se a assinatura com a **chave pública**. Se os valores coincidirem, a assinatura é válida.

## Exemplo prático

1. Gere um par de chaves RSA (caso ainda não tenha).
2. Crie um arquivo `contrato.txt` com um texto qualquer.
3. Assine o arquivo:

```bash
openssl dgst -sha256 -sign rsa_private.pem -out contrato.sig contrato.txt
```

4. Verifique a assinatura:

```bash
openssl dgst -sha256 -verify rsa_public.pem -signature contrato.sig contrato.txt
```

Se a assinatura for válida, o comando exibirá `Verified OK`. Tente modificar `contrato.txt` e verificar novamente — a assinatura deixará de ser válida.

## Assinaturas no mundo real

- **Certificados digitais:** as autoridades certificadoras (CAs) assinam certificados X.509 com sua chave privada, permitindo que qualquer pessoa verifique sua autenticidade.
- **Código e artefatos:** sistemas como a Apple, Microsoft e repositórios de pacotes exigem assinaturas de desenvolvedores para evitar distribuições maliciosas.
- **Documentos eletrônicos:** contratos, escrituras e outros documentos são assinados digitalmente para ter validade jurídica, como veremos nos módulos posteriores.

Agora que você entende os componentes básicos — cifras, chaves, hashes e assinaturas — está pronto para avançar para a PKI e começar a emitir seus próprios certificados!
