# 4. Assinaturas Digitais

A assinatura digital combina criptografia assimétrica e hash para garantir **autenticidade**, **integridade** e **não repúdio**. Ela permite provar que determinada pessoa (ou serviço) assinou um documento e que o documento não foi alterado após a assinatura.

## Como funciona

1. Calcula-se o hash do documento.
2. O hash é cifrado com a **chave privada** do signatário — isso gera a assinatura.
3. Para verificar, recalcula-se o hash do documento e decifra-se a assinatura com a **chave pública**. Se os valores coincidirem, a assinatura é válida.

## Exemplo prático

1. Gere um par de chaves RSA (caso ainda não tenha).
2. Crie um arquivo `contrato.txt` com um texto qualquer.

Imagine o fluxo jurídico de uma escritura de compra e venda. Após a conferência documental, a tabeliã redige o instrumento, envia para revisão da parte interessada e agenda a sessão de assinatura. O regulamento interno determina que, antes de protocolar o título no tribunal, a versão digital deve ser assinada com a chave privada protegida no HSM e carimbada com o hash correspondente. Só depois dessa etapa é que o registro pode avançar, garantindo autenticidade e não repúdio.

Com o processo jurídico descrito em ata, o operador inicia a fase técnica e utiliza o `openssl dgst` porque o comando atende à política de assinatura e integra-se aos robôs que preparam os dossiês eletrônicos. Para emitir a assinatura criptográfica do arquivo, execute:

```bash
openssl dgst -sha256 -sign rsa_private.pem -out contrato.sig contrato.txt
```

Assim que a assinatura é emitida, o setor de controle de qualidade assume o processo jurídico. Ele precisa validar o documento antes de transmiti-lo ao órgão regulador, confirmando que nenhum ajuste foi feito entre a sessão de assinatura e o protocolo externo. Essa checagem é realizada com o mesmo `openssl dgst`, agora em modo de verificação, comparando a assinatura com a chave pública institucional e acusando qualquer tentativa de fraude. Verifique a assinatura com:

```bash
openssl dgst -sha256 -verify rsa_public.pem -signature contrato.sig contrato.txt
```

Por fim, o jurídico não encerra o processo sem confirmar a validade do certificado da tabeliã. A legislação exige que o documento esteja amparado por uma cadeia reconhecida pela ICP-Brasil e dentro do prazo de vigência. O time de compliance registra essa checagem na ata e utiliza o `openssl verify`, que analisa a cadeia de certificação antes de liberar a escritura para o protocolo externo. Verifique a cadeia de confiança do certificado da tabeliã (assumindo que `cadeia_ca.pem` contém os certificados da hierarquia):

```bash
openssl verify -CAfile cadeia_ca.pem certificado_tabelia.pem
```

Se a assinatura for válida, o comando exibirá `Verified OK`. Tente modificar `contrato.txt` e verificar novamente — a assinatura deixará de ser válida.

## Assinaturas no mundo real

- **Certificados digitais:** as autoridades certificadoras (CAs) assinam certificados X.509 com sua chave privada, permitindo que qualquer pessoa verifique sua autenticidade.
- **Código e artefatos:** sistemas como a Apple, Microsoft e repositórios de pacotes exigem assinaturas de desenvolvedores para evitar distribuições maliciosas.
- **Documentos eletrônicos:** contratos, escrituras e outros documentos são assinados digitalmente para ter validade jurídica, como veremos nos módulos posteriores.

Agora que você entende os componentes básicos — cifras, chaves, hashes e assinaturas — está pronto para avançar para a PKI e começar a emitir seus próprios certificados!
