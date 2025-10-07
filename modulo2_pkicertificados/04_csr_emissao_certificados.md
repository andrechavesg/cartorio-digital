# Gerando CSRs e emitindo certificados

## Exemplo Inspirador

Quando o cartório digital lançou sua primeira API pública, o deploy travou porque o provedor externo não entregaria um certificado personalizado a tempo. Em minutos, o time de infraestrutura abriu o terminal, gerou o CSR com todas as SANs necessárias e usou a CA intermediária recém-criada para assinar o pedido. Ver o endpoint responder com o nosso próprio certificado foi o combustível perfeito para provar que dominar a emissão interna nos torna ágeis e confiáveis.

## Conceitos Fundamentais

- **CSR (Certificate Signing Request):** arquivo que contém a chave pública e as informações do solicitante, usado para pedir a assinatura de um certificado.
- **Extensões alinhadas ao perfil:** SAN, Key Usage e EKU devem refletir o uso (servidor ou cliente).
- **Fluxo completo:** gerar chaves → criar CSR → assinar com a intermediária → instalar certificado.
- **Documentação de emissão:** cada certificado deve estar registrado com responsável, propósito e data de expiração.

## Práticas Reais

1. **Crie a chave e o CSR do servidor do cartório:**
   ```bash
   openssl genrsa -out ~/pki/reqs/server.key.pem 2048
   openssl req -new -key ~/pki/reqs/server.key.pem \
       -out ~/pki/reqs/server.csr.pem \
       -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=Servicos/CN=cartorio.local" \
       -reqexts req_ext \
       -config <(cat <<'CONF'
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
   CONF
   )
   ```
   Registre no inventário qual serviço utilizará essa chave.

2. **Assine o CSR com a CA intermediária:**
   ```bash
   cd ~/pki/intermediate
   openssl ca -config openssl.cnf \
       -extensions server_cert -days 375 -notext -md sha256 \
       -in csr/server.csr.pem \
       -out certs/cartorio.local.cert.pem
   chmod 444 certs/cartorio.local.cert.pem
   ```
   Confirme que o perfil `server_cert` contém `extendedKeyUsage = serverAuth`.

3. **Emita certificados de cliente para mTLS:**
   ```bash
   openssl genrsa -out ~/pki/reqs/client.key.pem 2048
   openssl req -new -key ~/pki/reqs/client.key.pem \
       -out ~/pki/reqs/client.csr.pem \
       -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=Usuarios/CN=usuario.exemplo"

   cd ~/pki/intermediate
   openssl ca -config openssl.cnf \
       -extensions usr_cert -days 375 -notext -md sha256 \
       -in csr/client.csr.pem \
       -out certs/usuario.exemplo.cert.pem
   chmod 444 certs/usuario.exemplo.cert.pem
   ```
   Adapte o perfil `usr_cert` para incluir `extendedKeyUsage = clientAuth` (e `emailProtection` se aplicável).

4. **Teste a instalação e a cadeia:**
   ```bash
   openssl verify -CAfile ~/pki/intermediate/certs/ca-chain.cert.pem \
       certs/cartorio.local.cert.pem
   ```
   Configure seu servidor (Nginx, Apache etc.) para usar a chave privada, o certificado e a cadeia `ca-chain.cert.pem`.

5. **Documente o ciclo de vida:** registre datas de expiração, responsáveis e sistemas que dependem de cada certificado para facilitar renovações.

## Próximos passos

Agora que emitimos e instalamos certificados, precisamos planejar o que fazer quando uma credencial é comprometida ou expira antes do previsto. No próximo capítulo veremos, inspirados por um caso real de revogação, como manter a confiança viva com CRL, OCSP e verificações constantes.
