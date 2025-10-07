# ACME com DNS-01 e Route 53

Agora que você dominou os desafios HTTP-01 e TLS-ALPN-01, vamos explorar o método **DNS-01**. Ele é ideal para emisão de certificados wildcard (por exemplo, `*.cartorio.local`) e para ambientes em que as portas 80 e 443 não estão disponíveis. A validação é feita via inserção de um registro TXT em `_acme-challenge.<domínio>`.

## Por que usar DNS-01?

- Permite gerar certificados para domínios e subdomínios, incluindo curinga (`*.`) em um único certificado;
- Não requer abertura de portas; é ótimo para serviços internos ou ambientes de staging;
- Recomendado para ambientes com automação CI/CD, pois integrações com provedores DNS (como Route 53) facilitam a gestão.

## Configurando credenciais da AWS

Para usar o plugin DNS da AWS você precisa de um **usuário IAM** com permissões mínimas para atualizar registros no Route 53. Crie um usuário com a política `AmazonRoute53FullAccess` ou, preferencialmente, uma política personalizada restringindo o acesso à sua zona hospedada.

Salve as credenciais (Access Key ID e Secret) no arquivo `~/.aws/credentials`:

```
[route53-certbot]
aws_access_key_id=SEU_ACCESS_KEY
aws_secret_access_key=SEU_SECRET_KEY
region=us-east-1
```

Ou defina as variáveis de ambiente `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY`.

## Instalando o plugin DNS-Route53

O `certbot` possui um plugin oficial para Route 53. Instale via gerenciador de pacotes ou `pip`:

```bash
sudo apt-get install python3-certbot-dns-route53
# ou
pip install certbot-dns-route53
```

## Obtendo um certificado wildcard

Depois de instalar o plugin e configurar as credenciais, execute o certbot indicando o plugin DNS-Route53 e os domínios desejados. O exemplo a seguir emite um certificado para `example.com` e `*.example.com` no ambiente de testes (staging) da Let’s Encrypt:

```bash
sudo certbot certonly --dns-route53 \
  -d example.com -d '*.example.com' \
  --email you@example.com --agree-tos \
  --server https://acme-staging-v02.api.letsencrypt.org/directory \
  --non-interactive
```

O plugin criará automaticamente o registro TXT em `_acme-challenge.example.com`, esperará a propagação DNS e, após validação, removerá o registro. Os certificados serão salvos em `/etc/letsencrypt/live/example.com/`.

Para emitir em produção, remova o parâmetro `--server` (o certbot usará o endpoint padrão de produção). Você pode automatizar renovações com um cron ou systemd timer:

```bash
sudo certbot renew --dns-route53 --post-hook 'systemctl reload nginx'
```

## Alternativas e ferramentas

- **lego**: um cliente ACME escrito em Go que suporta diversos provedores DNS. Para AWS use:
  
  ```bash
  AWS_ACCESS_KEY_ID=SEU_ACCESS_KEY \
  AWS_SECRET_ACCESS_KEY=SEU_SECRET_KEY \
  lego --dns route53 -d example.com -d '*.example.com' \
    --email you@example.com run
  ```

- **Smallstep CLI**: o Step também suporta DNS-01 via plugins. Consulte a documentação em https://smallstep.com/docs/step-ca/acme

## Atividades

- Crie um usuário IAM restrito para o Route 53 e registre as credenciais no seu ambiente de desenvolvimento.
- Instale o plugin `certbot-dns-route53` e emita um certificado wildcard para um domínio de testes. Verifique se o arquivo `fullchain.pem` contém os domínios esperados com `openssl x509 -noout -text -in fullchain.pem`.
- Agende uma tarefa de renovação automática e um **hook** que reinicie o servidor ou o load balancer após a renovação.

No próximo capítulo você aprenderá a configurar scripts e **hooks** para implantar automaticamente os novos certificados em seus serviços.
