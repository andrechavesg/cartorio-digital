# Infraestrutura como Código

## Exemplo Inspirador

O cartório provisionou um ambiente completo em minutos usando Terraform: redes, clusters Kubernetes, HSM gerenciado e integrações com KMS. A automação eliminou erros manuais e permitiu reproduzir ambientes com exatidão.

## Conceitos Fundamentais

- **Terraform/CloudFormation:** definem recursos em arquivos versionados.
- **Módulos reutilizáveis:** padronizam ambientes e reduzem duplicidade.
- **Controle de estado:** garante rastreabilidade do que foi criado ou alterado.
- **Políticas de segurança:** integrações com IAM e redes privadas.

## Práticas Reais

1. Versione a infraestrutura em repositório Git com revisões por pares.
2. Use módulos para redes, clusters e serviços compartilhados.
3. Integre verificações de segurança (tfsec, cfn-nag) no pipeline.
4. Documente procedimentos de destruição e recuperação de ambientes.

## Gancho para o Próximo Capítulo

Após provisionar infraestrutura, precisamos mantê-la observável e monitorada. No próximo capítulo veremos, inspirados por um incidente real, como alertas e dashboards mantêm o cartório seguro.
