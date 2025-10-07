# ACME: como funciona o protocolo de automação de certificados

Quando um certificado expira em produção, cada requisição HTTPS começa a falhar e o cartório digital enfrenta uma cascata de erros em APIs e portais. O Automatic Certificate Management Environment (ACME) surge justamente para evitar essa ruptura: é um protocolo padronizado pelo IETF (RFC 8555) que permite que clientes solicitem, renovem e revoguem certificados automaticamente junto às autoridades certificadoras (ACs) sem intervenção humana. Ele define um fluxo HTTP seguro entre **cliente ACME** e **servidor ACME** (como o Let’s Encrypt) usando assinaturas JWS (JSON Web Signature), devolvendo à operação o controle preventivo sobre renovações.

## Visão geral do fluxo

Quando não dominamos o fluxo ACME, a equipe corre o risco de descobrir uma renovação falha apenas quando os usuários já enfrentam indisponibilidade. O passo a passo abaixo funciona como antídoto: entender cada fase prepara você para monitorar e automatizar o processo antes que os certificados do ambiente produtivo expirem silenciosamente.

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

Uma falha comum de renovação ocorre quando o time perde a chave de conta (account key) e precisa revalidar todos os domínios às pressas. Prevenir esse caos significa manter uma identidade criptográfica bem gerenciada. Ferramentas como `certbot` fazem isso automaticamente, mas você pode explorar o protocolo manualmente com `openssl` e `curl` para fins educacionais, garantindo que saiba recuperar ou recriar a chave antes que a produção pare:

Pense no nosso cartório digital avançando rumo ao objetivo maior de oferecer renovação contínua de certificados para cada serviço crítico. Em um cenário real, a equipe de infraestrutura percebe que depender de renovações manuais ameaça o compromisso de estabilidade que assumimos no projeto principal, por isso decide registrar uma conta ACME dedicada para o cluster de aplicações. O comando abaixo materializa esse movimento estratégico: ao gerar a chave da conta, criamos a identidade criptográfica que permitirá vincular os domínios do cartório a renovações automáticas e orquestradas, sustentando a visão de automação confiável.

```bash
# Criar uma chave RSA para a conta ACME
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out account.key

# Inscrever uma conta de teste no Let's Encrypt (staging)
# Este passo é normalmente feito por certbot automaticamente
```

## Desafios na prática

Outra causa frequente de incidentes é escolher um desafio incompatível com o ambiente e perceber o erro apenas quando o certificado já venceu. Antecipe o risco analisando os desafios a seguir: ao mapear portas expostas e integrações DNS, você reduz o intervalo entre a expiração e a correção de serviço.

No próximo capítulo você verá como um cliente ACME (Certbot ou Step‑CLI) abstrai esses detalhes, mas é importante entender as diferenças:

- **HTTP‑ 01** é simples e funciona quando o domínio aponta para seu servidor na porta 80. Não serve para certificados wildcard (`*.dominio.com`).
- **DNS‑ 01** é indispensável para certificados wildcard ou quando você não pode abrir a porta 80. Requer acesso ao provedor DNS para criar registros TXT.
- **TLS‑ALPN‑ 01** utiliza a porta 443 e um certificado temporário, sendo útil em ambientes onde a porta 80 está bloqueada.

Cada desafio tem requisitos de rede e automação diferentes. Escolha aquele que se encaixa melhor no seu cenário.

### Exercício sugerido

1. Leia a RFC 8555 (seção 1 e 7) para compreender a terminologia (Account, Order, Authorization, Challenge).
2. Explore o endpoint de diretório do Let's Encrypt *staging* usando `curl`:
   Ao preparar o lançamento do portal unificado do cartório, nossa equipe de observabilidade precisa auditar o diretório ACME que sustentará as renovações contínuas dos certificados do ambiente de produção. A consulta com `curl` ajuda a confirmar que cada URL exposto pelo serviço está alinhado com os fluxos automatizados que integraremos aos pipelines do projeto principal, reforçando a trilha inspiradora de construir uma operação resiliente e auditável.
   ```bash
   curl https://acme-staging-v02.api.letsencrypt.org/directory | jq .
   ```
3. Identifique os URLs para *newAccount* e *newOrder* retornados no JSON; isso é o coração do protocolo ACME.
