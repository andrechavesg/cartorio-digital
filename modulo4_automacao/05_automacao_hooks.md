# Hooks, Pipelines e Governança

## Introdução

Após uma renovação automática, uma aplicação deixou de reiniciar e continuou usando o certificado antigo. O time respondeu criando hooks que reiniciavam os serviços e notificavam as áreas envolvidas. Na renovação seguinte, tudo ocorreu sem intervenção manual, e o registro detalhado ficou disponível para auditoria. A automação passou a ser sinônimo de governança.

## Conceitos Fundamentais

- **Hooks:** scripts executados antes ou depois da emissão/renovação.
- **Integração contínua:** pipelines que propagam certificados para múltiplos ambientes.
- **Observabilidade:** logs e dashboards acompanham sucesso ou falhas das renovações.
- **Governança:** documentação e aprovações garantem conformidade com requisitos legais.

## Práticas Reais

1. **Implemente hooks do Certbot:**
   ```bash
   sudo certbot renew --deploy-hook "/usr/local/bin/reinicia_cartorio.sh"
   ```
   Crie o script para reiniciar serviços com segurança e registre auditorias.

2. **Integre com pipelines CI/CD:** use ferramentas como GitLab CI ou GitHub Actions para distribuir certificados para contêineres, balanceadores e appliances.

3. **Monitore resultados:** envie logs para o Stackdriver, CloudWatch ou Prometheus e configure alertas para falhas repetidas.

4. **Documente o fluxo:** mantenha um runbook descrevendo ações de emergência caso a automação falhe.

## Próximos passos

Com as renovações sob controle, estamos prontos para navegar pelas exigências regulatórias que sustentam a operação. Na Introdução do módulo seguinte analisaremos normas da ICP-Brasil e padrões internacionais a partir de um caso de conformidade que abriu portas para o cartório digital.
