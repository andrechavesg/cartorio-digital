# ACME com desafios HTTP-01 e TLS-ALPN-01

Após compreender o protocolo ACME no capítulo anterior, vamos colocar a teoria em prática: usar clientes ACME para responder aos desafios **HTTP-01** e **TLS-ALPN-01**. Esses métodos permitem obter certificados de forma automática, sem intervenção manual, algo essencial para os serviços do cartório digital.

## HTTP-01 (Standalone e Webroot)

No desafio HTTP-01 o servidor ACME valida o controle do domínio acessando um arquivo estático em `http://<domínio>/.well-known/acme-challenge/<token>`. Para isso você precisa de acesso à porta 80. Existem dois modos comuns de usar o `certbot`:

- **Standalone**: o certbot abre temporariamente a porta 80 para servir o desafio. Útil para servidores sem servidor web pré-instalado.
  
  ```bash
  sudo certbot certonly --standalone -d seudominio.com \
    --email seu@email.com --agree-tos --non-interactive
  ```

- **Webroot**: se você já possui um servidor web (Nginx ou Apache), o certbot grava o arquivo de desafio em um diretório público. Você informa o diretório com `--webroot-path`:

  ```bash
  sudo certbot certonly --webroot -w /var/www/html \
    -d seudominio.com -d www.seudominio.com \
    --email seu@email.com --agree-tos --non-interactive
  ```

Use a opção `--test-cert` para usar o ambiente de staging da Let’s Encrypt durante seus testes, evitando o limite de emissões:

```bash
sudo certbot certonly --standalone --test-cert -d seudominio.com
```

Após a emissão, os certificados ficam em `/etc/letsencrypt/live/seudominio.com/` (`fullchain.pem` e `privkey.pem`). No módulo seguinte você aprenderá a automatizar a configuração do servidor.

## TLS-ALPN-01

O desafio TLS-ALPN-01 usa a porta 443 em vez da 80. Ele verifica se você controla o domínio por meio de uma conexão TLS com protocolo ALPN `acme-tls/1`. É útil quando a porta 80 está bloqueada (por exemplo, devido a regras de firewall). Para usar esse desafio:

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

## Atividades

- Escolha um domínio de testes (ou registre um subdomínio gratuito) e obtenha um certificado usando o método standalone e outro com webroot. Observe os arquivos gerados e o tempo de validade.
- Use `certbot certificates` para listar e verificar seus certificados instalados.
- Simule o desafio TLS-ALPN-01 com o step-cli e verifique se o certificado é válido usando `openssl x509 -noout -text -in seudominio.crt`.

No próximo capítulo veremos como usar o método DNS-01 para obter certificados wildcard no Route 53.
