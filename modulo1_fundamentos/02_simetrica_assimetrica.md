# 2. Criptografia Simétrica e Assimétrica

Neste capítulo, diferenciamos os dois principais tipos de criptografia usados no cotidiano.

## Criptografia Simétrica

Na criptografia simétrica, **a mesma chave é usada para cifrar e decifrar**. É muito rápida e ideal para volumes grandes de dados, porém exige que as partes compartilhem a chave com segurança.

Algoritmos populares:
- **AES (Advanced Encryption Standard)** nas variantes 128, 192 e 256 bits.
- **ChaCha20** com Poly1305, usado em dispositivos móveis e no TLS 1.3.

### Exemplo prático

Quando o cartório exporta lotes inteiros de certidões digitais para uma central estadual, a dor é manter essas remessas protegidas enquanto estão armazenadas no servidor de homologação. A equipe de infraestrutura recorre a uma cifra simétrica aprovada pelo comitê de segurança para que apenas usuários autorizados possam abrir os arquivos arquivados.

Para blindar o pacote com AES-256-CBC e garantir que apenas quem possui a senha consiga reprocessá-lo, utilize:

```bash
openssl enc -aes-256-cbc -salt -in mensagem.txt -out mensagem.enc
```

Quando for necessário recuperar o conteúdo para uma conferência da corregedoria, o mesmo time precisa reverter a criptografia de forma íntegra, mantendo a rastreabilidade do acesso:

```bash
openssl enc -d -aes-256-cbc -in mensagem.enc -out mensagem.dec
```

Compare `mensagem.txt` e `mensagem.dec` para confirmar que o conteúdo foi recuperado.

## Criptografia Assimétrica

A criptografia assimétrica utiliza **um par de chaves**: uma chave pública e uma chave privada. A pública pode ser compartilhada livremente; a privada deve ser mantida em segredo. É mais lenta e geralmente utilizada para trocar chaves simétricas (como no TLS) ou assinar dados.

Algoritmos populares:
- **RSA**, baseado em fatoração de inteiros.
- **ECC (Elliptic Curve Cryptography)** como P-256 e Curve25519.
- **Ed25519**, conhecido por ser rápido e seguro.

### Exemplo prático

Sempre que uma unidade do cartório precisa enviar dados sensíveis para outra unidade em outra cidade, o comitê de TI exige a troca segura de chaves para que nenhum intermediário consiga interceptar as credenciais. O processo começa com a geração do par de chaves institucional.

Para gerar uma chave RSA de 2048 bits e disponibilizar a parte pública às demais unidades, execute:

```bash
openssl genpkey -algorithm RSA -out rsa_private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in rsa_private.pem -out rsa_public.pem
```

O arquivo `rsa_private.pem` contém a chave privada; `rsa_public.pem` contém a chave pública.

Quando a unidade precisa de chaves menores e mais rápidas para autenticação de serviços internos (como assinaturas automáticas de XMLs), o time escolhe curvas elípticas aprovadas pela ICP-Brasil para reduzir o tempo de processamento.

Para gerar uma chave Elliptic Curve (P-256) e compartilhar a chave pública com o sistema automatizado, use:

```bash
openssl genpkey -algorithm EC -out ec_private.pem -pkeyopt ec_paramgen_curve:P-256
openssl pkey -pubout -in ec_private.pem -out ec_public.pem
```

### Usando as chaves

- Qualquer pessoa pode criptografar uma mensagem com a **chave pública**; somente o detentor da **chave privada** pode decifrar.
- Para compartilhar um segredo, você poderia gerar uma chave simétrica aleatória, cifrá-la com a chave pública do destinatário e enviar os dois — essa é a base do TLS.

Experimente cifrar um arquivo pequeno com a chave pública RSA e depois decifrar com a chave privada; compare o desempenho com o algoritmo simétrico.
