# Capítulo 3 – Instalação e Configuração do EJBCA-CE

## Pré-requisitos

Antes de instalar o EJBCA-CE, certifique-se de ter:

### Hardware mínimo

- **CPU:** 2 cores
- **RAM:** 4 GB (8 GB recomendado para produção)
- **Disco:** 20 GB livres
- **Rede:** Acesso à internet para download de imagens

### Software necessário

- **Docker:** versão 20.10+
- **Docker Compose:** versão 2.0+ (opcional, mas recomendado)
- **Navegador web:** Chrome, Firefox ou Edge (para acessar Admin UI)

### Conhecimentos recomendados

- Linha de comando Linux/Unix
- Conceitos básicos de Docker
- Noções de PKI (módulos 1 e 2 deste curso)

## Executando a partir deste repositório

O código-fonte de referência deste módulo está em `apendices/ejbca-ce/ejbca-ce-source`. Ele acompanha arquivos de configuração prontos para uso, incluindo a habilitação de `awskms.cryptotoken.enabled=true`, e um `docker-compose.yml` na raiz de `apendices/ejbca-ce` para orquestrar os serviços necessários.

Para levantar o ambiente utilizando o material deste repositório:

1. A partir da raiz do projeto, entre no diretório do módulo:

   ```bash
   cd apendices/ejbca-ce
   ```

2. Caso deseje customizações adicionais, copie `ejbca-ce-source/conf/web.properties.sample` para `ejbca-ce-source/conf/web.properties` antes de subir os serviços.

3. Suba a stack localmente (usa MariaDB e expõe HTTP/HTTPS):

   ```bash
   docker compose up -d
   ```

4. Acompanhe a inicialização pela composição:

   ```bash
   docker compose logs -f ejbca
   ```

Os próximos passos deste capítulo seguem válidos e podem ser executados sobre esse ambiente provisionado localmente.

## Instalação com Docker (método recomendado)

Este é o método mais rápido para ter um ambiente EJBCA-CE funcional.

### Passo 1: Baixar a imagem oficial

```bash
docker pull keyfactor/ejbca-ce:latest
```

**Nota:** A imagem tem aproximadamente 1.5 GB. O download pode levar alguns minutos dependendo da sua conexão.

### Passo 2: Executar o container

```bash
docker run -d \
  --name ejbca \
  -p 8080:8080 \
  -p 8443:8443 \
  -e TLS_SETUP_ENABLED=simple \
  -e DATABASE_JDBC_URL="jdbc:h2:/mnt/persistent/ejbcadb;DB_CLOSE_DELAY=-1" \
  -v ejbca_data:/mnt/persistent \
  keyfactor/ejbca-ce:latest
```

**Explicação dos parâmetros:**

- `-d`: Executa em background (daemon)
- `--name ejbca`: Define o nome do container
- `-p 8080:8080`: Expõe porta HTTP
- `-p 8443:8443`: Expõe porta HTTPS
- `-e TLS_SETUP_ENABLED=simple`: Cria certificado TLS auto-assinado
- `-e DATABASE_JDBC_URL`: Usa banco H2 embarcado (apenas para desenvolvimento)
- `-v ejbca_data:/mnt/persistent`: Persiste dados em volume Docker

### Passo 3: Aguardar inicialização

```bash
docker logs -f ejbca
```

Aguarde até ver a mensagem:
```
INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0025: EJBCA CE started
```

Isso pode levar de 2 a 5 minutos no primeiro boot.

### Passo 4: Acessar a interface administrativa

Abra o navegador em:

```
https://localhost:8443/ejbca/adminweb/
```

**Nota:** Você verá um aviso de certificado não confiável (esperado, pois é auto-assinado). Aceite a exceção de segurança.

#### Credenciais padrão

- **Username:** `superadmin`
- **Password:** Não há senha. A autenticação é feita via **certificado de cliente**.

Para gerar o certificado de cliente no primeiro acesso:

```bash
# Extrair certificado e chave do container
docker exec ejbca /opt/primekey/scripts/setup-credentials.sh

# Os arquivos serão salvos em /tmp/ dentro do container
docker cp ejbca:/tmp/superadmin.p12 .
```

Importe o arquivo `superadmin.p12` no seu navegador:

- **Chrome/Edge:** Settings → Privacy and security → Security → Manage certificates → Import
- **Firefox:** Settings → Privacy & Security → View Certificates → Your Certificates → Import

Senha padrão do arquivo P12: `ejbca`

## Instalação com Docker Compose (recomendado para desenvolvimento)

Este método configura EJBCA-CE com PostgreSQL para ambiente mais próximo de produção.

### Passo 1: Criar diretório de trabalho

```bash
mkdir -p ~/ejbca-lab
cd ~/ejbca-lab
```

### Passo 2: Criar arquivo docker-compose.yml

```yaml
version: '3.8'

services:
  database:
    image: postgres:15
    container_name: ejbca_db
    environment:
      POSTGRES_USER: ejbca
      POSTGRES_PASSWORD: ejbca
      POSTGRES_DB: ejbca
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ejbca_network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ejbca"]
      interval: 10s
      timeout: 5s
      retries: 5

  ejbca:
    image: keyfactor/ejbca-ce:latest
    container_name: ejbca
    depends_on:
      database:
        condition: service_healthy
    ports:
      - "8080:8080"
      - "8443:8443"
    environment:
      TLS_SETUP_ENABLED: "simple"
      DATABASE_JDBC_URL: "jdbc:postgresql://database:5432/ejbca"
      DATABASE_USER: "ejbca"
      DATABASE_PASSWORD: "ejbca"
      LOG_LEVEL_APP: "INFO"
      LOG_LEVEL_SERVER: "INFO"
    volumes:
      - ejbca_data:/mnt/persistent
    networks:
      - ejbca_network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/ejbca/publicweb/healthcheck/ejbcahealth"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 120s

volumes:
  postgres_data:
  ejbca_data:

networks:
  ejbca_network:
    driver: bridge
```

### Passo 3: Iniciar os serviços

```bash
docker-compose up -d
```

### Passo 4: Acompanhar logs

```bash
docker-compose logs -f ejbca
```

### Passo 5: Verificar status

```bash
curl -k https://localhost:8443/ejbca/publicweb/healthcheck/ejbcahealth
```

Resposta esperada:
```
ALLOK
```

## Configuração inicial

### 1. Importar certificado de administrador

```bash
# Gerar certificado de superadmin
docker exec ejbca /opt/primekey/bin/ejbca.sh ra addendentity \
  --username superadmin \
  --dn "CN=SuperAdmin,O=Cartorio Digital,C=BR" \
  --caname "ManagementCA" \
  --type 1 \
  --token P12

docker exec ejbca /opt/primekey/bin/ejbca.sh batch

# Extrair P12
docker cp ejbca:/opt/primekey/superadmin.p12 ./superadmin.p12
```

Importe `superadmin.p12` no navegador conforme instruções anteriores.

### 2. Acessar Admin Web

Após importar o certificado, acesse:

```
https://localhost:8443/ejbca/adminweb/
```

Você verá o dashboard administrativo do EJBCA-CE.

## Criando sua primeira CA

### 1. Navegar para CA Functions

No menu esquerdo: **Certification Authorities** → **Create CA**

### 2. Configurar a CA

**Basic Configuration:**
- **CA Name:** `Cartorio-Root-CA`
- **Subject DN:** `CN=Cartorio Digital Root CA,O=Cartorio Digital,C=BR`
- **Validity:** `3650` dias (10 anos)

**Key Configuration:**
- **Key Algorithm:** `RSA`
- **Key Size:** `4096` bits
- **Signature Algorithm:** `SHA256WithRSA`

**Certificate Policy:**
- **Policy ID:** `1.2.3.4.5.6.7.1` (OID customizado)
- **CPS URI:** `http://cartorio.gov.br/cps`

**CRL Configuration:**
- **CRL Period:** `86400000` ms (24 horas)
- **CRL Overlap:** `600000` ms (10 minutos)
- **CRL Distribution Point:** `http://crl.cartorio.gov.br/root-ca.crl`

Clique em **Create**.

### 3. Criar CA intermediária

Repita o processo com:

- **CA Name:** `Cartorio-Issuing-CA`
- **Subject DN:** `CN=Cartorio Digital Issuing CA,O=Cartorio Digital,C=BR`
- **Validity:** `1825` dias (5 anos)
- **Signed By:** `Cartorio-Root-CA`

## Criando perfis de certificado

### 1. Certificate Profile para TLS Server

**Certification Authorities** → **Certificate Profiles** → **Add**

- **Profile Name:** `TLS-Server-Profile`
- **Type:** `End Entity`
- **Available Key Algorithms:** RSA 2048, RSA 4096, ECDSA P-256
- **Key Usage (Critical):**
  - ☑ Digital Signature
  - ☑ Key Encipherment
- **Extended Key Usage (Critical):**
  - ☑ Server Authentication (1.3.6.1.5.5.7.3.1)
- **Validity:** `90` dias

### 2. End Entity Profile para servidores

**RA Functions** → **End Entity Profiles** → **Add**

- **Profile Name:** `TLS-Server-Entity`
- **Subject DN Attributes:**
  - `CN` – Common Name (Required, Modifiable)
  - `O` – Organization (Required, Fixed: "Cartorio Digital")
  - `C` – Country (Required, Fixed: "BR")
- **Subject Alternative Name:**
  - `DNS Name` (Required, Modifiable, Can have multiple)
- **Available CAs:** `Cartorio-Issuing-CA`
- **Default Certificate Profile:** `TLS-Server-Profile`
- **Default Token:** `P12 File`

## Emitindo seu primeiro certificado

### Via Admin Web (Manual)

1. **RA Functions** → **Add End Entity**
2. Preencher:
   - **Username:** `api-cartorio`
   - **Password:** `changeme` (para retirar o certificado)
   - **CN:** `api.cartorio.local`
   - **DNS Name:** `api.cartorio.local`
   - **End Entity Profile:** `TLS-Server-Entity`
   - **Certificate Profile:** `TLS-Server-Profile`
   - **CA:** `Cartorio-Issuing-CA`
3. Clicar em **Add**
4. **Create Browser Certificate** → Será gerado e baixado um arquivo P12

### Via CLI (Automático)

```bash
docker exec -it ejbca /opt/primekey/bin/ejbca.sh ra addendentity \
  --username api-cartorio \
  --dn "CN=api.cartorio.local,O=Cartorio Digital,C=BR" \
  --caname "Cartorio-Issuing-CA" \
  --type 1 \
  --token USERGENERATED \
  --altname "dnsName=api.cartorio.local"

docker exec -it ejbca /opt/primekey/bin/ejbca.sh batch
```

### Via REST API

```bash
curl -X POST https://localhost:8443/ejbca/ejbca-rest-api/v1/certificate/enrollkeystore \
  --cert superadmin.p12:ejbca \
  --header "Content-Type: application/json" \
  --data '{
    "username": "api-cartorio",
    "password": "changeme",
    "key_alg": "RSA",
    "key_spec": "2048"
  }' \
  -o api-cartorio.p12
```

## Configurando OCSP

### 1. Criar OCSP Signing Certificate

```bash
docker exec -it ejbca /opt/primekey/bin/ejbca.sh ra addendentity \
  --username ocsp-signer \
  --dn "CN=OCSP Signer,O=Cartorio Digital,C=BR" \
  --caname "Cartorio-Issuing-CA" \
  --type 1 \
  --token P12
```

### 2. Configurar OCSP Service

**System Configuration** → **OCSP Configuration**

- **OCSP Signing Certificate:** Selecionar `ocsp-signer`
- **Response Validity:** `600` segundos (10 minutos)
- **Include Certificate Chain:** ☑
- **Nonce:** ☑ Enable

### 3. Testar OCSP

```bash
# Obter certificado emitido
openssl s_client -connect localhost:8443 -showcerts </dev/null 2>/dev/null | \
  openssl x509 -outform PEM > server.pem

# Consultar OCSP
openssl ocsp \
  -issuer issuing-ca.pem \
  -cert server.pem \
  -url http://localhost:8080/ejbca/publicweb/status/ocsp \
  -text
```

Resposta esperada:
```
Response verify OK
server.pem: good
```

## Backup e restauração

### Fazer backup

```bash
# Parar o container
docker-compose down

# Backup do volume de dados
docker run --rm -v ejbca_data:/data -v $(pwd):/backup \
  ubuntu tar czf /backup/ejbca-backup-$(date +%Y%m%d).tar.gz -C /data .

# Backup do banco PostgreSQL
docker run --rm -v postgres_data:/data -v $(pwd):/backup \
  ubuntu tar czf /backup/postgres-backup-$(date +%Y%m%d).tar.gz -C /data .

# Reiniciar
docker-compose up -d
```

### Restaurar backup

```bash
docker-compose down

# Restaurar dados EJBCA
docker run --rm -v ejbca_data:/data -v $(pwd):/backup \
  ubuntu tar xzf /backup/ejbca-backup-YYYYMMDD.tar.gz -C /data

# Restaurar banco PostgreSQL
docker run --rm -v postgres_data:/data -v $(pwd):/backup \
  ubuntu tar xzf /backup/postgres-backup-YYYYMMDD.tar.gz -C /data

docker-compose up -d
```

## Solução de problemas

### Container não inicia

```bash
# Ver logs completos
docker logs ejbca

# Verificar uso de recursos
docker stats ejbca

# Reiniciar clean
docker-compose down -v
docker-compose up -d
```

### Erro de certificado no navegador

- Certifique-se de importar o `superadmin.p12` corretamente
- Limpe o cache de certificados do navegador
- Tente em modo anônimo/privado

### Banco de dados não conecta

```bash
# Testar conectividade
docker exec ejbca psql -h database -U ejbca -d ejbca -c "SELECT version();"

# Ver logs do PostgreSQL
docker logs ejbca_db
```

### OCSP não responde

```bash
# Verificar se o serviço está ativo
curl http://localhost:8080/ejbca/publicweb/status/ocsp

# Ver logs de OCSP
docker exec ejbca grep OCSP /opt/wildfly/standalone/log/server.log
```

## Próximos passos

Agora que você tem um ambiente EJBCA-CE funcional, no próximo capítulo você aprenderá sobre **gestão completa do ciclo de vida de certificados**, incluindo renovação, revogação e auditoria.

## Referências

- **EJBCA Docker Hub:** [https://hub.docker.com/r/keyfactor/ejbca-ce](https://hub.docker.com/r/keyfactor/ejbca-ce)
- **EJBCA Installation Guide:** [https://doc.primekey.com/ejbca/ejbca-installation](https://doc.primekey.com/ejbca/ejbca-installation)
- **Docker Compose Best Practices:** [https://docs.docker.com/compose/production/](https://docs.docker.com/compose/production/)

