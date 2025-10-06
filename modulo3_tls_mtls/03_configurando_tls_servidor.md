# Configurando TLS 1.3 em um Servidor

Com os certificados emitidos pela sua CA (módulo 2) e o entendimento dos conceitos de TLS 1.3, você está pronto para proteger um serviço web. Neste capítulo vamos configurar o Nginx (ou outro servidor) para oferecer TLS 1.3 com a cadeia completa de certificação.

## Preparando a cadeia de certificados

Reúna os seguintes arquivos:

- `server.crt`: o certificado do servidor assinado pela CA intermediária.
- `server.key`: a chave privada do servidor.
- `intermediate_ca.crt`: o certificado da CA intermediária.
- `root_ca.crt`: o certificado da CA raiz (opcional para clientes, mas útil para teste local).

Crie o *fullchain* concatenando o certificado do servidor com o da intermediária:

```bash
cat server.crt intermediate_ca.crt > fullchain.pem
```

## Configurando o Nginx

No arquivo de configuração (`nginx.conf` ou um `server` dentro de `sites-available`), adicione um `server` ouvindo na porta 443:

```nginx
server {
    listen 443 ssl http2;
    server_name cartorio.local;

    ssl_certificate      /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key  /etc/nginx/certs/server.key;

    # Usar somente TLS 1.3
    ssl_protocols       TLSv1.3;
    # Suites permitidas (TLS 1.3 ignora esta diretiva, mas mantemos para retrocompatibilidade)
    ssl_ciphers         TLS_AES_256_GCM_SHA384:TLS_AES_128_GCM_SHA256:TLS_CHACHA20_POLY1305_SHA256;

    # Curvas ECDH para PFS (X25519 e secp384r1 são recomendadas)
    ssl_ecdh_curve      X25519:secp384r1;

    # Opcional: Habilitar stapling OCSP (detalhado no capítulo 5)
    ssl_stapling on;
    ssl_stapling_verify on;
    resolver 1.1.1.1 8.8.8.8 valid=300s;

    # Restante das diretivas do seu site...
    location / {
        root /var/www/cartorio;
        index index.html;
    }
}
```

Recarregue o Nginx para aplicar as mudanças:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### Testando a configuração

Verifique se o servidor está ouvindo com TLS 1.3:

```bash
openssl s_client -connect cartorio.local:443 -tls1_3 -servername cartorio.local -showcerts
```

O comando deve mostrar a cadeia de certificação completa e a suite negociada (por exemplo, TLS_AES_256_GCM_SHA384). Também é possível usar o `curl` para testar:

```bash
curl -v https://cartorio.local --http2
```

Certifique-se de que o certificado é considerado confiável pelo cliente; adicione a CA ao *trust store* local se necessário.

### Outras plataformas

- **Apache**: use as diretivas `SSLEngine on`, `SSLCertificateFile`, `SSLCertificateKeyFile`, `SSLCertificateChainFile` e defina `SSLProtocol TLSv1.3`.
- **AWS Application Load Balancer**: importe o certificado (fullchain + private key) no ACM (Amazon Certificate Manager) e associe-o ao listener 443 do ALB. Certifique-se de selecionar uma *security policy* que suporte TLS 1.2/1.3.

Uma vez que o servidor aceita conexões TLS 1.3, o próximo capítulo mostra como exigir certificados de cliente para implementar mTLS.
