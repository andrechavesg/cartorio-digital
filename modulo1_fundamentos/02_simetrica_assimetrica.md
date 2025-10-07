# 2. Criptografia Simétrica e Assimétrica

Neste capítulo, diferenciamos os dois principais tipos de criptografia usados no cotidiano.

## Criptografia Simétrica

Na criptografia simétrica, **a mesma chave é usada para cifrar e decifrar**. É muito rápida e ideal para volumes grandes de dados, porém exige que as partes compartilhem a chave com segurança.

Algoritmos populares:
- **AES (Advanced Encryption Standard)** nas variantes 128, 192 e 256 bits.
- **ChaCha20** com Poly1305, usado em dispositivos móveis e no TLS 1.3.

### Exemplo prático

Durante os mutirões mensais, o cartório exporta lotes inteiros de certidões digitais para a central estadual. Antes de subir os pacotes para o repositório temporário, a coordenadora de TI descreve no plano de contingência o risco de vazamento dessas certidões enquanto aguardam validação. Para mitigar a dor, ela decide blindar a pasta com uma cifra simétrica aprovada pelo comitê de segurança, garantindo que só os analistas designados consigam abrir o conteúdo arquivado.

Somente depois de registrar o cenário e obter a senha compartilhada, o time executa o comando OpenSSL que aplica a proteção:

```bash
openssl enc -aes-256-cbc -salt -in mensagem.txt -out mensagem.enc
```

Semanas depois, a corregedoria solicita uma conferência aleatória do lote. O mesmo time segue o procedimento documentado para restaurar os arquivos, coletando logs de acesso para manter a rastreabilidade:

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

Em outro cenário, uma unidade distrital precisa enviar dados sensíveis para a sede regional a fim de atender um pedido judicial urgente. A diretoria de tecnologia convoca uma reunião rápida para revisar o protocolo de troca segura de chaves, pois não pode correr o risco de um intermediário interceptar as credenciais. O procedimento aprovado inicia com a geração do par de chaves institucional.

Somente após registrar o incidente e autorizar a emissão, a equipe dispara os comandos abaixo para gerar a chave RSA de 2048 bits e disponibilizar a parte pública às demais unidades:

```bash
openssl genpkey -algorithm RSA -out rsa_private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in rsa_private.pem -out rsa_public.pem
```

O arquivo `rsa_private.pem` contém a chave privada; `rsa_public.pem` contém a chave pública.

Mais tarde, ao automatizar a assinatura de XMLs entre sistemas internos do cartório, a equipe percebe que a opção RSA deixa a fila de processamento lenta. Eles registram o problema em ata e decidem migrar para curvas elípticas aprovadas pela ICP-Brasil, ganhando desempenho sem comprometer a conformidade.

Com a decisão formalizada, o analista responsável executa os comandos a seguir para gerar a chave Elliptic Curve (P-256) e publicar a parte pública para o sistema automatizado:

```bash
openssl genpkey -algorithm EC -out ec_private.pem -pkeyopt ec_paramgen_curve:P-256
openssl pkey -pubout -in ec_private.pem -out ec_public.pem
```

### Usando as chaves

- Qualquer pessoa pode criptografar uma mensagem com a **chave pública**; somente o detentor da **chave privada** pode decifrar.
- Para compartilhar um segredo, você poderia gerar uma chave simétrica aleatória, cifrá-la com a chave pública do destinatário e enviar os dois — essa é a base do TLS.

Experimente cifrar um arquivo pequeno com a chave pública RSA e depois decifrar com a chave privada; compare o desempenho com o algoritmo simétrico.
