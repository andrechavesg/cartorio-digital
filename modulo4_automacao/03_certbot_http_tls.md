# ACME com desafios HTTP-01 e TLS-ALPN-01

Quando um certificado expira de madrugada e a aplicação fica inacessível, a pergunta que ecoa é: “por que ninguém automatizou isso?” Após compreender o protocolo ACME no capítulo anterior, vamos colocar a teoria em prática e mostrar como responder aos desafios **HTTP-01** e **TLS-ALPN-01** resolve riscos clássicos — indisponibilidade por falta de servidor web e restrições de firewall que barram a porta 80.

## HTTP-01 (Standalone e Webroot)

O primeiro incidente que o time enfrentou ocorreu porque não havia ninguém para subir o servidor HTTP quando o certificado venceu. O desafio HTTP-01 valida o domínio acessando um arquivo estático em `http://<domínio>/.well-known/acme-challenge/<token>`. Em vez de esperar a pane repetir, configuramos o `certbot` para assumir o controle.

**Por que standalone?** Quando toda a infraestrutura está em manutenção ou os containers de front-end foram desligados, a renovação falha e os usuários veem erros TLS. O modo standalone abre temporariamente a porta 80 para responder ao desafio, garantindo que a validação aconteça mesmo na ausência de um web server permanente. Só depois de entender esse cenário é que executamos o comando:

```bash
sudo certbot certonly --standalone -d seudominio.com \
  --email seu@email.com --agree-tos --non-interactive
```

**Por que webroot?** Em outra semana, a equipe evitava mexer no Nginx em produção com medo de quebrar o atendimento digital. A renovação automática falhou porque ninguém criou o arquivo de desafio na pasta servida pelo portal. O modo webroot elimina esse gargalo: o `certbot` deposita o token diretamente no diretório publicado e a validação acontece sem downtime. Só depois de mapear esse risco repetitivo é que incluímos o comando no playbook:

```bash
sudo certbot certonly --webroot -w /var/www/html \
  -d seudominio.com -d www.seudominio.com \
  --email seu@email.com --agree-tos --non-interactive
```

> Nota inspiradora: cada certificado emitido mantém a plataforma de certidões eletrônicas e as APIs internas de registro acolhidas sob uma camada de confiança, garantindo que cidadãos e sistemas parceiros encontrem um cartório moderno e seguro.

Sempre que repetimos o incidente em um ambiente de testes, utilizamos o ambiente de staging da Let’s Encrypt para comprovar que a automação está resiliente sem comprometer as cotas de produção. Por isso adicionamos ao roteiro o uso de `--test-cert` antes de qualquer mudança ousada:

```bash
sudo certbot certonly --standalone --test-cert -d seudominio.com
```

Após a emissão, os certificados ficam em `/etc/letsencrypt/live/seudominio.com/` (`fullchain.pem` e `privkey.pem`). No módulo seguinte você aprenderá a automatizar a configuração do servidor.

## TLS-ALPN-01

As áreas jurídicas exigiram firewalls rígidos que bloqueiam permanentemente a porta 80. Resultado: todo agendamento de renovação via HTTP-01 fracassava, ameaçando APIs que só podem operar em TLS. A resposta é o desafio TLS-ALPN-01, que valida o domínio pela porta 443 com protocolo ALPN `acme-tls/1`. Depois de alinhar com a equipe de redes que esse fluxo é permitido, rodamos o comando abaixo para manter os certificados em dia:

```bash
sudo certbot certonly --standalone \
  --preferred-challenges tls-alpn-01 \
  -d seudominio.com --test-cert
```

Quando precisamos do mesmo procedimento em ambientes com pipelines Go ou scripts customizados, adotamos o **step-cli**. A decisão veio após perceber que algumas equipes não tinham privilégios para instalar o `certbot`, mas podiam acionar binários portáteis. Com o Step já instalado (https://github.com/smallstep/cli), aplicamos o comando como solução padrão para contornar firewalls e garantir a continuidade das renovações:

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
