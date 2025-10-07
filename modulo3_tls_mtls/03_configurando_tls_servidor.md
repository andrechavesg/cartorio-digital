# Configurando TLS 1.3 em um Servidor

Com os certificados emitidos pela sua CA (módulo 2) e o entendimento dos conceitos de TLS 1.3, você está pronto para proteger um serviço web. Neste capítulo vamos configurar o Nginx (ou outro servidor) para oferecer TLS 1.3 com a cadeia completa de certificação.

## Preparando a cadeia de certificados

Suporte relatou que alguns clientes corporativos estão vendo alertas de "cadeia incompleta" ao acessar o portal do cartório. O motivo é que o servidor entrega apenas o certificado da aplicação, sem a intermediária que faz a ponte até a raiz confiável. Para corrigir a situação, reúna e encadeie os certificados antes de colocá-los no servidor.

Reúna os seguintes arquivos:

- `server.crt`: o certificado do servidor assinado pela CA intermediária.
- `server.key`: a chave privada do servidor.
- `intermediate_ca.crt`: o certificado da CA intermediária.
- `root_ca.crt`: o certificado da CA raiz (opcional para clientes, mas útil para teste local).

Em seguida, monte o *fullchain* concatenando o certificado do servidor com o da intermediária. O comando abaixo passa a ser parte do procedimento padrão sempre que um novo certificado for emitido e encerra os chamados de cadeia incompleta:

```bash
cat server.crt intermediate_ca.crt > fullchain.pem
```

## Configurando o Nginx

O próximo desafio é garantir que o portal do cartório atenda apenas via HTTPS moderno, com HTTP/2, TLS 1.3 e OCSP stapling para reduzir advertências nos navegadores. Cada diretiva a seguir foi escolhida para resolver uma dor específica:

- `listen 443 ssl http2;`: força o servidor virtual a responder somente via HTTPS e habilita HTTP/2 para melhorar a experiência dos usuários.
- `ssl_certificate`/`ssl_certificate_key`: apontam para o *fullchain* recém-gerado e a chave privada, evitando a falha de cadeia relatada.
- `ssl_protocols TLSv1.3;`: restringe o tráfego à versão mais moderna suportada, atendendo às exigências da auditoria.
- `ssl_ciphers ...;`: mantém um conjunto compatível com o legado, mas alinhado às suites robustas aprovadas pela equipe de segurança.
- `ssl_ecdh_curve ...;`: define curvas que entregam sigilo perfeito, cobrindo a necessidade de proteção contra vazamentos futuros de chaves.
- `ssl_stapling`/`ssl_stapling_verify` e `resolver`: habilitam OCSP stapling para acelerar a validação de revogação, eliminando atrasos percebidos pelos usuários finais.

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

Com a configuração aplicada, a equipe precisa comprovar para a diretoria que o serviço entrega a cadeia completa com TLS 1.3 antes de prosseguir para a etapa de mTLS. Primeiro, confirme com `openssl` que o handshake mostra a cadeia correta e negocia a suite esperada:

```bash
openssl s_client -connect cartorio.local:443 -tls1_3 -servername cartorio.local -showcerts
```

O comando deve listar os certificados da cadeia intermediária logo após o `server.crt` e exibir uma suite TLS 1.3 (por exemplo, `TLS_AES_256_GCM_SHA384`). Esse resultado valida a entrega correta e libera a equipe para configurar autenticação mútua no capítulo seguinte. Em seguida, use o `curl` para garantir que os clientes HTTP/2 do portal também funcionem sem advertências:

```bash
curl -v https://cartorio.local --http2
```

O `curl` deve concluir o handshake sem erros de certificado, mostrar `ALPN, server accepted to use h2` e retornar `HTTP/2 200`. Com esse relatório positivo, a próxima etapa do projeto (habilitar mTLS no módulo 4) fica desbloqueada. Caso contrário, investigue a *trust store* local e importe a CA, se necessário.

### Outras plataformas

- **Apache**: use as diretivas `SSLEngine on`, `SSLCertificateFile`, `SSLCertificateKeyFile`, `SSLCertificateChainFile` e defina `SSLProtocol TLSv1.3`.
- **AWS Application Load Balancer**: importe o certificado (fullchain + private key) no ACM (Amazon Certificate Manager) e associe-o ao listener 443 do ALB. Certifique-se de selecionar uma *security policy* que suporte TLS 1.2/1.3.

Uma vez que o servidor aceita conexões TLS 1.3, o próximo capítulo mostra como exigir certificados de cliente para implementar mTLS.
