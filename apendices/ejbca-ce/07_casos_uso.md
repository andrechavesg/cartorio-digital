# Capítulo 7 – Casos de Uso no Cartório Digital

## Visão geral

Este capítulo demonstra como aplicar o EJBCA-CE nos cenários reais do projeto Cartório Digital, integrando com os conceitos e práticas dos módulos anteriores do curso.

## Caso de Uso 1: PKI para assinatura de documentos oficiais

### Contexto

O Cartório Digital emite certidões eletrônicas (nascimento, casamento, óbito) que precisam ser assinadas digitalmente com certificados qualificados, conforme normas ICP-Brasil.

### Requisitos

- Certificados de assinatura de documentos (não-repúdio)
- Conformidade com DOC-ICP-04 (Requisitos Mínimos para Políticas de Certificado)
- Validade: 1 ano
- Algoritmo: RSA 2048 ou ECDSA P-256
- Timestamp (RFC 3161) obrigatório

### Implementação com EJBCA-CE

#### 1. Criar hierarquia de CA

```bash
# Root CA (manter offline após setup)
docker exec ejbca /opt/primekey/bin/ejbca.sh ca init \
  --caname "Cartorio-Root-CA" \
  --dn "CN=Cartorio Digital Root CA,O=Cartorio Digital,C=BR" \
  --tokenType soft \
  --tokenPass changeit \
  --keyspec 4096 \
  --keytype RSA \
  --validity 7300 \
  -s SHA256WithRSA

# Intermediate CA para assinatura de documentos
docker exec ejbca /opt/primekey/bin/ejbca.sh ca init \
  --caname "Cartorio-Document-Signing-CA" \
  --dn "CN=Cartorio Digital Document Signing CA,O=Cartorio Digital,C=BR" \
  --tokenType soft \
  --tokenPass changeit \
  --keyspec 2048 \
  --keytype RSA \
  --validity 3650 \
  --signedby "Cartorio-Root-CA" \
  -s SHA256WithRSA
```

#### 2. Criar perfil de certificado conforme ICP-Brasil

**Certificate Profile: "ICP-Brasil-Document-Signing"**

Via Admin UI → Certificate Profiles → Add:

```
Profile Name: ICP-Brasil-Document-Signing
Type: End Entity

Key Algorithms:
  ☑ RSA 2048, 3072, 4096
  ☑ ECDSA P-256, P-384

Validity:
  1y (365 dias)

Key Usage (Critical):
  ☑ Digital Signature
  ☑ Non Repudiation

Extended Key Usage (Not Critical):
  ☑ Document Signing (1.3.6.1.4.1.311.10.3.12)

Certificate Policies:
  Policy OID: 2.16.76.1.2.1 (ICP-Brasil A1)
  CPS URI: https://cartorio.gov.br/dpc

CRL Distribution Points:
  http://crl.cartorio.gov.br/document-signing-ca.crl

Authority Information Access:
  OCSP: http://ocsp.cartorio.gov.br
  CA Issuers: http://aia.cartorio.gov.br/document-signing-ca.crt

Subject Alternative Name:
  ☐ Não usar (documentos geralmente não precisam)
```

#### 3. Criar End Entity Profile para escrivães

```
Profile Name: Escrivao-Entity

Subject DN Attributes:
  CN (Common Name): Required, Modifiable
  SERIALNUMBER: Required, Modifiable (CPF do escrivão)
  O (Organization): Fixed = "Cartorio Digital"
  OU (Organizational Unit): Modifiable (nome do cartório)
  C (Country): Fixed = "BR"

Email: Required (email institucional)

Available CAs: Cartorio-Document-Signing-CA
Default Certificate Profile: ICP-Brasil-Document-Signing
Token: User Generated (cliente gera chave, envia CSR)

Approval Settings:
  ☑ Require Approval (supervisor deve aprovar)
  Number of Approvals: 1
```

#### 4. Workflow de emissão

```python
# backend/services/certificate_service.py

import requests
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography import x509
from cryptography.x509.oid import NameOID

class CartorioCertificateService:
    def __init__(self, ejbca_url, admin_cert, admin_key):
        self.ejbca_url = ejbca_url
        self.cert = (admin_cert, admin_key)
    
    def request_escrivao_certificate(self, nome, cpf, cartorio, email):
        """
        Solicita certificado de assinatura para escrivão.
        O escrivão já possui chave privada gerada localmente.
        """
        
        # 1. Gerar chave privada (no cliente, não no servidor!)
        private_key = rsa.generate_private_key(
            public_exponent=65537,
            key_size=2048
        )
        
        # 2. Criar CSR
        csr = x509.CertificateSigningRequestBuilder().subject_name(
            x509.Name([
                x509.NameAttribute(NameOID.COMMON_NAME, nome),
                x509.NameAttribute(NameOID.SERIAL_NUMBER, cpf),
                x509.NameAttribute(NameOID.ORGANIZATION_NAME, "Cartorio Digital"),
                x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, cartorio),
                x509.NameAttribute(NameOID.COUNTRY_NAME, "BR"),
            ])
        ).add_extension(
            x509.BasicConstraints(ca=False, path_length=None),
            critical=True
        ).sign(private_key, hashes.SHA256())
        
        csr_pem = csr.public_bytes(serialization.Encoding.PEM).decode()
        
        # 3. Enviar CSR para EJBCA via REST API
        username = f"escrivao-{cpf.replace('.', '').replace('-', '')}"
        
        response = requests.post(
            f"{self.ejbca_url}/ejbca-rest-api/v1/certificate/pkcs10enroll",
            cert=self.cert,
            json={
                "certificate_request": csr_pem,
                "certificate_profile_name": "ICP-Brasil-Document-Signing",
                "end_entity_profile_name": "Escrivao-Entity",
                "ca_name": "Cartorio-Document-Signing-CA",
                "username": username,
                "password": "temp-password-123",  # Será mudada
                "email": email
            }
        )
        
        if response.status_code == 201:
            cert_pem = response.json()["certificate"]
            
            # 4. Salvar certificado e chave privada em token/smartcard
            # (em produção, isso seria feito via hardware token)
            return {
                "certificate": cert_pem,
                "status": "PENDING_APPROVAL",
                "message": "Aguardando aprovação do supervisor"
            }
        else:
            raise Exception(f"Erro ao solicitar certificado: {response.text}")
    
    def approve_certificate_request(self, username, supervisor_id):
        """
        Supervisor aprova solicitação de certificado.
        """
        # Implementar lógica de aprovação
        # (requer configuração de approval profiles no EJBCA)
        pass
```

#### 5. Assinar documentos com certificados emitidos

```python
# backend/services/document_signing_service.py

from endesive.pdf import cms
import datetime

def sign_certidao(pdf_path, cert_path, key_path, output_path):
    """
    Assina certidão PDF com certificado EJBCA.
    """
    
    # Carregar certificado e chave
    with open(cert_path, "rb") as f:
        cert_data = f.read()
    
    with open(key_path, "rb") as f:
        key_data = f.read()
    
    # Preparar dados de assinatura
    dct = {
        "sigflags": 3,
        "contact": "certidoes@cartorio.gov.br",
        "location": "Cartório Digital",
        "signingdate": datetime.datetime.now().strftime("%Y%m%d%H%M%S+00'00'"),
        "reason": "Emissão de Certidão Oficial",
    }
    
    # Assinar PDF (PAdES)
    with open(pdf_path, "rb") as pdf_file:
        pdf_data = pdf_file.read()
    
    signed_data = cms.sign(
        pdf_data,
        dct,
        cert_data,
        key_data,
        [],  # Chain (se necessário)
        "sha256"
    )
    
    # Salvar PDF assinado
    with open(output_path, "wb") as output_file:
        output_file.write(pdf_data)
        output_file.write(signed_data)
    
    return output_path
```

## Caso de Uso 2: mTLS para microsserviços internos

### Contexto

Os microsserviços do Cartório Digital (backend, BFF, workers) precisam se comunicar de forma segura usando autenticação mútua (mTLS).

### Implementação

#### 1. Criar CA interna para serviços

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh ca init \
  --caname "Cartorio-Internal-Services-CA" \
  --dn "CN=Cartorio Internal Services CA,O=Cartorio Digital,C=BR" \
  --tokenType soft \
  --tokenPass changeit \
  --keyspec 2048 \
  --keytype RSA \
  --validity 1825 \
  --signedby "Cartorio-Root-CA" \
  -s SHA256WithRSA
```

#### 2. Profile para serviços

**Certificate Profile: "Internal-Service-mTLS"**

```
Key Usage:
  ☑ Digital Signature
  ☑ Key Encipherment

Extended Key Usage:
  ☑ Server Authentication (1.3.6.1.5.5.7.3.1)
  ☑ Client Authentication (1.3.6.1.5.5.7.3.2)

Validity: 90 dias (rotação frequente)
```

#### 3. Automação com ACME

```yaml
# docker-compose.yml do serviço backend

services:
  backend:
    image: cartorio/backend:latest
    volumes:
      - ./certs:/certs
    environment:
      TLS_CERT: /certs/backend.crt
      TLS_KEY: /certs/backend.key
      TLS_CA: /certs/ca.crt
    entrypoint:
      - /bin/sh
      - -c
      - |
        # Solicitar certificado via ACME
        certbot certonly \
          --standalone \
          --non-interactive \
          --agree-tos \
          --email admin@cartorio.gov.br \
          --server https://ejbca.interno.cartorio/ejbca/acme/internal-services \
          --domain backend.interno.cartorio \
          --deploy-hook "cp /etc/letsencrypt/live/backend.interno.cartorio/* /certs/"
        
        # Iniciar aplicação
        python main.py
```

#### 4. Configurar FastAPI com mTLS

```python
# backend/main.py

from fastapi import FastAPI, Security, HTTPException
from fastapi.security import HTTPBearer
import ssl

app = FastAPI()

# Configurar SSL context para mTLS
ssl_context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
ssl_context.load_cert_chain(
    certfile="/certs/backend.crt",
    keyfile="/certs/backend.key"
)
ssl_context.load_verify_locations(cafile="/certs/ca.crt")
ssl_context.verify_mode = ssl.CERT_REQUIRED

@app.get("/certidoes/{id}")
async def get_certidao(id: str, request: Request):
    # Extrair certificado do cliente
    client_cert = request.scope["transport"].get_extra_info("ssl_object").getpeercert()
    
    # Validar CN do certificado
    cn = dict(x[0] for x in client_cert["subject"])["commonName"]
    
    if not cn.endswith(".interno.cartorio"):
        raise HTTPException(401, "Certificado inválido")
    
    # Processar requisição...
    return {"id": id, "authenticated_as": cn}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8443,
        ssl_certfile="/certs/backend.crt",
        ssl_keyfile="/certs/backend.key",
        ssl_ca_certs="/certs/ca.crt",
        ssl_cert_reqs=ssl.CERT_REQUIRED
    )
```

## Caso de Uso 3: Certificados para totens de autoatendimento

### Contexto

O Cartório Digital instala totens em locais públicos para emissão de certidões. Cada totem precisa de certificado para autenticação no backend.

### Implementação

#### 1. Profile para dispositivos IoT

```
Certificate Profile: IoT-Device
End Entity Profile: Totem-Entity

Subject DN:
  CN: totem-{serial-number}.cartorio.gov.br
  O: Cartorio Digital
  OU: Dispositivos IoT
  C: BR

Validity: 180 dias

Extended Key Usage:
  Client Authentication
```

#### 2. Provisionamento automatizado com EST

```python
# Script de provisionamento no totem

import requests
import os
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography import x509
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes

def provision_totem(serial_number, provisioning_token):
    """
    Provisiona certificado no totem usando EST.
    """
    
    # 1. Gerar chave privada (armazenar em TPM se disponível)
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048
    )
    
    # 2. Criar CSR
    csr = x509.CertificateSigningRequestBuilder().subject_name(
        x509.Name([
            x509.NameAttribute(NameOID.COMMON_NAME, f"totem-{serial_number}.cartorio.gov.br"),
            x509.NameAttribute(NameOID.ORGANIZATION_NAME, "Cartorio Digital"),
            x509.NameAttribute(NameOID.ORGANIZATIONAL_UNIT_NAME, "Dispositivos IoT"),
            x509.NameAttribute(NameOID.COUNTRY_NAME, "BR"),
        ])
    ).sign(private_key, hashes.SHA256())
    
    # 3. Enviar via EST
    response = requests.post(
        "https://ejbca.cartorio.gov.br/.well-known/est/totem-ca/simpleenroll",
        data=csr.public_bytes(serialization.Encoding.DER),
        headers={
            "Content-Type": "application/pkcs10",
            "Authorization": f"Bearer {provisioning_token}"
        },
        verify="/etc/ssl/certs/cartorio-ca-bundle.pem"
    )
    
    if response.status_code == 200:
        # Salvar certificado
        cert_data = response.content
        with open("/etc/cartorio/totem.crt", "wb") as f:
            f.write(cert_data)
        
        # Salvar chave privada (protegida)
        key_pem = private_key.private_bytes(
            encoding=serialization.Encoding.PEM,
            format=serialization.PrivateFormat.PKCS8,
            encryption_algorithm=serialization.BestAvailableEncryption(b"senha-do-totem")
        )
        with open("/etc/cartorio/totem.key", "wb") as f:
            f.write(key_pem)
        
        print(f"✅ Totem {serial_number} provisionado com sucesso")
        return True
    else:
        print(f"❌ Erro no provisionamento: {response.status_code}")
        return False

# Executar provisionamento
if __name__ == "__main__":
    serial = os.environ.get("TOTEM_SERIAL", "UNKNOWN")
    token = os.environ.get("PROVISIONING_TOKEN")
    provision_totem(serial, token)
```

## Caso de Uso 4: Timestamping (carimbo de tempo RFC 3161)

### Contexto

Documentos assinados precisam de timestamp para garantir quando foram criados, conforme MP 2.200-2/2001.

### Implementação

#### 1. Criar Time Stamping Authority (TSA)

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh ca init \
  --caname "Cartorio-TSA" \
  --dn "CN=Cartorio Digital Time Stamping Authority,O=Cartorio Digital,C=BR" \
  --tokenType soft \
  --tokenPass changeit \
  --keyspec 2048 \
  --keytype RSA \
  --validity 3650 \
  -s SHA256WithRSA
```

#### 2. Emitir certificado de TSA

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh ra addendentity \
  --username tsa-signer \
  --dn "CN=Cartorio TSA Signer,O=Cartorio Digital,C=BR" \
  --caname "Cartorio-TSA" \
  --type 1 \
  --token P12 \
  --eku 1.3.6.1.5.5.7.3.8  # Time Stamping EKU

docker exec ejbca /opt/primekey/bin/ejbca.sh batch
```

#### 3. Configurar servidor de timestamp

```python
# backend/services/timestamp_service.py

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography import x509
from cryptography.x509.oid import ExtendedKeyUsageOID
import datetime
import hashlib

class TimestampService:
    def __init__(self, tsa_cert_path, tsa_key_path):
        with open(tsa_cert_path, "rb") as f:
            self.tsa_cert = x509.load_pem_x509_certificate(f.read())
        
        with open(tsa_key_path, "rb") as f:
            self.tsa_key = serialization.load_pem_private_key(f.read(), password=None)
    
    def create_timestamp_token(self, data_hash):
        """
        Cria token de timestamp RFC 3161.
        """
        # Simplificado - usar biblioteca rfc3161ng em produção
        timestamp = datetime.datetime.utcnow()
        
        # Construir TimeStampReq response
        tst_info = {
            "version": 1,
            "policy": "1.2.3.4.5.6.7.1.1",  # OID da política
            "messageImprint": {
                "hashAlgorithm": "sha256",
                "hashedMessage": data_hash
            },
            "serialNumber": int.from_bytes(os.urandom(8), "big"),
            "genTime": timestamp,
            "accuracy": {"seconds": 1}
        }
        
        # Assinar com chave TSA
        signature = self.tsa_key.sign(
            str(tst_info).encode(),
            padding.PKCS1v15(),
            hashes.SHA256()
        )
        
        return {
            "status": "granted",
            "timestamp": timestamp.isoformat(),
            "token": signature.hex()
        }

# API endpoint
from fastapi import FastAPI, UploadFile

app = FastAPI()
tsa_service = TimestampService("/certs/tsa.crt", "/certs/tsa.key")

@app.post("/timestamp")
async def timestamp_document(file: UploadFile):
    # Calcular hash do documento
    content = await file.read()
    doc_hash = hashlib.sha256(content).digest()
    
    # Gerar timestamp
    token = tsa_service.create_timestamp_token(doc_hash)
    
    return token
```

## Integração com módulos do curso

### Módulo 2 (PKI e Certificados)

O EJBCA-CE permite praticar todos os conceitos:
- Criação de hierarquias de CA
- Emissão de certificados X.509
- Revogação e CRL

### Módulo 4 (Automação ACME)

O EJBCA-CE tem servidor ACME integrado, permitindo:
- Testar Certbot localmente
- Configurar desafios HTTP-01, DNS-01
- Renovação automática

### Módulo 5 (Conformidade)

Permite criar perfis compatíveis com:
- ICP-Brasil (OIDs, extensões)
- eIDAS (certificados qualificados)

### Módulo 6 (KMS/HSM)

Integração com HSM via PKCS#11 para proteger chaves de CA.

## Próximos passos

Agora que você conhece o EJBCA-CE em profundidade:

1. **Experimente** os exemplos deste capítulo no seu ambiente
2. **Compare** com implementações usando AWS Private CA (módulo 2)
3. **Decida** qual abordagem usar para cada parte do Cartório Digital
4. **Documente** suas decisões arquiteturais

## Referências

- **ICP-Brasil DOC-ICP-04:** Requisitos Mínimos para Políticas de Certificado
- **RFC 3161:** Time-Stamp Protocol (TSP)
- **RFC 5280:** X.509 Certificate Profile
- **EJBCA Use Cases:** [https://www.ejbca.org/use-cases/](https://www.ejbca.org/use-cases/)

