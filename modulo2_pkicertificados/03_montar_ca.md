# Construindo uma Autoridade Certificadora (CA)

Nesta etapa você criará a base da sua PKI: uma Autoridade Certificadora raiz e uma CA intermediária. A CA raiz é o ponto mais confiável da cadeia; seus certificados nunca devem sair do ambiente seguro. A CA intermediária é usada para assinar certificados de servidor e cliente, preservando a chave da raiz.

### 1. Definindo a estrutura de diretórios

Crie um diretório `pki` com subpastas para a CA raiz e CA intermediária:

```bash
mkdir -p ~/pki/root/{certs,crl,newcerts,private}
mkdir -p ~/pki/intermediate/{certs,crl,csr,newcerts,private}
chmod 700 ~/pki/root/private ~/pki/intermediate/private
touch ~/pki/root/index.txt ~/pki/intermediate/index.txt
echo 1000 > ~/pki/root/serial
echo 1000 > ~/pki/intermediate/serial
```

Crie arquivos `openssl.cnf` específicos para cada CA (raiz e intermediária) contendo as políticas de assinatura e caminhos de diretórios. Para simplificar, consulte exemplos do [OpenSSL PKI Tutorial](https://jamielinux.com/docs/openssl-certificate-authority/). Você também pode usar o **[Smallstep step‑ca](https://smallstep.com/docs/step-ca)** para iniciar uma CA local com comandos mais simples.

### 2. Gerando a chave e o certificado da CA raiz

```bash
cd ~/pki/root
# Gere a chave privada (RSA 4096 bits ou ECDSA)
openssl genrsa -aes256 -out private/ca.key.pem 4096
chmod 400 private/ca.key.pem

# Crie o certificado da CA raiz
openssl req -config openssl.cnf \
      -key private/ca.key.pem \
      -new -x509 -days 3650 -sha256 -extensions v3_ca \
      -out certs/ca.cert.pem

# Verifique o certificado
openssl x509 -noout -text -in certs/ca.cert.pem
```

Anote a senha da chave e guarde `ca.key.pem` em local seguro.

### 3. Gerando a CA intermediária

```bash
cd ~/pki/intermediate
# Chave privada da intermediária
openssl genrsa -aes256 -out private/intermediate.key.pem 4096
chmod 400 private/intermediate.key.pem

# CSR da intermediária
openssl req -config openssl.cnf -new -sha256 \
      -key private/intermediate.key.pem \
      -out csr/intermediate.csr.pem

# Assine o CSR com a CA raiz
cd ~/pki/root
openssl ca -config openssl.cnf -extensions v3_intermediate_ca \
      -days 1825 -notext -md sha256 \
      -in ~/pki/intermediate/csr/intermediate.csr.pem \
      -out ~/pki/intermediate/certs/intermediate.cert.pem

# Verifique e proteja o certificado intermediário
chmod 444 ~/pki/intermediate/certs/intermediate.cert.pem
openssl x509 -noout -text -in ~/pki/intermediate/certs/intermediate.cert.pem
```

### 4. Criando a cadeia de certificados

```bash
cat ~/pki/intermediate/certs/intermediate.cert.pem \
    ~/pki/root/certs/ca.cert.pem > ~/pki/intermediate/certs/ca-chain.cert.pem
chmod 444 ~/pki/intermediate/certs/ca-chain.cert.pem
```

A cadeia `ca-chain.cert.pem` será usada para validar certificados emitidos pela intermediária.

### 5. Usando `step-ca` como alternativa

Se preferir simplificar o processo, você pode usar o **step-ca** para inicializar uma CA de desenvolvimento em poucos comandos:

```bash
# Instale o step-cli conforme documentação
step ca init --name "Cartório Digital" --dns localhost --address :9000
# Responda às perguntas para gerar raiz e intermediária automaticamente
```

No próximo capítulo, emitiremos certificados de servidor e de cliente usando nossa CA intermediária.
