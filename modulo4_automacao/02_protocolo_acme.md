# ACME: como funciona o protocolo de automação de certificados

O Automatic Certificate Management Environment (ACME) é um protocolo padronizado pelo IETF (RFC 8555) que permite que clientes solicitem, renovem e revoguem certificados automaticamente junto às autoridades certificadoras (ACs) sem intervenção humana. Ele define um fluxo HTTP seguro entre **cliente ACME** e **servidor ACME** (como o Let’s Encrypt) usando assinaturas JWS (JSON Web Signature).

## Visão geral do fluxo

1. **Registro de conta:** O cliente gera uma chave (account key) e registra‑se no servidor ACME informando um endereço de e‑mail para contato. Isso cria uma “conta” que será usada para assinar todas as requisições seguintes.
2. **Ordem de certificado:** Para solicitar um certificado, o cliente cria uma *ordem* (order) especificando os domínios (identidades) desejados.
3. **Autorizações e desafios:** Para cada domínio, o servidor ACME cria uma *autorização* e associa um ou mais *desafios* (challenges) que comprovam o controle sobre o domínio. Os principais tipos são:
   - **HTTP‑ 01:** O cliente publica um token em um arquivo HTTP acessível em `http://<dominio>/.well-known/acme-challenge/<token>`. O servidor valida fazendo uma requisição HTTP.
   - **DNS‑ 01:** O cliente cria um registro TXT `_acme-challenge.<dominio>` contendo o token de validação. O servidor valida consultando o DNS.
   - **TLS‑ALPN‑ 01:** O cliente responde na porta 443 com um certificado especial contendo o token no campo `acmeIdentifier` (menos usado, mas útil para ambientes sem HTTP/80).
4. **Validação:** Uma vez que o desafio é satisfeito, o servidor marca a autorização como `valid`.
5. **Emissão:** Quando todas as autorizações de uma ordem estão válidas, o cliente envia um *CSR* (Certificate Signing Request) e o servidor emite o certificado.
6. **Renovação/Revogação:** O mesmo fluxo é usado para renovar ou revogar certificados quando necessário.

## Trabalhando com contas e chaves

Para usar ACME você precisa de uma **account key**. Ferramentas como `certbot` gerenciam isso automaticamente, mas você pode explorar o protocolo manualmente com `openssl` e `curl` para fins educacionais:

```bash
# Criar uma chave RSA para a conta ACME
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out account.key

# Inscrever uma conta de teste no Let's Encrypt (staging)
# Este passo é normalmente feito por certbot automaticamente
```

## Desafios na prática

No próximo capítulo você verá como um cliente ACME (Certbot ou Step‑CLI) abstrai esses detalhes, mas é importante entender as diferenças:

- **HTTP‑ 01** é simples e funciona quando o domínio aponta para seu servidor na porta 80. Não serve para certificados wildcard (`*.dominio.com`).
- **DNS‑ 01** é indispensável para certificados wildcard ou quando você não pode abrir a porta 80. Requer acesso ao provedor DNS para criar registros TXT.
- **TLS‑ALPN‑ 01** utiliza a porta 443 e um certificado temporário, sendo útil em ambientes onde a porta 80 está bloqueada.

Cada desafio tem requisitos de rede e automação diferentes. Escolha aquele que se encaixa melhor no seu cenário.

### Exercício sugerido

1. Leia a RFC 8555 (seção 1 e 7) para compreender a terminologia (Account, Order, Authorization, Challenge).
2. Explore o endpoint de diretório do Let's Encrypt *staging* usando `curl`:
   ```bash
   curl https://acme-staging-v02.api.letsencrypt.org/directory | jq .
   ```
3. Identifique os URLs para *newAccount* e *newOrder* retornados no JSON; isso é o coração do protocolo ACME.
