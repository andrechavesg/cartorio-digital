# 2. Criptografia Simétrica e Assimétrica

Neste capítulo, diferenciamos os dois principais tipos de criptografia usados no cotidiano.

## Criptografia Simétrica

Na criptografia simétrica, **a mesma chave é usada para cifrar e decifrar**. É muito rápida e ideal para volumes grandes de dados, porém exige que as partes compartilhem a chave com segurança.

Algoritmos populares:
- **AES (Advanced Encryption Standard)** nas variantes 128, 192 e 256 bits.
- **ChaCha20** com Poly1305, usado em dispositivos móveis e no TLS 1.3.

### Exemplo prático

Crie um arquivo de texto chamado `mensagem.txt` com um conteúdo qualquer. Para cifrar usando AES‑256‑CBC:

```bash
openssl enc -aes-256-cbc -salt -in mensagem.txt -out mensagem.enc
```

O comando solicitará uma senha (que atua como chave). Para decifrar:

```bash
openssl enc -d -aes-256-cbc -in mensagem.enc -out mensagem.dec
```

Compare `mensagem.txt` e `mensagem.dec` para confirmar que o conteúdo foi recuperado.

## Criptografia Assimétrica

A criptografia assimétrica utiliza **um par de chaves**: uma chave pública e uma chave privada. A pública pode ser compartilhada livremente; a privada deve ser mantida em segredo. É mais lenta e geralmente utilizada para trocar chaves simétricas (como no TLS) ou assinar dados.

Algoritmos populares:
- **RSA**, baseado em fatoração de inteiros.
- **ECC (Elliptic Curve Cryptography)** como P‑256 e Curve25519.
- **Ed25519**, conhecido por ser rápido e seguro.

### Exemplo prático

Gere um par de chaves RSA de 2048 bits:

```bash
openssl genpkey -algorithm RSA -out rsa_private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in rsa_private.pem -out rsa_public.pem
```

O arquivo `rsa_private.pem` contém a chave privada; `rsa_public.pem` contém a chave pública.

Também é possível gerar uma chave Elliptic Curve (P‑256):

```bash
openssl genpkey -algorithm EC -out ec_private.pem -pkeyopt ec_paramgen_curve:P-256
openssl pkey -pubout -in ec_private.pem -out ec_public.pem
```

### Usando as chaves

- Qualquer pessoa pode criptografar uma mensagem com a **chave pública**; somente o detentor da **chave privada** pode decifrar.
- Para compartilhar um segredo, você poderia gerar uma chave simétrica aleatória, cifrá‑la com a chave pública do destinatário e enviar os dois — essa é a base do TLS.

Experimente cifrar um arquivo pequeno com a chave pública RSA e depois decifrar com a chave privada; compare o desempenho com o algoritmo simétrico.
