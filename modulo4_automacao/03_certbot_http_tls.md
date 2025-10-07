# ACME com desafios HTTP-01 e TLS-ALPN-01

Após compreender o protocolo ACME no capítulo anterior, vamos colocar a teoria em prática: usar clientes ACME para responder aos desafios **HTTP-01** e **TLS-ALPN-01**. Esses métodos permitem obter certificados de forma automática, sem intervenção manual, algo essencial para os serviços do cartório digital.

## HTTP-01 (Standalone e Webroot)

No desafio HTTP-01 o servidor ACME valida o controle do domínio acessando um arquivo estático em `http://<domínio>/.well-known/acme-challenge/<token>`. Para isso você precisa de acesso à porta 80. Existem dois modos comuns de usar o `certbot`:

Antes de acionar o modo **standalone**, imagine o plantão noturno do cartório: a aplicação de emissão de certidões precisa renovar o certificado, mas o servidor entrou em manutenção e nenhum serviço web está disponível. O comando abaixo entra como solução rápida, abrindo temporariamente a porta 80 para que a validação aconteça sem depender de um servidor dedicado.

```bash
sudo certbot certonly --standalone -d seudominio.com \
  --email seu@email.com --agree-tos --non-interactive
```

Quando o cartório já mantém um Nginx servindo o portal de solicitações de certidões, porém a equipe teme tocar na infraestrutura para não interromper o atendimento digital, o modo **webroot** resolve: o certbot escreve o arquivo do desafio no diretório publicado e o fluxo segue em produção sem paradas.

```bash
sudo certbot certonly --webroot -w /var/www/html \
  -d seudominio.com -d www.seudominio.com \
  --email seu@email.com --agree-tos --non-interactive
```

> Nota inspiradora: cada certificado emitido mantém a plataforma de certidões eletrônicas e as APIs internas de registro acolhidas sob uma camada de confiança, garantindo que cidadãos e sistemas parceiros encontrem um cartório moderno e seguro.

Use a opção `--test-cert` para usar o ambiente de staging da Let’s Encrypt durante seus testes, evitando o limite de emissões:

```bash
sudo certbot certonly --standalone --test-cert -d seudominio.com
```

Após a emissão, os certificados ficam em `/etc/letsencrypt/live/seudominio.com/` (`fullchain.pem` e `privkey.pem`). No módulo seguinte você aprenderá a automatizar a configuração do servidor.

## TLS-ALPN-01

O desafio TLS-ALPN-01 usa a porta 443 em vez da 80. Ele verifica se você controla o domínio por meio de uma conexão TLS com protocolo ALPN `acme-tls/1`. É útil quando a porta 80 está bloqueada (por exemplo, devido a regras de firewall). Para usar esse desafio:

Visualize o cenário do cartório com firewalls rígidos que nunca permitem a abertura da porta 80 para a internet, porque as integrações com órgãos públicos trafegam exclusivamente via TLS. O comando a seguir contorna o impasse e mantém a rotina de emissão de certificados em dia.

```bash
sudo certbot certonly --standalone \
  --preferred-challenges tls-alpn-01 \
  -d seudominio.com --test-cert
```

Outra alternativa é o **step-cli**. Instale o Step se ainda não o fez (veja https://github.com/smallstep/cli). Em seguida:

```bash
step ca certificate seudominio.com seudominio.crt seudominio.key \
  --acme https://acme-staging-v02.api.letsencrypt.org/directory \
  --rsa
```

O step-cli gerará o certificado e a chave e responderá automaticamente ao desafio TLS-ALPN-01.

> Nota inspiradora: sustentar o cadeado verde nas APIs internas de escrituras e averbações é como abrir as portas do cartório para o futuro—cada integração segura reforça a confiança do cidadão nos serviços digitais.

## Atividades

- Quando um ambiente de contingência fica sem servidor web, reproduza essa situação com um domínio de testes e resolva-a emitindo um certificado pelo método standalone e outro via webroot; ao final, observe os arquivos gerados e o tempo de validade para confirmar a recuperação completa.
- Sempre que o time de atendimento duvidar do status dos certificados que protegem o serviço de certidões online, responda com dados: rode `certbot certificates` e apresente a lista atualizada como evidência da solução.
- Se a equipe de tecnologia questionar a integridade dos certificados usados pelas APIs internas, simule o desafio TLS-ALPN-01 com o step-cli e, como prova final, utilize `openssl x509 -noout -text -in seudominio.crt` para mostrar cada detalhe criptográfico alinhado ao plano de continuidade.

No próximo capítulo veremos como usar o método DNS-01 para obter certificados wildcard no Route 53.
