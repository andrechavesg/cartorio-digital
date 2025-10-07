
# Automação de renovações e hooks

Os certificados Let's Encrypt e de outras autoridades ACME geralmente têm validade de 90 dias. Automatizar a renovação é fundamental para evitar interrupções. Neste capítulo você aprenderá a configurar timers e **hooks** para atualizar serviços após a renovação.

## Cron ou systemd timer

O Certbot instala um timer systemd por padrão nas distribuições modernas. Para visualizar:

```bash
sudo systemctl list-timers certbot.timer
```

Se não estiver habilitado, ative:

```bash
sudo systemctl enable --now certbot.timer
```

Isso executara `certbot renew` duas vezes ao dia. Voce tambem pode agendar renovacoes via cron:

```bash
0 */12 * * * root certbot renew --quiet
```

## Hooks de deploy

Durante o comando `renew`, voce pode especificar scripts que serao executados:

- `--pre-hook`: executa antes da tentativa de renovacao (por exemplo, parar um servidor que esta ocupando a porta 80/443).
- `--post-hook`: executa apos cada tentativa de renovacao, independentemente de sucesso.
- `--deploy-hook`: executa apenas quando um certificado e efetivamente renovado.

Exemplo para recarregar o Nginx somente quando um certificado mudou:

```bash
sudo certbot renew \
  --deploy-hook "systemctl reload nginx"
```

Voce
pode escrever um script `deploy_cert.sh` mais complexo:
Crie um script `deploy_cert.sh` que copia os arquivos renovados para o local usado pelo seu serviço e reinicia ou reload o serviço. Por exemplo:

    #!/bin/bash
    # Este script sera chamado automaticamente pelo certbot com variaveis de ambiente
    echo "Implantando novo certificado..."
    cp "$RENEWED_LINEAGE/fullchain.pem" /etc/nginx/ssl/cert.pem
    cp "$RENEWED_LINEAGE/privkey.pem" /etc/nginx/ssl/key.pem
    systemctl reload nginx
    echo "Certificado aplicado com sucesso!"

Salve-o em `/etc/letsencrypt/renewal-hooks/deploy/deploy_cert.sh` e torne-o executável (`chmod +x deploy_cert.sh`). O certbot executara esse script apenas quando um novo certificado for emitido.

Para ambientes AWS, voce pode substituir os comandos de copia por chamadas da AWS CLI para importar o certificado no ACM:

    aws acm import-certificate \
      --certificate fileb://"$RENEWED_LINEAGE/cert.pem" \
      --private-key fileb://"$RENEWED_LINEAGE/privkey.pem" \
      --certificate-chain fileb://"$RENEWED_LINEAGE/chain.pem" \
      --certificate-arn arn:aws:acm:REGIAO:CONTA:certificate/ID

### Testando e depurando

Utilize `sudo certbot renew --dry-run` para testar a renovacao sem de fato emitir um novo certificado. Verifique se os scripts de hooks sao executados conforme esperado e ajuste permissoes ou caminhos se necessario.

### Atividades

- Habilite o `certbot.timer` ou agende um cron no seu servidor de testes.
- Crie um `deploy-hook` script para copiar os certificados e reiniciar o servico (ou importar no ACM).
- Realize um `certbot renew --dry-run` e verifique nos logs se o hook foi chamado.
- Experimente adicionar um `post-hook` que envia uma notificacao (e.g. Slack) quando houver renovacao.
