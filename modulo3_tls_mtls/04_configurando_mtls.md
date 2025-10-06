# Configurando mTLS (Autenticação Mútua)

TLS assegura a identidade do servidor; mTLS estende isso exigindo que o cliente também se autentique com um certificado. Isso é útil em APIs internas, comunicação entre microserviços ou para acesso restrito a funcionários.

## Emitindo certificados de cliente

Utilize a CA intermediária criada no módulo 2 para emitir um certificado de cliente:

```bash
# Gerar chave privada do cliente
openssl genpkey -algorithm RSA -out client.key -pkeyopt rsa_keygen_bits:2048

# Criar CSR (inclua CN e e-mail se desejar)
openssl req -new -key client.key -out client.csr -subj "/CN=usuario1/O=Cartorio Digital"

# Assinar o CSR com a CA intermediária usando o perfil de cliente (usr_cert)
openssl ca -config openssl_intermediate.cnf -extensions usr_cert \
    -days 365 -in client.csr -out client.crt -batch
```

Converta para formato PKCS#12 para uso em navegadores ou com o curl:

```bash
openssl pkcs12 -export -out client.p12 -inkey client.key -in client.crt -certfile intermediate_ca.crt
```

## Configurando o Nginx para mTLS

Adicione as seguintes diretivas ao bloco `server` configurado no capítulo anterior:

```nginx
server {
    # ... configuração TLS já existente ...

    # Caminho para o certificado da CA que emitiu os certificados de cliente
    ssl_client_certificate /etc/nginx/certs/intermediate_ca.crt;
    # Exigir e verificar certificado do cliente (on|off|optional)
    ssl_verify_client on;

    # Opcional: Controle de acesso baseado em DN do cliente
    # if ($ssl_client_s_dn !~* \"O=Cartorio Digital\") {
    #     return 403;
    # }

    # ...
}
```

Recarregue o Nginx para aplicar as mudanças:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## Testando com o curl e o openssl

Para acessar o servidor que exige mTLS, o cliente deve apresentar seu certificado:

```bash
# Usar o PKCS#12 com o curl (digite a senha de exportação se houver)
curl -v https://cartorio.local --cert client.p12 --cert-type P12 --key client.key

# Ou com o par PEM (certificado + chave)
curl -v https://cartorio.local --cert client.crt --key client.key
```

Com `openssl s_client`:

```bash
openssl s_client -connect cartorio.local:443 -tls1_3 \
    -cert client.crt -key client.key -servername cartorio.local
```

Se a verificação falhar, o servidor encerrará a conexão com um erro TLS de handshake. Você pode acompanhar os logs do Nginx para depurar (`error_log`).

### Uso em microserviços e APIs

Em ambientes de microserviços (Kubernetes, service meshes), mTLS é frequentemente ativado por padrão para assegurar que apenas serviços legítimos se comuniquem. Plataformas como Istio e Linkerd automatizam a geração e rotação de certificados de cliente. Mesmo assim, o conceito é o mesmo: cada parte autentica a outra com certificados emitidos por uma autoridade confiável.

No próximo capítulo veremos como reforçar a segurança habilitando **HSTS**, **OCSP stapling** e outros detalhes importantes.
