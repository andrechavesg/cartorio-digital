# ACME com DNS-01 e Route 53

Ao chegarmos à etapa de automação DNS-01 do projeto do cartório digital, já temos filas de casamentos, escrituras e balcões digitais operando 24x7. Para manter essa confiança, precisamos emitir certificados wildcard capazes de cobrir subdomínios como `*.balcao.cartorio.gov.br`, mesmo quando os serviços residem em infraestruturas sem portas públicas expostas. O desafio DNS-01 da ACME resolve esse cenário ao validar a posse do domínio por meio de um registro TXT temporário em `_acme-challenge.<domínio>`.

## Por que usar DNS-01?

- Permite gerar certificados para domínios e subdomínios (inclusive curinga `*.`) em um único certificado, garantindo que todo novo balcão digital receba proteção imediata sem intervenção manual;
- Não exige abertura de portas 80/443, o que habilita as centrais de processamento do cartório a funcionarem em redes privadas ou ambientes com políticas rígidas de firewall;
- Integra-se naturalmente ao nosso pipeline de DevSecOps: cada execução de emissão ou renovação pode ser incorporada às etapas de validação automatizadas antes da liberação de um novo serviço notarial.

Com essa abordagem, conseguimos manter o ritmo de inovação do cartório digital sem sacrificar a segurança.

## Por que o time escolheu o Route 53?

Durante os pilotos do projeto, avaliamos diversos provedores DNS. Optamos pelo Route 53 porque já centralizamos nele as zonas hospedadas do cartório digital e podemos usar identidades IAM para restringir acessos por squad. Além disso, as APIs maduras da AWS simplificam a integração com nosso pipeline GitLab/GitHub Actions, evitando scripts complexos e reduzindo o tempo de auditoria de compliance. A partir daqui cada comando mostrará como operamos nessa plataforma.

## Configurando credenciais da AWS

O primeiro alerta veio quando tentamos renovar o wildcard `*.balcao.cartorio.gov.br` e percebemos que nenhum humano tinha acesso em tempo hábil para criar o registro TXT exigido. Para que a automação crie os desafios em nome do cartório, precisamos de um usuário IAM com permissões mínimas sobre a zona hospedada oficial. Criamos um usuário dedicado ao pipeline de certificados e aplicamos a ele uma política restrita às zonas `cartorio.gov.br` e `intra.cartorio.gov.br`.

Salve as credenciais (Access Key ID e Secret) no arquivo `~/.aws/credentials`. Esse passo permite que o agente de CI conecte-se ao Route 53 durante a fase de emissão:

```
[route53-certbot]
aws_access_key_id=SEU_ACCESS_KEY
aws_secret_access_key=SEU_SECRET_KEY
region=us-east-1
```

Quando o pipeline roda em runners efêmeros, exportamos as variáveis `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY` diretamente na etapa de job. Assim garantimos que o comando de emissão, executado logo após a validação das infraestruturas do cartório, encontre as credenciais certas.

## Instalando o plugin DNS-Route53

Assim que o primeiro deploy automático falhou por falta do módulo DNS, ficou claro que o pipeline só seria confiável se o próprio `certbot` pudesse conversar com o Route 53. O plugin oficial `certbot-dns-route53` resolve isso, permitindo criar e apagar registros TXT sem intervenção manual. Instalamos o plugin no contêiner da pipeline para evitar novas corridas contra o relógio:

```bash
sudo apt-get install python3-certbot-dns-route53
# ou
pip install certbot-dns-route53
```

Esses comandos garantem que, ao dispararmos a automação, o agente possua o módulo necessário para criar e remover os desafios no Route 53 sem intervenção humana.

## Obtendo um certificado wildcard

Quando o certificado wildcard expirou em um domingo, tivemos de subir consoles às pressas para criar o TXT `_acme-challenge`. Com o plugin disponível e as credenciais armazenadas, automatizamos esse passo para que o pipeline execute o `certbot` e renove tudo sozinho. O comando a seguir exemplifica o job que usamos na fase de staging do cartório digital para validar novas integrações de balcões eletrônicos:

```bash
sudo certbot certonly --dns-route53 \
  -d cartorio.gov.br -d '*.balcao.cartorio.gov.br' \
  --email dpo@cartorio.gov.br --agree-tos \
  --server https://acme-staging-v02.api.letsencrypt.org/directory \
  --non-interactive
```

O plugin cria o registro TXT `_acme-challenge.cartorio.gov.br`, aguarda a propagação e, após a validação pela ACME, remove o registro. Em seguida o pipeline armazena o certificado em `/etc/letsencrypt/live/cartorio.gov.br/` e o publica em nosso cofre de segredos para ser consumido pelos ambientes de homologação.

Quando precisamos mover para produção, o mesmo job roda em outra branch do pipeline e remove o parâmetro `--server`, permitindo que a Let’s Encrypt emita o certificado definitivo. Também aprendemos com auditorias que não adianta renovar se o proxy não recarregar o material novo: em um incidente anterior, os certificados ficaram atualizados no disco, mas o Nginx seguiu servindo a cadeia vencida. Para sustentar a continuidade do atendimento no balcão digital, adicionamos uma etapa recorrente de renovação com reload automático dos proxies mTLS que protegem os livros eletrônicos:

```bash
sudo certbot renew --dns-route53 --post-hook 'systemctl reload nginx'
```

Esse comando mantém os certificados atualizados e notifica imediatamente a camada de entrada do cartório, garantindo que o atendimento nunca fique sem criptografia.

## Integração com o pipeline do cartório digital

Nosso fluxo CI/CD executa as etapas de DNS-01 logo após a aprovação de mudanças que impactam URLs públicas ou internas. As credenciais do usuário IAM são injetadas no job, o comando de emissão gera o certificado e, ao final, um artefato assinado é armazenado no repositório de segredos e distribuído para os ambientes via GitOps. Esse ciclo fechado garante rastreabilidade, auditoria e repetibilidade — pilares para qualquer serviço notarial em nuvem.

## Alternativas e ferramentas

- **lego**: durante um exercício de contingência, detectamos que a dependência no `certbot` era um ponto único de falha para times que usam ferramentas Go em pipelines serverless. Adotamos o cliente ACME em Go quando precisamos emitir certificados diretamente em scripts de infraestrutura como código. As variáveis de ambiente abaixo liberam o acesso do nosso usuário IAM e o comando `lego` emite o wildcard necessário para os testes do balcão digital:

  ```bash
  AWS_ACCESS_KEY_ID=SEU_ACCESS_KEY \
  AWS_SECRET_ACCESS_KEY=SEU_SECRET_KEY \
  lego --dns route53 -d cartorio.gov.br -d '*.balcao.cartorio.gov.br' \
    --email dpo@cartorio.gov.br run
  ```

- **Smallstep CLI**: o Step CA também suporta DNS-01 e pode integrar-se ao Route 53. É útil quando emitimos certificados internos para integrações com serventias parceiras. Consulte a documentação em https://smallstep.com/docs/step-ca/acme.

## Atividades

- Crie um usuário IAM restrito ao Route 53 das zonas do cartório digital e registre as credenciais no seu ambiente de desenvolvimento ou no cofre de segredos do pipeline;
- Instale o plugin `certbot-dns-route53` no ambiente de automação e emita um certificado wildcard de staging para um domínio de testes do cartório. Verifique se o arquivo `fullchain.pem` contém os domínios esperados com `openssl x509 -noout -text -in fullchain.pem`;
- Agende uma tarefa de renovação automática e um **hook** que recarregue o balanceador mTLS dos balcões digitais após a renovação.

Continuamos evoluindo o cartório digital com automações que inspiram confiança. A validação DNS-01 abriu caminho para que cada serventia e balcão eletrônico ofereça um atendimento seguro, reforçando a visão de um cartório moderno, disponível e comprometido com o cidadão.
