# Capítulo 5 – Protocolos e Integrações

## Visão geral dos protocolos suportados

O EJBCA-CE oferece suporte a múltiplos protocolos de gerenciamento de certificados, permitindo integração com diversos tipos de sistemas e dispositivos.

```
┌──────────────────────────────────────────────────────┐
│              EJBCA-CE Protocol Stack                 │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │  Web Protocols                                 │ │
│  │  • REST API (JSON)                             │ │
│  │  • SOAP Web Services (XML)                     │ │
│  │  • Admin Web UI                                │ │
│  │  • Public Web (Enrollment Pages)               │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │  Modern Enrollment Protocols                   │ │
│  │  • ACME (RFC 8555) - Let's Encrypt compatible  │ │
│  │  • EST (RFC 7030) - IoT devices               │ │
│  │  • CMP (RFC 4210) - Enterprise PKI            │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │  Legacy Enrollment Protocols                   │ │
│  │  • SCEP (RFC 8894) - Network devices          │ │
│  │  • CSR via HTTP                                │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
│  ┌────────────────────────────────────────────────┐ │
│  │  Validation Protocols                          │ │
│  │  • OCSP (RFC 6960) - Real-time validation     │ │
│  │  • CRL Distribution (HTTP/LDAP)               │ │
│  └────────────────────────────────────────────────┘ │
│                                                      │
└──────────────────────────────────────────────────────┘
```

## ACME (Automated Certificate Management Environment)

### Configuração do servidor ACME

O EJBCA-CE inclui um servidor ACME compatível com clientes como Certbot, acme.sh, Caddy e Traefik.

#### Passo 1: Habilitar ACME

**System Configuration** → **Protocol Configuration** → **ACME Configuration**

- **Alias:** `cartorio-acme`
- **CA:** `Cartorio-Issuing-CA`
- **End Entity Profile:** `ACME-Entity-Profile`
- **Certificate Profile:** `TLS-Server-Profile`
- **Pre-authorization:** ☐ (Desabilitado)
- **Wildcard Certificates:** ☑ (Habilitado)
- **Terms of Service URL:** `https://cartorio.gov.br/tos`
- **Website URL:** `https://cartorio.gov.br`

#### Passo 2: Configurar DNS para challenges

Para DNS-01 challenges (necessário para wildcards), configure permissões de API:

```bash
# AWS Route 53 example
export AWS_ACCESS_KEY_ID="AKIAxxxxxxxxx"
export AWS_SECRET_ACCESS_KEY="xxxxxxxxxxxxxx"
```

#### Passo 3: URL do servidor ACME

```
https://localhost:8443/ejbca/acme/cartorio-acme/directory
```

### Uso com Certbot

#### HTTP-01 Challenge (domínios individuais)

```bash
certbot certonly \
  --standalone \
  --server https://ejbca.cartorio.gov.br/ejbca/acme/cartorio-acme/directory \
  --domain api.cartorio.gov.br \
  --domain portal.cartorio.gov.br \
  --agree-tos \
  --email admin@cartorio.gov.br
```

#### DNS-01 Challenge (wildcards)

```bash
certbot certonly \
  --dns-route53 \
  --server https://ejbca.cartorio.gov.br/ejbca/acme/cartorio-acme/directory \
  --domain "*.cartorio.gov.br" \
  --domain cartorio.gov.br \
  --agree-tos \
  --email admin@cartorio.gov.br
```

#### Renovação automática

```bash
# Adicionar ao crontab
0 3 * * * certbot renew --quiet --deploy-hook "systemctl reload nginx"
```

### Uso com acme.sh

```bash
# Instalar acme.sh
curl https://get.acme.sh | sh

# Configurar servidor ACME customizado
acme.sh --set-default-ca \
  --server https://ejbca.cartorio.gov.br/ejbca/acme/cartorio-acme/directory

# Emitir certificado (HTTP-01)
acme.sh --issue \
  -d api.cartorio.gov.br \
  -d portal.cartorio.gov.br \
  --standalone

# Emitir certificado wildcard (DNS-01 com Route 53)
export AWS_ACCESS_KEY_ID="AKIAxxxxxxxxx"
export AWS_SECRET_ACCESS_KEY="xxxxxxxxxxxxxx"

acme.sh --issue \
  -d "*.cartorio.gov.br" \
  -d cartorio.gov.br \
  --dns dns_aws
```

### Uso com Traefik (proxy reverso)

```yaml
# traefik.yml
entryPoints:
  web:
    address: ":80"
  websecure:
    address: ":443"

certificatesResolvers:
  ejbca:
    acme:
      email: admin@cartorio.gov.br
      storage: /acme/acme.json
      caServer: https://ejbca.cartorio.gov.br/ejbca/acme/cartorio-acme/directory
      httpChallenge:
        entryPoint: web
```

```yaml
# docker-compose.yml para serviço
services:
  api:
    image: cartorio/api:latest
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.api.rule=Host(`api.cartorio.gov.br`)"
      - "traefik.http.routers.api.tls=true"
      - "traefik.http.routers.api.tls.certresolver=ejbca"
```

### Uso com Caddy (web server)

```
# Caddyfile
{
  acme_ca https://ejbca.cartorio.gov.br/ejbca/acme/cartorio-acme/directory
  email admin@cartorio.gov.br
}

api.cartorio.gov.br {
  reverse_proxy localhost:8080
}

portal.cartorio.gov.br {
  root * /var/www/portal
  file_server
}
```

## REST API

A REST API do EJBCA-CE oferece controle programático completo sobre operações de PKI.

### Autenticação

Todas as chamadas requerem certificado de cliente (mTLS):

```bash
# Usando curl
curl https://ejbca.cartorio.gov.br/ejbca/ejbca-rest-api/v1/ca \
  --cert admin-client.pem \
  --key admin-client-key.pem \
  --cacert ca-chain.pem

# Usando arquivo P12
curl https://ejbca.cartorio.gov.br/ejbca/ejbca-rest-api/v1/ca \
  --cert admin-client.p12:senha
```

### Endpoints principais

#### 1. Gestão de CAs

**Listar CAs:**
```bash
GET /ejbca/ejbca-rest-api/v1/ca
```

**Obter status de uma CA:**
```bash
GET /ejbca/ejbca-rest-api/v1/ca/{ca_name}/status
```

#### 2. Emissão de certificados

**Enrollment (gerar chave e certificado):**
```bash
POST /ejbca/ejbca-rest-api/v1/certificate/enrollkeystore

{
  "username": "servidor-backend",
  "password": "temp-password",
  "key_alg": "RSA",
  "key_spec": "2048",
  "subject_dn": "CN=backend.cartorio.gov.br,O=Cartorio Digital,C=BR",
  "subject_alt_name": "dnsName=backend.cartorio.gov.br",
  "ca_name": "Cartorio-Issuing-CA",
  "certificate_profile_name": "TLS-Server-Profile",
  "end_entity_profile_name": "TLS-Server-Entity"
}
```

**Assinar CSR existente:**
```bash
POST /ejbca/ejbca-rest-api/v1/certificate/pkcs10enroll

{
  "certificate_request": "-----BEGIN CERTIFICATE REQUEST-----\n...",
  "certificate_profile_name": "TLS-Server-Profile",
  "end_entity_profile_name": "TLS-Server-Entity",
  "ca_name": "Cartorio-Issuing-CA",
  "username": "servidor-backend",
  "password": "temp-password"
}
```

#### 3. Revogação

**Revogar por serial number:**
```bash
PUT /ejbca/ejbca-rest-api/v1/certificate/{issuer_dn}/{certificate_serial_number}/revoke

{
  "reason": "KEY_COMPROMISE",
  "date": "2025-10-09T14:30:00Z"
}
```

#### 4. Busca de certificados

**Buscar certificados por username:**
```bash
POST /ejbca/ejbca-rest-api/v1/certificate/search

{
  "max_number_of_results": 10,
  "criteria": [
    {
      "property": "USERNAME",
      "value": "maria.santos",
      "operation": "EQUAL"
    }
  ]
}
```

### Exemplo de integração Python

```python
import requests
from requests.auth import HTTPBasicAuth

class EJBCAClient:
    def __init__(self, base_url, cert_path, key_path, ca_bundle):
        self.base_url = base_url
        self.cert = (cert_path, key_path)
        self.verify = ca_bundle
    
    def issue_certificate(self, cn, dns_names, profile="TLS-Server-Profile"):
        """Emite certificado TLS"""
        url = f"{self.base_url}/certificate/enrollkeystore"
        
        san = ",".join([f"dnsName={dns}" for dns in dns_names])
        
        payload = {
            "username": cn.replace("*", "wildcard"),
            "password": "temp-password",
            "key_alg": "RSA",
            "key_spec": "2048",
            "subject_dn": f"CN={cn},O=Cartorio Digital,C=BR",
            "subject_alt_name": san,
            "ca_name": "Cartorio-Issuing-CA",
            "certificate_profile_name": profile,
            "end_entity_profile_name": "TLS-Server-Entity"
        }
        
        response = requests.post(
            url, 
            json=payload, 
            cert=self.cert, 
            verify=self.verify
        )
        
        if response.status_code == 200:
            # Salvar P12
            with open(f"{cn}.p12", "wb") as f:
                f.write(response.content)
            return True
        else:
            print(f"Erro: {response.status_code} - {response.text}")
            return False
    
    def revoke_certificate(self, serial_number, reason="UNSPECIFIED"):
        """Revoga certificado"""
        issuer_dn = "CN=Cartorio Digital Issuing CA,O=Cartorio Digital,C=BR"
        url = f"{self.base_url}/certificate/{issuer_dn}/{serial_number}/revoke"
        
        payload = {"reason": reason}
        
        response = requests.put(
            url, 
            json=payload, 
            cert=self.cert, 
            verify=self.verify
        )
        
        return response.status_code == 200

# Uso
client = EJBCAClient(
    base_url="https://ejbca.cartorio.gov.br/ejbca/ejbca-rest-api/v1",
    cert_path="/path/to/admin.crt",
    key_path="/path/to/admin.key",
    ca_bundle="/path/to/ca-bundle.crt"
)

# Emitir certificado
client.issue_certificate(
    cn="api.cartorio.gov.br",
    dns_names=["api.cartorio.gov.br", "api-backup.cartorio.gov.br"]
)

# Revogar certificado
client.revoke_certificate("1A2B3C4D5E6F", reason="KEY_COMPROMISE")
```

## SCEP (Simple Certificate Enrollment Protocol)

### Configuração

**System Configuration** → **Protocol Configuration** → **SCEP Configuration**

- **Alias:** `cartorio-scep`
- **CA:** `Cartorio-Issuing-CA`
- **RA Mode:** ☐ (Desabilitado)
- **Include CA Chain:** ☑

### URL do serviço SCEP

```
http://ejbca.cartorio.gov.br/ejbca/publicweb/apply/scep/cartorio-scep/pkiclient.exe
```

### Uso com sscep (cliente SCEP)

```bash
# Instalar sscep
sudo apt-get install sscep

# Obter certificado da CA
sscep getca \
  -u http://ejbca.cartorio.gov.br/ejbca/publicweb/apply/scep/cartorio-scep/pkiclient.exe \
  -c ca.crt

# Gerar chave e CSR
openssl genrsa -out device.key 2048
openssl req -new -key device.key -out device.csr \
  -subj "/CN=router-001.cartorio.local/O=Cartorio Digital/C=BR"

# Enrollment via SCEP
sscep enroll \
  -u http://ejbca.cartorio.gov.br/ejbca/publicweb/apply/scep/cartorio-scep/pkiclient.exe \
  -c ca.crt \
  -k device.key \
  -r device.csr \
  -l device.crt
```

### Configuração em Cisco IOS

```
crypto pki trustpoint CARTORIO-CA
  enrollment url http://ejbca.cartorio.gov.br/ejbca/publicweb/apply/scep/cartorio-scep/pkiclient.exe
  subject-name CN=router-001.cartorio.local,O=Cartorio Digital,C=BR
  revocation-check none
  rsakeypair CARTORIO-KEY 2048

crypto pki authenticate CARTORIO-CA
crypto pki enroll CARTORIO-CA
```

## EST (Enrollment over Secure Transport)

### Configuração

**System Configuration** → **Protocol Configuration** → **EST Configuration**

- **Alias:** `cartorio-est`
- **CA:** `Cartorio-Issuing-CA`
- **Require Client Certificate:** ☑

### URL do serviço EST

```
https://ejbca.cartorio.gov.br/.well-known/est/cartorio-est
```

### Uso com libest (cliente EST)

```bash
# Instalar libest
sudo apt-get install libest-dev

# Enrollment simples
estclient -s ejbca.cartorio.gov.br \
  -p 8443 \
  --srp username:password \
  -o device.p7
```

## CMP (Certificate Management Protocol)

O CMP é o protocolo mais completo para gestão de certificados em ambientes empresariais, suportando operações avançadas como key update e revocation requests.

### Configuração

**System Configuration** → **Protocol Configuration** → **CMP Configuration**

- **Alias:** `cartorio-cmp`
- **CA:** `Cartorio-Issuing-CA`
- **Authentication Module:** HMAC ou End Entity Certificate

### URL do serviço CMP

```
http://ejbca.cartorio.gov.br/ejbca/publicweb/cmp/cartorio-cmp
```

## Webhooks e notificações

O EJBCA-CE Enterprise Edition suporta webhooks nativos. Na Community Edition, você pode implementar polling ou integrar via logs.

### Alternativa: Monitorar logs e disparar webhooks

```bash
#!/bin/bash
# webhook-monitor.sh

tail -F /opt/wildfly/standalone/log/server.log | while read line; do
  if echo "$line" | grep -q "CERT_CREATION"; then
    # Extrair dados do log
    username=$(echo "$line" | grep -oP 'username=\K[^ ]+')
    
    # Enviar webhook
    curl -X POST https://cartorio.gov.br/webhooks/cert-issued \
      -H "Content-Type: application/json" \
      -d "{\"event\": \"certificate.issued\", \"username\": \"$username\"}"
  fi
done
```

## Conclusão

O EJBCA-CE oferece protocolos para praticamente qualquer cenário de automação de PKI. No próximo capítulo, você verá uma **comparação entre EJBCA-CE e soluções comerciais** como AWS Private CA, para ajudar na escolha da melhor opção para cada caso de uso.

## Referências

- **RFC 8555:** ACME Protocol
- **RFC 8894:** SCEP Protocol
- **RFC 7030:** EST Protocol
- **RFC 4210:** CMP Protocol
- **EJBCA REST API Docs:** [https://doc.primekey.com/ejbca/ejbca-operations/ejbca-rest-interface](https://doc.primekey.com/ejbca/ejbca-operations/ejbca-rest-interface)

