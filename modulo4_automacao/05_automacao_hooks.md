
# Automação de renovações e hooks

Os certificados Let's Encrypt e de outras autoridades ACME geralmente têm validade de 90 dias. Se um único deles expira, os portais de certidões do cartório ficam fora do ar, os cidadãos perdem o acesso e a auditoria de conformidade registra o incidente como falha operacional grave. Automatizar a renovação é fundamental para evitar interrupções e relatórios negativos. Neste capítulo você aprenderá a configurar timers e **hooks** para atualizar serviços após a renovação, com foco nas dores reais que já enfrentamos.

## Cron ou systemd timer

Durante uma madrugada de fechamento contábil, descobrimos que o `certbot.timer` estava desabilitado; o certificado venceu, as APIs recusaram conexões mTLS e o SLA de atendimento foi rompido. Para não repetir esse episódio, o primeiro passo de qualquer desenvolvedor pleno é verificar se o timer está ativo:

```bash
sudo systemctl list-timers certbot.timer
```

Quando o comando não exibe o timer, trate como prioridade zero: habilitar agora significa evitar uma nova parada inesperada e as chamadas urgentes de diretoria.

```bash
sudo systemctl enable --now certbot.timer
```

Isso executará `certbot renew` duas vezes ao dia. Já em servidores legados que não contam com systemd — caso clássico de integrações com prefeituras — tivemos incidentes porque ninguém lembrou de criar um agendamento alternativo. Evite expiração configurando um cron manual documentado no repositório da equipe:

```bash
0 */12 * * * root certbot renew --quiet
```

## Hooks de deploy

Em mais de uma investigação pós-incidente percebemos que o certificado era renovado, mas os serviços continuavam servindo a cadeia antiga. Faltava automatizar o pós-renovação. Durante o comando `renew`, você pode especificar scripts que serão executados e impedir tanto downtime quanto não conformidades com o Plano de Continuidade Operacional:

- `--pre-hook`: executa antes da tentativa de renovacao (por exemplo, parar um servidor que esta ocupando a porta 80/443).
- `--post-hook`: executa apos cada tentativa de renovacao, independentemente de sucesso.
- `--deploy-hook`: executa apenas quando um certificado e efetivamente renovado.

Imagine que o Nginx precisa carregar o certificado novo sem derrubar o serviço. Em um episódio real, a falta de `reload` obrigou a equipe a reiniciar manualmente o proxy em horário de pico. Para não reviver a dor, utilize um deploy hook que só dispara quando houver material novo:

```bash
sudo certbot renew \
  --deploy-hook "systemctl reload nginx"
```

Para ambientes que passam por auditorias de compliance, criamos um script `deploy_cert.sh` versionado no repositório do time. Ele registra logs claros, copia os artefatos para o diretório monitorado e executa apenas um `reload`, preservando conexões ativas enquanto aplica o novo material criptográfico — requisito cobrado pelos fiscais para garantir continuidade operacional. Crie o arquivo com o conteúdo a seguir:

    #!/bin/bash
    # Este script sera chamado automaticamente pelo certbot com variaveis de ambiente
    echo "Implantando novo certificado..."
    cp "$RENEWED_LINEAGE/fullchain.pem" /etc/nginx/ssl/cert.pem
    cp "$RENEWED_LINEAGE/privkey.pem" /etc/nginx/ssl/key.pem
    systemctl reload nginx
    echo "Certificado aplicado com sucesso!"

Salve-o em `/etc/letsencrypt/renewal-hooks/deploy/deploy_cert.sh` e torne-o executavel (`chmod +x deploy_cert.sh`). O certbot executara esse script apenas quando um novo certificado for emitido, mantendo o portal disponivel durante todo o processo.

Em outro incidente, o compliance apontou que o certificado aplicado no AWS Certificate Manager estava vencido mesmo após a renovação local. Resolver isso exigiu inserir a importação no próprio hook, garantindo que os balanceadores gerenciados recebam o material válido imediatamente:

    aws acm import-certificate \
      --certificate fileb://"$RENEWED_LINEAGE/cert.pem" \
      --private-key fileb://"$RENEWED_LINEAGE/privkey.pem" \
      --certificate-chain fileb://"$RENEWED_LINEAGE/chain.pem" \
      --certificate-arn arn:aws:acm:REGIAO:CONTA:certificate/ID

### Testando e depurando

Por fim, já tivemos hooks quebrados por falta de permissão e ninguém notou até o próximo vencimento. Para evitar que renovações silenciosas falhem sem alerta, inclua ensaios regulares com o modo de teste e confirme o comportamento dos scripts:

```bash
sudo certbot renew --dry-run
```

Verifique se os hooks sao executados conforme esperado e ajuste permissoes ou caminhos se necessario.

### Atividades

- Habilite o `certbot.timer` ou agende um cron no seu servidor de testes.
- Crie um `deploy-hook` script para copiar os certificados e reiniciar o servico (ou importar no ACM).
- Realize um `certbot renew --dry-run` e verifique nos logs se o hook foi chamado.
- Experimente adicionar um `post-hook` que envia uma notificacao (e.g. Slack) quando houver renovacao.
