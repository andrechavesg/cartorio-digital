# Gerando CSRs e emitindo certificados

Com a CA intermediária pronta, podemos emitir certificados para servidores (como o site do cartório) e para clientes (usuários ou sistemas internos). O processo envolve gerar um par de chaves e um CSR (Certificate Signing Request), e então assinar esse CSR com a CA intermediária.

## 1. Gerar a chave e CSR do servidor

1. Quando o site do cartório foi migrado para Nginx, percebemos que dependíamos de certificados emitidos por terceiros e não conseguíamos incluir SANs específicos para homologação. O problema era garantir identidade completa dos domínios internos; a solução veio ao gerar nosso próprio par de chaves e CSR com `openssl genrsa` e `openssl req`, diretamente no host do serviço:

   ```bash
   # Gere a chave privada do servidor
   openssl genrsa -out ~/pki/reqs/server.key.pem 2048

   # Crie o CSR
   openssl req -new -key ~/pki/reqs/server.key.pem \
       -out ~/pki/reqs/server.csr.pem \
       -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=Servicos/CN=cartorio.local" \
       -reqexts req_ext \
       -config <(cat <<'EOF'
[req]
distinguished_name = dn
req_extensions = req_ext
prompt = no

[dn]
C = BR
ST = Sao Paulo
L = Santo Andre
O = Cartorio Digital
OU = Servicos
CN = cartorio.local

[req_ext]
subjectAltName = @alt_names

[alt_names]
DNS.1 = cartorio.local
DNS.2 = www.cartorio.local
EOF
)
   ```

   Esse comando usa uma configuração inline para incluir SANs no CSR.

2. Logo após gerar o CSR, enfrentamos a dor de esperar horas por aprovações externas. Para desbloquear o deploy, assinamos o pedido internamente com a CA intermediária usando `openssl ca`, demonstrando como o cartório pode responder com agilidade às demandas do Nginx:

   ```bash
   cd ~/pki/intermediate
   openssl ca -config openssl.cnf \
       -extensions server_cert -days 375 -notext -md sha256 \
       -in csr/server.csr.pem \
       -out certs/cartorio.local.cert.pem

   chmod 444 certs/cartorio.local.cert.pem
   ```

   A extensão `server_cert` deve estar definida no `openssl.cnf` da intermediária com `keyUsage = digitalSignature, keyEncipherment` e `extendedKeyUsage = serverAuth`.

## 2. Gerar certificados de cliente

Quando habilitamos mTLS entre sistemas do cartório, surgiram tickets de suporte porque usuários não possuíam credenciais confiáveis. O problema era fornecer identidades fortes para pessoas e integrações; replicamos o processo com `openssl genrsa`, `openssl req` e `openssl ca`, emitindo certificados de cliente que devolvem autonomia à operação:

```bash
# Chave e CSR do cliente
openssl genrsa -out ~/pki/reqs/client.key.pem 2048
openssl req -new -key ~/pki/reqs/client.key.pem \
    -out ~/pki/reqs/client.csr.pem \
    -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=Usuarios/CN=usuario.exemplo"

# Assinatura com extensão client_cert
cd ~/pki/intermediate
openssl ca -config openssl.cnf \
    -extensions usr_cert -days 375 -notext -md sha256 \
    -in csr/client.csr.pem \
    -out certs/usuario.exemplo.cert.pem

chmod 444 certs/usuario.exemplo.cert.pem
```

O perfil `usr_cert` no `openssl.cnf` deve conter `keyUsage = digitalSignature, keyEncipherment` e `extendedKeyUsage = clientAuth, emailProtection` se desejar usar o certificado para S/MIME.

## 3. Instalando e testando os certificados

- Combine a chave privada e o certificado do servidor em um único arquivo (`.pem`) se necessário.
- Configure seu serviço web (Nginx, Apache ou ALB) para usar `cartorio.local.cert.pem` e `server.key.pem`, além da cadeia `ca-chain.cert.pem` como `trusted_ca`.
- Para clientes mTLS, distribua `usuario.exemplo.cert.pem`, `client.key.pem` e a cadeia de CA para importação no navegador ou aplicação.

Para evitar novas interrupções, verificamos se a cadeia estava íntegra e reconhecida pelos parceiros. O comando `openssl verify` fecha o ciclo mostrando que o certificado do Nginx está ancorado na confiança do cartório:

```bash
openssl verify -CAfile ~/pki/intermediate/certs/ca-chain.cert.pem certs/cartorio.local.cert.pem
```

No próximo capítulo, veremos como revogar um certificado e verificar seu status via CRL e OCSP.
