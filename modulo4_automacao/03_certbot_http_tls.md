# Certbot e desafios HTTP-01

## Introdução

Em pleno horário de pico, o portal do cartório precisava de um certificado renovado. Com o Certbot configurado, bastou executar o agendador e assistir ao log: o desafio HTTP foi respondido automaticamente e o certificado atualizado sem tempo de indisponibilidade. A equipe celebrou a sensação de ter um guardião silencioso trabalhando em segundo plano.

## Conceitos Fundamentais

- **Certbot:** cliente ACME popular que automatiza desafios HTTP-01 e instala certificados em servidores web.
- **Plugins:** integrações específicas para Nginx, Apache ou modo standalone.
- **Renovação agendada:** tarefas cron ou systemd timers mantêm certificados atualizados sem intervenção manual.
- **Segurança operacional:** permissões corretas evitam exposição das chaves privadas.

## Práticas Reais

1. **Instale o Certbot e habilite o plugin adequado:**
   ```bash
   sudo apt install certbot python3-certbot-nginx
   ```

2. **Execute o fluxo automatizado para um domínio de homologação:**
   ```bash
   sudo certbot --nginx -d cartorio.local -d www.cartorio.local --agree-tos --register-unsafely-without-email
   ```
   Revise o arquivo de configuração gerado e garanta que a cadeia interna esteja referenciada.

3. **Programe renovações periódicas:**
   ```bash
   sudo systemctl enable --now certbot.timer
   ```
   Teste a renovação com `sudo certbot renew --dry-run` e registre os resultados.

4. **Documente logs e alertas:** configure notificações para falhas de renovação e mantenha registros de quem revisou cada execução.

## Gancho para o Próximo Capítulo

Alguns domínios do cartório não podem responder desafios HTTP. No próximo capítulo veremos, inspirados por uma integração na nuvem, como automatizar desafios DNS-01 usando provedores como AWS Route 53.
