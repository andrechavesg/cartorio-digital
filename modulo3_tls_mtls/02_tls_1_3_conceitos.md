# TLS 1.3 – Conceitos e Handshake

## Exemplo Inspirador

Durante o lançamento da consulta de certidões on-line, notamos que os cidadãos aguardavam ansiosos pelo carregamento inicial. Após habilitar TLS 1.3 com 0-RTT para clientes conhecidos, a primeira página passou a responder quase instantaneamente. A equipe celebrou: o protocolo moderno não só protegia a comunicação, como também entregava agilidade percebida por quem mais importa — o usuário final.

## Conceitos Fundamentais

- **Handshake simplificado:** apenas uma ida e volta (1-RTT) para estabelecer a sessão, com suporte a 0-RTT para clientes que retornam.
- **Cifradores modernos:** AES-GCM e ChaCha20-Poly1305 com Perfect Forward Secrecy via curvas elípticas (X25519, P-256).
- **Chaves efêmeras:** cada sessão utiliza chaves temporárias, limitando o impacto de vazamentos futuros.
- **Extensões essenciais:** SNI para indicar o domínio, ALPN para negociar protocolos de aplicação e OCSP stapling para status de certificados.

## Práticas Reais

1. **Observe o handshake com `openssl s_client`:**
   ```bash
   openssl s_client -connect cartorio.local:443 -tls1_3 -msg
   ```
   Analise as mensagens ClientHello e ServerHello, confirmando o uso de chaves efêmeras.

2. **Compare tempos com 1.2 vs 1.3:** execute testes de latência entre um servidor de homologação configurado com TLS 1.2 e outro com TLS 1.3, documentando os ganhos percebidos.

3. **Liste os cifradores habilitados:**
   ```bash
   openssl ciphers -v 'TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256'
   ```
   Garanta que apenas conjuntos modernos estejam presentes na configuração do servidor.

4. **Planeje suporte a 0-RTT com cautela:** avalie quais requisições podem ser reaplicadas com segurança e documente proteções contra replay.

## Próximos passos

Compreendido o handshake, chegou a hora de colocá-lo em produção. No próximo capítulo configuraremos um servidor web do cartório com TLS 1.3, iniciando pela cadeia de certificados que você emitiu e terminando com testes inspiradores de acesso seguro.
