# 4. Assinaturas Digitais

A assinatura digital combina criptografia assimétrica e hash para garantir **autenticidade**, **integridade** e **não repúdio**. Ela permite provar que determinada pessoa (ou serviço) assinou um documento e que o documento não foi alterado após a assinatura.

## Como funciona

1. Calcula-se o hash do documento.
2. O hash é cifrado com a **chave privada** do signatário — isso gera a assinatura.
3. Para verificar, recalcula-se o hash do documento e decifra-se a assinatura com a **chave pública**. Se os valores coincidirem, a assinatura é válida.

## Exemplo prático

1. Gere um par de chaves RSA (caso ainda não tenha).
2. Crie um arquivo `contrato.txt` com um texto qualquer.

No cartório digital, cada escritura eletrônica precisa ser assinada pela tabeliã responsável antes de seguir para o registro no tribunal. A validação automatizada precisa combinar o documento com a chave privada armazenada no HSM, gerando um comprovante que possa ser arquivado junto ao termo. O comando `openssl dgst` é empregado porque permite aplicar o algoritmo exigido na política de assinatura e integra-se aos processos já utilizados nas rotinas notariais.

Para emitir a assinatura criptográfica do arquivo, execute:

```bash
openssl dgst -sha256 -sign rsa_private.pem -out contrato.sig contrato.txt
```

Após a emissão, outro setor do cartório valida o documento antes de enviá-lo ao órgão regulador. Essa checagem é essencial para garantir que nenhuma edição foi feita entre a assinatura e o protocolo externo. O `openssl dgst` em modo de verificação compara a assinatura com a chave pública institucional e acusa qualquer tentativa de fraude.

Verifique a assinatura com:

```bash
openssl dgst -sha256 -verify rsa_public.pem -signature contrato.sig contrato.txt
```

Além do conteúdo assinado, o cartório também precisa confirmar que o certificado usado pela tabeliã está dentro do prazo de validade e encadeado a uma autoridade reconhecida pela ICP-Brasil. Para isso, o time de compliance utiliza o `openssl verify`, que analisa a cadeia de certificação antes de liberar a escritura para o protocolo externo.

Verifique a cadeia de confiança do certificado da tabeliã (assumindo que `cadeia_ca.pem` contém os certificados da hierarquia):

```bash
openssl verify -CAfile cadeia_ca.pem certificado_tabelia.pem
```

Se a assinatura for válida, o comando exibirá `Verified OK`. Tente modificar `contrato.txt` e verificar novamente — a assinatura deixará de ser válida.

## Assinaturas no mundo real

- **Certificados digitais:** as autoridades certificadoras (CAs) assinam certificados X.509 com sua chave privada, permitindo que qualquer pessoa verifique sua autenticidade.
- **Código e artefatos:** sistemas como a Apple, Microsoft e repositórios de pacotes exigem assinaturas de desenvolvedores para evitar distribuições maliciosas.
- **Documentos eletrônicos:** contratos, escrituras e outros documentos são assinados digitalmente para ter validade jurídica, como veremos nos módulos posteriores.

Agora que você entende os componentes básicos — cifras, chaves, hashes e assinaturas — está pronto para avançar para a PKI e começar a emitir seus próprios certificados!
