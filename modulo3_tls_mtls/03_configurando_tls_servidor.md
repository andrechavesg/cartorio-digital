# Configurando TLS 1.3 em um Servidor

## Exemplo Inspirador

Ao ativar o novo site do cartório, a equipe realizou um ritual simbólico: com todos reunidos, o administrador aplicou a configuração de TLS 1.3 e reiniciou o Nginx. Em seguida, o time jurídico acessou o portal e viu o cadeado verde, acompanhado de um relatório automático confirmando o uso da nossa cadeia interna. Esse instante consolidou a percepção de que tecnologia e confiança caminham lado a lado.

## Conceitos Fundamentais

- **Cadeia completa:** servidor deve apresentar certificado, chave privada e cadeia intermediária.
- **Protocolos e cifras:** restringir versões a TLS 1.3 e ciphers modernos.
- **Segurança adicional:** HSTS, OCSP stapling e redirecionamento HTTP→HTTPS fortalecem a experiência.
- **Automação:** scripts de deploy garantem consistência e reduzem erro humano.

## Práticas Reais

1. **Prepare os arquivos necessários:**
   - `cartorio.local.cert.pem` – certificado do servidor.
   - `server.key.pem` – chave privada correspondente.
   - `ca-chain.cert.pem` – cadeia raiz + intermediária.

2. **Configure o Nginx com TLS 1.3:**
   ```nginx
   server {
       listen 443 ssl http2;
       server_name cartorio.local;

       ssl_certificate     /etc/nginx/certs/cartorio.local.cert.pem;
       ssl_certificate_key /etc/nginx/certs/server.key.pem;
       ssl_trusted_certificate /etc/nginx/certs/ca-chain.cert.pem;

       ssl_protocols TLSv1.3;
       ssl_ciphers   TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256;
       ssl_prefer_server_ciphers off;

       add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
       ssl_stapling on;
       ssl_stapling_verify on;

       root /var/www/html;
   }
   ```
   Documente onde os arquivos foram armazenados e quais permissões de sistema protegem as chaves.

3. **Teste a conexão:**
   ```bash
   curl -I https://cartorio.local --cacert ca-chain.cert.pem
   openssl s_client -connect cartorio.local:443 -tls1_3
   ```
   Registre capturas de tela ou logs mostrando a negociação bem-sucedida.

4. **Automatize verificações de configuração:** integre ferramentas como `sslscan` ou `testssl.sh` na pipeline para detectar regressões.

## Próximos passos

Com o servidor seguro, precisamos garantir que apenas clientes autorizados acessem as APIs confidenciais. No próximo capítulo ativaremos mTLS, começando por um exemplo inspirador de cooperação entre equipes técnicas e de atendimento do cartório.
