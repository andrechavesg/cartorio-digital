# Desafios DNS-01 na AWS

## Introdução

Para emitir certificados wildcard para o domínio corporativo, o cartório precisou provar controle via DNS. Durante uma noite de integração com a AWS, o time automatizou a criação de registros TXT temporários e viu o certificado ser liberado minutos depois. O processo parecia mágico: infraestrutura como código cuidando da segurança automaticamente.

## Conceitos Fundamentais

- **DNS-01:** desafio ACME que valida propriedade do domínio por meio de registros TXT.
- **Integração com provedores:** bibliotecas e scripts atualizam DNS via APIs (Route 53, Cloudflare, etc.).
- **Tempo de propagação:** deve ser considerado para evitar falhas.
- **Limpeza automática:** remover registros após validação reduz riscos e mantém a zona organizada.

## Práticas Reais

1. **Configure credenciais com menor privilégio:** crie um usuário IAM com permissão apenas para modificar registros TXT na zona desejada.

2. **Use o plugin DNS do Certbot ou scripts com AWS CLI:**
   ```bash
   sudo apt install python3-certbot-dns-route53
   sudo certbot -a dns-route53 -d "*.cartorio.local" -d cartorio.local --agree-tos --register-unsafely-without-email
   ```
   Documente a política IAM utilizada e armazene as chaves com segurança.

3. **Automatize via pipelines:** integre Terraform, AWS CLI ou Step Functions para gerar certificados em ambientes dinâmicos.

4. **Planeje monitoramento:** verifique logs de alterações DNS e alerte quando desafios demorarem a propagar.

## Gancho para o Próximo Capítulo

Depois de automatizar as renovações, precisamos acionar tarefas complementares em cada emissão. Na Introdução do próximo capítulo veremos como hooks e pipelines garantem deploys seguros e coerentes em todo o cartório digital.
