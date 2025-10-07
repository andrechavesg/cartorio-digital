# Conceitos de KMS e HSM

## Exemplo Inspirador

Na migração para o HSM, a equipe realizou uma sessão aberta para todo o cartório. Demonstraram como uma chave criada dentro do módulo jamais sai em formato texto, apenas é utilizada via operações assinadas. Esse espetáculo tecnológico encantou a todos e reforçou a importância de investir em hardware dedicado.

## Conceitos Fundamentais

- **Chaves mestras e chaves derivadas:** KMS usa chaves raiz para proteger chaves de dados.
- **Operações dentro do HSM:** geração, assinatura e decriptação ocorrem dentro do hardware.
- **Políticas de acesso:** definem quem pode gerar, usar ou girar chaves.
- **Logs imutáveis:** cada operação fica registrada para auditoria.

## Práticas Reais

1. Experimente criar uma chave simétrica em um KMS gerenciado (AWS KMS, GCP KMS, Azure Key Vault) e use-a para cifrar dados.
2. Explore operações assimétricas assinando um hash via HSM e verificando o resultado externamente.
3. Configure políticas que restringem o uso da chave a determinados serviços ou funções.
4. Ative logs de auditoria e configure alertas para uso fora do horário esperado ou por identidades não autorizadas.

## Gancho para o Próximo Capítulo

Com a teoria clara, é hora de construir a implementação específica do cartório. No próximo capítulo veremos, inspirado por um projeto real, como desenhar uma arquitetura de KMS que suporta todos os fluxos críticos da instituição.
