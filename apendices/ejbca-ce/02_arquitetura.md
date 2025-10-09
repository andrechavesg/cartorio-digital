# Capítulo 2 – Arquitetura e Componentes do EJBCA-CE

## Visão geral da arquitetura

O EJBCA-CE implementa uma arquitetura modular que separa claramente as responsabilidades de uma PKI completa. Esta separação permite escalabilidade, segurança e flexibilidade na operação.

```
┌────────────────────────────────────────────────────────────┐
│                      EJBCA-CE Platform                      │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Web Interface (Admin UI)                │ │
│  │         (Gestão de CAs, Perfis, Usuários)           │ │
│  └──────────────────────────────────────────────────────┘ │
│                            │                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                Protocol Endpoints                     │ │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────────┐  │ │
│  │  │ ACME │ │ SCEP │ │ EST  │ │ CMP  │ │ REST API │  │ │
│  │  └──────┘ └──────┘ └──────┘ └──────┘ └──────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
│                            │                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Core PKI Services                        │ │
│  │  ┌────────┐  ┌────────┐  ┌────────┐                 │ │
│  │  │   CA   │  │   RA   │  │   VA   │                 │ │
│  │  └────────┘  └────────┘  └────────┘                 │ │
│  └──────────────────────────────────────────────────────┘ │
│                            │                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Data Persistence Layer                   │ │
│  │    ┌──────────────┐      ┌──────────────┐           │ │
│  │    │  PostgreSQL  │      │  File System │           │ │
│  │    │   Database   │      │   (Certs)    │           │ │
│  │    └──────────────┘      └──────────────┘           │ │
│  └──────────────────────────────────────────────────────┘ │
│                            │                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Security & Crypto Layer                  │ │
│  │    ┌──────────────┐      ┌──────────────┐           │ │
│  │    │ BouncyCastle │      │   HSM/PKCS11 │           │ │
│  │    │   Provider   │      │   Interface  │           │ │
│  │    └──────────────┘      └──────────────┘           │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

## Autoridade Certificadora (CA)

### Responsabilidades

A CA é o coração do EJBCA-CE, responsável por:

1. **Geração e armazenamento de pares de chaves**
   - Chaves privadas protegidas por senha ou HSM
   - Suporte a múltiplos algoritmos (RSA, ECDSA, EdDSA)

2. **Emissão de certificados**
   - Assinatura de CSRs validados pela RA
   - Aplicação de perfis de certificado
   - Inclusão de extensões X.509

3. **Manutenção de hierarquias**
   - Root CA (deve ficar offline em produção)
   - Intermediate CAs (emitem certificados para entidades finais)
   - Sub-CAs (para segregação por departamento/função)

4. **Gestão do ciclo de vida**
   - Renovação de certificados
   - Revogação e publicação de CRL
   - Arquivamento de certificados expirados

### Tipos de CA no EJBCA-CE

#### Root CA (CA Raiz)

- **Função:** Âncora de confiança da hierarquia
- **Características:**
  - Auto-assinada
  - Validade longa (10-20 anos)
  - Deve ficar offline após emitir CAs intermediárias
  - Chave privada protegida em HSM ou mídia offline

#### Intermediate CA (CA Intermediária)

- **Função:** Emitir certificados para entidades finais ou sub-CAs
- **Características:**
  - Assinada pela Root CA
  - Validade menor que Root CA (5-10 anos)
  - Opera online
  - Pode ter múltiplas instâncias para diferentes propósitos

#### Sub-CA

- **Função:** Segregar emissão por departamento, região ou tipo de certificado
- **Características:**
  - Assinada por Intermediate CA
  - Validade limitada
  - Permite políticas específicas

### Perfis de CA

O EJBCA-CE permite criar perfis de CA com diferentes configurações:

- **Nome e Subject DN**
- **Algoritmo de assinatura** (SHA256withRSA, SHA384withECDSA, etc.)
- **Período de validade**
- **Extensões X.509** (Key Usage, Extended Key Usage, CRL Distribution Points)
- **Políticas de certificado** (OIDs customizados)

### Exemplo de hierarquia no Cartório Digital

```
Cartório Digital Root CA
  └── Cartório Digital Issuing CA (TLS)
      ├── api.cartorio.gov.br
      ├── portal.cartorio.gov.br
      └── *.interno.cartorio.gov.br
  └── Cartório Digital Issuing CA (Document Signing)
      ├── Certidão de Nascimento Signer
      ├── Certidão de Casamento Signer
      └── Certidão de Óbito Signer
```

## Autoridade de Registro (RA)

### Responsabilidades

A RA funciona como a camada de validação e aprovação, responsável por:

1. **Recebimento de solicitações**
   - Aceitar CSRs via web, API ou protocolos
   - Validar formato e conteúdo do CSR

2. **Autenticação de solicitantes**
   - Verificar identidade via senha, token, certificado
   - Integração com LDAP/Active Directory
   - Autenticação via OAuth2/OIDC

3. **Aprovação de requisições**
   - Aprovação automática (baseada em regras)
   - Aprovação manual (workflow administrativo)
   - Aprovação multinível (múltiplos aprovadores)

4. **Gestão de identidades**
   - Manter registros de usuários e dispositivos
   - Associar certificados a entidades
   - Rastrear histórico de emissões

### End Entity Profiles (Perfis de Entidade Final)

Os perfis de entidade final definem:

- **Campos permitidos no Subject DN** (CN, O, OU, C, etc.)
- **Subject Alternative Names (SAN)** permitidos
- **Tipo de certificado** (TLS Server, Client, Code Signing)
- **Validade máxima**
- **Requerimentos de aprovação**

#### Exemplo de perfil para servidor TLS

```
End Entity Profile: "TLS Server Certificate"
  Subject DN Attributes:
    - CN (Common Name): Obrigatório
    - O (Organization): Fixo = "Cartório Digital"
    - C (Country): Fixo = "BR"
  Subject Alternative Names:
    - DNS Name: Obrigatório (pode ter múltiplos)
    - IP Address: Opcional
  Certificate Profile: "TLS Server"
  CA: "Cartório Digital Issuing CA (TLS)"
  Validity: 90 dias
  Approval Required: Não (automático se DNS validado)
```

#### Exemplo de perfil para assinatura de documentos

```
End Entity Profile: "Document Signer"
  Subject DN Attributes:
    - CN: Nome do escrivão
    - E (Email): Email institucional
    - O: "Cartório Digital"
    - OU: Cartório específico
    - C: "BR"
  Certificate Profile: "Document Signing"
  CA: "Cartório Digital Issuing CA (Document Signing)"
  Validity: 1 ano
  Approval Required: Sim (aprovação do supervisor)
```

### Certificate Profiles (Perfis de Certificado)

Os perfis de certificado definem as características técnicas:

- **Algoritmo de chave** (RSA 2048, ECDSA P-256, Ed25519)
- **Key Usage** (Digital Signature, Key Encipherment, etc.)
- **Extended Key Usage** (Server Authentication, Code Signing, etc.)
- **Políticas de certificado** (OIDs)
- **CRL Distribution Points**
- **OCSP URL**

#### Exemplo de perfil TLS Server

```
Certificate Profile: "TLS Server"
  Type: End Entity
  Key Algorithms: RSA 2048+, ECDSA P-256+
  Key Usage (Critical):
    - Digital Signature
    - Key Encipherment
  Extended Key Usage (Critical):
    - Server Authentication (1.3.6.1.5.5.7.3.1)
  CRL Distribution Points:
    - http://crl.cartorio.gov.br/issuing-ca.crl
  Authority Information Access:
    - OCSP: http://ocsp.cartorio.gov.br
```

## Autoridade de Validação (VA)

### Responsabilidades

A VA fornece serviços de validação em tempo real:

1. **OCSP Responder**
   - Responder consultas sobre status de certificados
   - Assinar respostas OCSP com certificado dedicado
   - Suportar OCSP Stapling

2. **Publicação de CRL**
   - Gerar CRLs periodicamente
   - Publicar em HTTP/LDAP
   - Suportar Delta CRLs (atualizações incrementais)

3. **Validação de cadeia**
   - Verificar integridade de cadeias de certificação
   - Fornecer status de revogação em tempo real

### OCSP (Online Certificate Status Protocol)

O OCSP permite verificar o status de um certificado sem baixar uma CRL completa.

#### Fluxo de validação OCSP

```
Cliente                      OCSP Responder (VA)           CA Database
  │                                │                          │
  │ 1. OCSP Request               │                          │
  │   (Serial Number)             │                          │
  ├───────────────────────────────>│                          │
  │                                │ 2. Query certificate     │
  │                                │    status                │
  │                                ├─────────────────────────>│
  │                                │                          │
  │                                │ 3. Status (good/revoked) │
  │                                │<─────────────────────────┤
  │ 4. OCSP Response               │                          │
  │   (Signed status)              │                          │
  │<───────────────────────────────┤                          │
  │                                │                          │
```

#### Vantagens do OCSP

- ✅ Respostas em tempo real (mais atual que CRL)
- ✅ Menor tráfego de rede (apenas status do certificado consultado)
- ✅ Suporte a OCSP Stapling (servidor entrega resposta junto com certificado)

#### Configuração no EJBCA-CE

```
OCSP Service Configuration:
  - OCSP Signing Key: Dedicada (não usa chave da CA)
  - Response Validity: 10 minutos
  - Max Age: 30 segundos
  - Include Certificate Chain: Sim
  - Nonce: Suportado (previne replay attacks)
```

### CRL (Certificate Revocation List)

CRLs são listas de certificados revogados, publicadas periodicamente.

#### Estrutura de uma CRL

```
Certificate Revocation List:
  Issuer: CN=Cartório Digital Issuing CA
  This Update: 2025-10-09 12:00:00 UTC
  Next Update: 2025-10-10 12:00:00 UTC
  Revoked Certificates:
    Serial Number: 0x1A2B3C4D
      Revocation Date: 2025-10-08 15:30:00 UTC
      Reason: Key Compromise
    Serial Number: 0x5E6F7A8B
      Revocation Date: 2025-10-09 08:15:00 UTC
      Reason: Superseded
```

#### Configuração no EJBCA-CE

```
CRL Configuration:
  - Update Interval: 24 horas
  - Overlap Time: 10 minutos
  - Delta CRL: Habilitado (a cada 1 hora)
  - Include Revocation Reasons: Sim
  - Distribution Points:
    - http://crl.cartorio.gov.br/issuing-ca.crl
    - ldap://ldap.cartorio.gov.br/cn=CRL,dc=cartorio,dc=gov,dc=br
```

## Protocolos de inscrição

O EJBCA-CE suporta múltiplos protocolos para diferentes casos de uso:

### ACME (Automated Certificate Management Environment)

- **RFC 8555**
- **Uso:** Automação de TLS/SSL (Let's Encrypt)
- **Challenges:** HTTP-01, DNS-01, TLS-ALPN-01
- **Integração:** Certbot, acme.sh, Traefik, Caddy

### SCEP (Simple Certificate Enrollment Protocol)

- **RFC 8894**
- **Uso:** Dispositivos de rede (roteadores, switches, firewalls)
- **Características:** Simples, amplamente suportado por equipamentos

### EST (Enrollment over Secure Transport)

- **RFC 7030**
- **Uso:** Dispositivos IoT e embarcados
- **Características:** Mais moderno que SCEP, baseado em HTTPS

### CMP (Certificate Management Protocol)

- **RFC 4210/4211**
- **Uso:** Ambientes corporativos complexos
- **Características:** Suporte a workflows avançados, múltiplas operações

### REST API

- **Documentação:** OpenAPI/Swagger
- **Uso:** Integração customizada com sistemas
- **Operações:** Emissão, revogação, consulta, gestão

## Camada de persistência

### Banco de dados

O EJBCA-CE armazena no banco de dados:

- **Metadados de certificados** (serial, subject, validade, status)
- **Perfis** (CA, Certificate, End Entity)
- **Usuários e permissões**
- **Logs de auditoria**
- **Configurações do sistema**

**Bancos suportados:**
- PostgreSQL (recomendado)
- MySQL/MariaDB
- Oracle Database
- Microsoft SQL Server
- H2 (apenas para desenvolvimento)

### Sistema de arquivos

Armazena:
- **Certificados emitidos** (formato PEM/DER)
- **CRLs publicadas**
- **Chaves privadas** (criptografadas)
- **Arquivos de configuração**

## Camada de segurança

### BouncyCastle Crypto Provider

O EJBCA-CE usa BouncyCastle como biblioteca criptográfica principal, oferecendo:

- Algoritmos modernos (Ed25519, ChaCha20-Poly1305)
- Suporte completo a X.509
- Implementação de protocolos (ACME, SCEP, CMP)

### Integração com HSM

Suporte via PKCS#11 para:

- **Luna HSM** (Thales)
- **nShield** (Entrust)
- **AWS CloudHSM**
- **Azure Key Vault**
- **SoftHSM** (emulação para testes)

## Conclusão

A arquitetura modular do EJBCA-CE permite:

- **Escalabilidade:** Separar CA, RA e VA em instâncias diferentes
- **Segurança:** Isolar chaves privadas em HSM e manter Root CA offline
- **Flexibilidade:** Suportar múltiplos protocolos e casos de uso
- **Auditabilidade:** Logs completos de todas as operações

No próximo capítulo, você aprenderá a **instalar e configurar o EJBCA-CE** usando Docker, criando sua primeira CA e emitindo certificados.

## Referências

- **EJBCA Architecture Guide:** [https://doc.primekey.com/ejbca/ejbca-introduction/ejbca-architecture](https://doc.primekey.com/ejbca/ejbca-introduction/ejbca-architecture)
- **RFC 5280:** X.509 Certificate and CRL Profile
- **RFC 6960:** OCSP - Online Certificate Status Protocol
- **RFC 5280:** CRL Profile

