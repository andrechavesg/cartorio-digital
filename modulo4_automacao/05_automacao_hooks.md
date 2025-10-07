
# Automação de renovações e hooks

Os certificados Let's Encrypt e de outras autoridades ACME geralmente têm validade de 90 dias. Automatizar a renovação é fundamental para evitar interrupções. Neste capítulo você aprenderá a configurar timers e **hooks** para atualizar serviços após a renovação.

## Cron ou systemd timer

Quando ha risco de interrupcao porque o certificado expirou sem que o timer rodasse, confira se o timer do Certbot esta ativo:

```bash
sudo systemctl list-timers certbot.timer
```

Caso identifique que o timer nao esta habilitado e queira evitar uma nova parada inesperada, execute:

```bash
sudo systemctl enable --now certbot.timer
```

Isso executara `certbot renew` duas vezes ao dia. Quando o ambiente nao possui systemd ou voce precisa garantir a renovacao em um servidor legado, evite expiracao configurando um cron manual:

```bash
0 */12 * * * root certbot renew --quiet
```

## Hooks de deploy

Durante o comando `renew`, voce pode especificar scripts que serao executados:

- `--pre-hook`: executa antes da tentativa de renovacao (por exemplo, parar um servidor que esta ocupando a porta 80/443).
- `--post-hook`: executa apos cada tentativa de renovacao, independentemente de sucesso.
- `--deploy-hook`: executa apenas quando um certificado e efetivamente renovado.

Imagine que o Nginx precisa carregar o certificado novo sem derrubar o servico; nesse cenario, utilize um deploy hook para recarregar o processo apenas quando houver mudanca:

```bash
sudo certbot renew \
  --deploy-hook "systemctl reload nginx"
```

Voce pode escrever um script `deploy_cert.sh` mais completo. Esse script garante que o portal do cartorio digital permaneça acessivel, porque ele copia os certificados para o diretorio monitorado pelo Nginx e realiza apenas um `reload`, preservando as conexoes existentes enquanto aplica o novo material criptografico. Crie o arquivo com o conteudo a seguir:

    #!/bin/bash
    # Este script sera chamado automaticamente pelo certbot com variaveis de ambiente
    echo "Implantando novo certificado..."
    cp "$RENEWED_LINEAGE/fullchain.pem" /etc/nginx/ssl/cert.pem
    cp "$RENEWED_LINEAGE/privkey.pem" /etc/nginx/ssl/key.pem
    systemctl reload nginx
    echo "Certificado aplicado com sucesso!"

Salve-o em `/etc/letsencrypt/renewal-hooks/deploy/deploy_cert.sh` e torne-o executavel (`chmod +x deploy_cert.sh`). O certbot executara esse script apenas quando um novo certificado for emitido, mantendo o portal disponivel durante todo o processo.

Quando o objetivo e substituir rapidamente um certificado expirado no AWS Certificate Manager (ACM) e liberar o acesso para balanceadores gerenciados, troque as copias locais por uma importacao via AWS CLI:

    aws acm import-certificate \
      --certificate fileb://"$RENEWED_LINEAGE/cert.pem" \
      --private-key fileb://"$RENEWED_LINEAGE/privkey.pem" \
      --certificate-chain fileb://"$RENEWED_LINEAGE/chain.pem" \
      --certificate-arn arn:aws:acm:REGIAO:CONTA:certificate/ID

### Testando e depurando

Para evitar que renovacoes silenciosas falhem sem alerta — problema comum quando hooks quebram ou permissoes mudam — faca um ensaio com o modo de teste e confirme o comportamento dos scripts:

```bash
sudo certbot renew --dry-run
```

Verifique se os hooks sao executados conforme esperado e ajuste permissoes ou caminhos se necessario.

### Atividades

- Habilite o `certbot.timer` ou agende um cron no seu servidor de testes.
- Crie um `deploy-hook` script para copiar os certificados e reiniciar o servico (ou importar no ACM).
- Realize um `certbot renew --dry-run` e verifique nos logs se o hook foi chamado.
- Experimente adicionar um `post-hook` que envia uma notificacao (e.g. Slack) quando houver renovacao.
