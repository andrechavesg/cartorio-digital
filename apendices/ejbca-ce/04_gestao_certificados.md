# Capítulo 4 – Emissão e Gestão de Certificados

## Ciclo de vida de certificados

O ciclo de vida de um certificado digital no EJBCA-CE passa por várias fases:

```
┌─────────────────────────────────────────────────────────┐
│           Ciclo de Vida de Certificado                 │
│                                                         │
│  1. Solicitação (CSR)                                  │
│       │                                                 │
│       ▼                                                 │
│  2. Validação de identidade (RA)                       │
│       │                                                 │
│       ▼                                                 │
│  3. Aprovação (manual ou automática)                   │
│       │                                                 │
│       ▼                                                 │
│  4. Emissão e assinatura (CA)                          │
│       │                                                 │
│       ▼                                                 │
│  5. Distribuição ao solicitante                        │
│       │                                                 │
│       ▼                                                 │
│  6. Uso ativo                                          │
│       │                                                 │
│       ├──> Renovação (volta ao passo 1)                │
│       │                                                 │
│       └──> Revogação (fim do ciclo)                    │
│                │                                        │
│                ▼                                        │
│  7. Arquivamento                                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Métodos de emissão

O EJBCA-CE oferece múltiplas formas de solicitar certificados, adequadas para diferentes cenários.

### 1. Via interface web (manual)

**Ideal para:** Emissões pontuais, certificados de usuários internos

#### Passo a passo:

1. Acesse **RA Functions** → **Add End Entity**
2. Preencha os dados:
   ```
   Username: joao.silva
   Password: senha-temporaria (para recuperar o certificado)
   Common Name (CN): João Silva
   Email: joao.silva@cartorio.gov.br
   Organization (O): Cartorio Digital
   End Entity Profile: Employee-Certificate
   Certificate Profile: Document-Signing
   CA: Cartorio-Issuing-CA
   Token: P12 File
   ```
3. Clique em **Add**
4. Vá em **RA Functions** → **Create Keystore**
5. Busque o usuário `joao.silva`
6. Clique em **Create P12** → O arquivo será baixado automaticamente

O arquivo `.p12` contém o certificado + chave privada, protegido pela senha informada.

### 2. Via linha de comando (CLI)

**Ideal para:** Automação, scripts, testes

```bash
# Adicionar entidade
docker exec ejbca /opt/primekey/bin/ejbca.sh ra addendentity \
  --username "maria.santos" \
  --dn "CN=Maria Santos,E=maria.santos@cartorio.gov.br,O=Cartorio Digital,C=BR" \
  --caname "Cartorio-Issuing-CA" \
  --certprofile "Document-Signing" \
  --eeprofile "Employee-Certificate" \
  --type 1 \
  --token P12 \
  --password "senha123"

# Gerar certificado
docker exec ejbca /opt/primekey/bin/ejbca.sh batch

# Exportar P12
docker cp ejbca:/opt/primekey/p12/maria.santos.p12 ./maria.santos.p12
```

### 3. Via REST API (programático)

**Ideal para:** Integração com sistemas, portais de autoatendimento

#### Autenticação

As APIs do EJBCA-CE usam certificados de cliente para autenticação. Primeiro, configure permissões para o certificado de administrador:

**System Configuration** → **Administrator Roles** → **Add**

- **Role Name:** `API-Automation`
- **Match with:** `X509: CN, Certificate`
- **Match value:** `API-Automation`
- **Permissions:**
  - `/ca/Cartorio-Issuing-CA`: Todos os direitos
  - `/ra_functionality/`: Todos os direitos

#### Exemplos de uso

**Criar certificado (enrollment):**

```bash
curl -X POST \
  https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/enrollkeystore \
  --cert superadmin.p12:ejbca \
  --cacert ca-chain.pem \
  --header "Content-Type: application/json" \
  --data '{
    "username": "servidor-backend",
    "password": "temp-password",
    "key_alg": "RSA",
    "key_spec": "2048",
    "subject_dn": "CN=backend.cartorio.local,O=Cartorio Digital,C=BR",
    "subject_alt_name": "dnsName=backend.cartorio.local,dnsName=api.cartorio.local",
    "ca_name": "Cartorio-Issuing-CA",
    "certificate_profile_name": "TLS-Server-Profile",
    "end_entity_profile_name": "TLS-Server-Entity"
  }' \
  --output servidor-backend.p12
```

**Revogar certificado:**

```bash
curl -X PUT \
  https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/servidor-backend/revoke \
  --cert superadmin.p12:ejbca \
  --header "Content-Type: application/json" \
  --data '{
    "reason": "KEY_COMPROMISE",
    "date": "2025-10-09T12:00:00Z"
  }'
```

### 4. Via protocolos (ACME, SCEP, EST)

#### ACME (Automated Certificate Management Environment)

**Ideal para:** Automação de certificados TLS/SSL

**Configuração no EJBCA-CE:**

1. **System Configuration** → **Protocol Configuration** → **ACME**
2. Habilitar ACME para `Cartorio-Issuing-CA`
3. Configurar:
   ```
   ACME Alias: cartorio-acme
   End Entity Profile: TLS-Server-Entity
   Certificate Profile: TLS-Server-Profile
   Pre-authorization allowed: No
   Wildcard certificates allowed: Yes
   ```

**Uso com Certbot:**

```bash
# Instalar Certbot
sudo apt-get update
sudo apt-get install certbot

# Solicitar certificado via HTTP-01
certbot certonly \
  --standalone \
  --server https://localhost:8443/ejbca/acme/directory \
  --domain portal.cartorio.local \
  --agree-tos \
  --email admin@cartorio.gov.br \
  --no-verify-ssl
```

**Uso com acme.sh:**

```bash
# Instalar acme.sh
curl https://get.acme.sh | sh

# Solicitar certificado
acme.sh --issue \
  --server https://localhost:8443/ejbca/acme/directory \
  -d api.cartorio.local \
  --standalone \
  --insecure
```

#### SCEP (Simple Certificate Enrollment Protocol)

**Ideal para:** Dispositivos de rede (roteadores, switches)

**URL SCEP do EJBCA-CE:**
```
http://localhost:8080/ejbca/publicweb/apply/scep/cartorio-issuing-ca/pkiclient.exe
```

**Exemplo com cliente SCEP:**

```bash
# Gerar chave privada
openssl genrsa -out device.key 2048

# Gerar CSR
openssl req -new -key device.key -out device.csr \
  -subj "/CN=device-001.cartorio.local/O=Cartorio Digital/C=BR"

# Enviar via SCEP (usando sscep client)
sscep enroll \
  -u http://localhost:8080/ejbca/publicweb/apply/scep/cartorio-issuing-ca/pkiclient.exe \
  -c cacert.pem \
  -k device.key \
  -r device.csr \
  -l device.crt
```

## Renovação de certificados

A renovação deve ocorrer antes da expiração para evitar interrupções.

### Renovação automática via ACME

Certbot e acme.sh já incluem renovação automática:

```bash
# Certbot (via cron ou systemd timer)
certbot renew --dry-run

# acme.sh (via cron)
acme.sh --renew -d api.cartorio.local
```

### Renovação manual via CLI

```bash
# Renovar certificado existente
docker exec ejbca /opt/primekey/bin/ejbca.sh ra renewendentity \
  --username "maria.santos"

# Gerar novo certificado
docker exec ejbca /opt/primekey/bin/ejbca.sh batch
```

### Renovação via REST API

```bash
curl -X POST \
  https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/renew \
  --cert superadmin.p12:ejbca \
  --header "Content-Type: application/json" \
  --data '{
    "username": "servidor-backend"
  }'
```

## Revogação de certificados

A revogação invalida um certificado antes da sua expiração natural.

### Motivos de revogação (RFC 5280)

| Código | Motivo | Quando usar |
|--------|--------|-------------|
| `0` | unspecified | Motivo não especificado |
| `1` | keyCompromise | Chave privada foi comprometida |
| `2` | cACompromise | CA foi comprometida |
| `3` | affiliationChanged | Pessoa mudou de organização |
| `4` | superseded | Certificado foi substituído |
| `5` | cessationOfOperation | Serviço foi desativado |
| `6` | certificateHold | Suspensão temporária |
| `8` | removeFromCRL | Reativar certificado suspenso |

### Revogar via interface web

1. **RA Functions** → **Search End Entities**
2. Buscar por username ou subject
3. Clicar em **Revoke**
4. Selecionar motivo (ex: `Key Compromise`)
5. Confirmar

### Revogar via CLI

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh ra revokeendentity \
  --username "maria.santos" \
  --reason KEY_COMPROMISE
```

### Revogar via REST API

```bash
curl -X PUT \
  https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/serial/1A2B3C4D5E6F/revoke \
  --cert superadmin.p12:ejbca \
  --header "Content-Type: application/json" \
  --data '{
    "reason": "KEY_COMPROMISE",
    "date": "2025-10-09T14:30:00Z"
  }'
```

### Verificar revogação via OCSP

```bash
openssl ocsp \
  -issuer issuing-ca.crt \
  -cert maria-santos.crt \
  -url http://localhost:8080/ejbca/publicweb/status/ocsp \
  -text
```

Resposta para certificado revogado:
```
Response verify OK
maria-santos.crt: revoked
        This Update: Oct  9 14:35:00 2025 GMT
        Reason: keyCompromise
        Revocation Time: Oct  9 14:30:00 2025 GMT
```

## Auditoria de certificados

O EJBCA-CE registra todas as operações em logs de auditoria.

### Acessar logs via interface web

**System Functions** → **Audit Log**

Filtros disponíveis:
- **Event:** Tipo de operação (CERT_CREATION, CERT_REVOKE, etc.)
- **Administrator:** Quem realizou a operação
- **CA:** Qual CA foi utilizada
- **Date Range:** Período de tempo

### Eventos importantes a monitorar

| Evento | Descrição | Alerta |
|--------|-----------|--------|
| `CERT_CREATION` | Certificado emitido | Monitorar volume anormal |
| `CERT_REVOKE` | Certificado revogado | Investigar motivo |
| `CA_OFFLINE` | CA ficou offline | Crítico |
| `ADMINISTRATORLOGGEDIN` | Admin fez login | Monitorar acessos fora de horário |
| `SYSTEMSTART` | Sistema reiniciado | Verificar se foi planejado |

### Exportar logs via CLI

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh audit \
  --export \
  --startdate "2025-10-01" \
  --enddate "2025-10-09" \
  --output /tmp/audit-export.csv

docker cp ejbca:/tmp/audit-export.csv ./audit-$(date +%Y%m%d).csv
```

### Integração com SIEM

Para ambientes de produção, integre logs com soluções como:

- **Splunk**
- **Elastic Stack (ELK)**
- **Graylog**
- **AWS CloudWatch**

Exemplo de configuração de log forwarding:

```bash
docker exec ejbca bash -c "tail -f /opt/wildfly/standalone/log/server.log" | \
  logger -t ejbca -n siem.cartorio.local -P 514
```

## Relatórios e estatísticas

### Certificados emitidos por período

```sql
-- Conectar ao banco de dados
docker exec -it ejbca_db psql -U ejbca

-- Query
SELECT 
  DATE_TRUNC('day', to_timestamp(updateTime/1000)) as data,
  COUNT(*) as total
FROM CertificateData
WHERE status = 20  -- CERT_ACTIVE
GROUP BY data
ORDER BY data DESC
LIMIT 30;
```

### Certificados próximos da expiração

```sql
SELECT 
  subjectDN,
  serialNumber,
  to_timestamp(expireDate/1000) as expira_em
FROM CertificateData
WHERE status = 20
  AND expireDate < EXTRACT(EPOCH FROM NOW() + INTERVAL '30 days') * 1000
ORDER BY expireDate ASC;
```

### Dashboard com Grafana

Configure datasource PostgreSQL no Grafana apontando para o banco EJBCA:

**Painel: Certificados ativos**
```sql
SELECT COUNT(*) as ativos
FROM CertificateData
WHERE status = 20;
```

**Painel: Certificados revogados (última semana)**
```sql
SELECT COUNT(*) as revogados
FROM CertificateData
WHERE status = 40
  AND revocationDate > EXTRACT(EPOCH FROM NOW() - INTERVAL '7 days') * 1000;
```

**Painel: Emissões por dia (últimos 30 dias)**
```sql
SELECT 
  DATE_TRUNC('day', to_timestamp(updateTime/1000)) as dia,
  COUNT(*) as emissoes
FROM CertificateData
WHERE updateTime > EXTRACT(EPOCH FROM NOW() - INTERVAL '30 days') * 1000
GROUP BY dia
ORDER BY dia;
```

## Boas práticas de gestão

### 1. Automação máxima

- Use ACME para certificados TLS/SSL sempre que possível
- Integre emissão via APIs em sistemas de provisionamento
- Configure renovação automática com alertas de falha

### 2. Monitoramento proativo

- Configure alertas para certificados próximos da expiração (30, 15, 7 dias)
- Monitore taxa de revogação (pico pode indicar incidente)
- Acompanhe tempo de resposta de OCSP

### 3. Segurança operacional

- Nunca compartilhe arquivos P12 por email ou chat
- Use canais seguros para distribuição (portal web com autenticação)
- Rotacione senhas temporárias imediatamente após uso
- Mantenha backup das chaves de CA offline e criptografado

### 4. Conformidade

- Mantenha logs de auditoria por pelo menos 7 anos (exigência ICP-Brasil)
- Documente todos os perfis de certificado e justificativas
- Realize auditorias trimestrais de certificados ativos
- Revogue imediatamente certificados de funcionários desligados

## Troubleshooting

### Erro: "End entity already exists"

```bash
# Remover entidade existente
docker exec ejbca /opt/primekey/bin/ejbca.sh ra revokeendentity \
  --username "usuario-duplicado"

docker exec ejbca /opt/primekey/bin/ejbca.sh ra deleteendentity \
  --username "usuario-duplicado"
```

### Erro: "Certificate profile not found"

Verifique se o perfil existe e está associado ao End Entity Profile:

```bash
docker exec ejbca /opt/primekey/bin/ejbca.sh ca listcertprofiles
```

### Erro: OCSP responde "unknown"

Certificado não foi emitido por uma CA conhecida do OCSP responder. Verifique:

1. Se a CA está configurada no OCSP Service
2. Se o certificado realmente foi emitido pelo EJBCA-CE
3. Se o serial number está correto

## Próximos passos

No próximo capítulo, você explorará em profundidade os **protocolos e integrações** suportados pelo EJBCA-CE, aprendendo a configurar ACME, REST API e webhooks para automação avançada.

## Referências

- **EJBCA Certificate Management:** [https://doc.primekey.com/ejbca/ejbca-operations/ejbca-ca-concept-guide](https://doc.primekey.com/ejbca/ejbca-operations/ejbca-ca-concept-guide)
- **RFC 5280:** X.509 Certificate and CRL Profile
- **RFC 6960:** OCSP Protocol
- **ACME Protocol (RFC 8555):** Automated Certificate Management

