# TLS 1.3 – Conceitos e Handshake

O TLS (Transport Layer Security) é o protocolo de camada de transporte usado para proteger conexões HTTPS e muitos outros serviços. A versão 1.3 simplificou o handshake e eliminou algoritmos inseguros das versões anteriores. As principais mudanças incluem:

- **Redução de ida e volta**: o handshake completo requer apenas 1‑RTT (uma única viagem de ida e volta), acelerando o estabelecimento da sessão.
- **Forward secrecy obrigatória**: todo handshake usa um algoritmo de troca de chaves efêmero (ECDHE), garantindo que a captura da chave privada do servidor no futuro não permita descriptografar sessões passadas.
- **Remoção de ciphers antigos**: suites com RC4, 3DES, AES‑CBC e handshake RSA foram removidas. Apenas AEAD (GCM ou ChaCha20‑Poly1305) e SHA‑2 são permitidos.
- **0‑RTT**: opcionalmente o cliente pode enviar dados no primeiro voo (early data), melhorando latência, mas com risco de replay.

O fluxo de mensagens no handshake 1.3 é resumido a seguir:

1. **ClientHello**: o cliente anuncia a versão TLS suportada, ciphers preferidos e envia sua `key_share` (ponto ECC para ECDHE) e o `pre_shared_key` se retomando sessão.
2. **ServerHello**: o servidor escolhe a versão/cipher, devolve sua `key_share` e inicia a derivação de chaves. A partir daqui as mensagens seguintes já são protegidas.
3. **EncryptedExtensions**: o servidor envia extensões (ALPN, SNI) dentro do canal já criptografado.
4. **Certificate**: o servidor envia seu certificado (cadeia completa) e prova de posse da chave (`CertificateVerify`).
5. **Finished**: cada lado envia um hash de todas as mensagens anteriores para confirmar a integridade e finalizar o handshake.

Todas as chaves de aplicação são derivadas com HKDF com base nos segredos efêmeros. Assim, cada sessão é criptograficamente independente.

### Listando suites TLS 1.3 suportadas

Use `openssl` para listar as suites da versão 1.3:

```bash
openssl ciphers -v -tls1_3
```

Isto deve retornar suites como:

```
TLS_AES_256_GCM_SHA384      TLSv1.3 Kx=any   Au=any Enc=AESGCM(256) Mac=AEAD
TLS_AES_128_GCM_SHA256      TLSv1.3 Kx=any   Au=any Enc=AESGCM(128) Mac=AEAD
TLS_CHACHA20_POLY1305_SHA256 TLSv1.3 Kx=any   Au=any Enc=CHACHA20_POLY1305 Mac=AEAD
```

### Testando um handshake TLS 1.3

É possível observar o handshake usando `openssl s_client`:

```bash
# Conectar ao site do seu cartório (quando configurado)
openssl s_client -connect localhost:443 -tls1_3 -servername localhost -showcerts

# Para analisar as mensagens do handshake, use:
openssl s_client -connect google.com:443 -tls1_3 -tlsextdebug -msg
```

No output, repare nas mensagens `ClientHello`, `ServerHello`, `EncryptedExtensions`, `Certificate`, `CertificateVerify` e `Finished`. Esses comandos ajudam a visualizar o fluxo e a cadeia de certificados enviada pelo servidor.

Nos próximos capítulos você irá usar estes conceitos para configurar um servidor real com TLS 1.3 e, em seguida, habilitar mTLS.
