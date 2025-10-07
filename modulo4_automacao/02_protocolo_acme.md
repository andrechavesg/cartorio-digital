# Protocolo ACME em Ação

## Exemplo Inspirador

Quando a equipe precisou renovar 20 certificados em um único fim de semana, perceberam que manualmente seria inviável. Eles configuraram um servidor ACME interno, registraram a conta do cartório e, em poucas horas, toda a frota estava renovada automaticamente. O sentimento foi de libertação: a infraestrutura passou a trabalhar em ritmo próprio, sem depender de plantões heroicos.

## Conceitos Fundamentais

- **Conta ACME:** registro com pares de chaves usados para assinar requisições.
- **Ordem:** pedido de certificado contendo identificadores (domínios) e desafios relacionados.
- **Desafios HTTP-01 e DNS-01:** formas de comprovar controle sobre o domínio.
- **Finalização:** envio do CSR e recebimento do certificado assinado.

## Práticas Reais

1. **Inicialize uma conta ACME (com Certbot ou step-cli):**
   ```bash
   step ca certificate --provisioner "acme" --acme --force
   ```
   Registre onde a chave da conta foi armazenada e quem tem permissão para usá-la.

2. **Simule uma ordem ACME:** acompanhe os estados `pending`, `ready` e `valid` usando ferramentas como `step ca` ou a API da autoridade escolhida.

3. **Estude os desafios disponíveis:** documente quando utilizar HTTP-01 (domínios públicos) e DNS-01 (wildcards, ambientes internos).

4. **Planeje políticas de segurança:** defina limites de taxa, monitoramento e rotação das chaves de conta.

## Próximos passos

Agora que entendemos o protocolo, vamos colocá-lo em prática com uma ferramenta popular. No próximo capítulo acompanharemos, com um exemplo inspirador, como o Certbot automatiza desafios HTTP e entrega certificados renovados sem drama.
