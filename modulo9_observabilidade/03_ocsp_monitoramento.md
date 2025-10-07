# Monitorando OCSP e Status de Certificados

## Introdução

Durante uma integração com um órgão parceiro, o responder OCSP do cartório ficou indisponível por alguns minutos. Graças ao monitoramento ativo, a equipe recebeu alertas e restabeleceu o serviço rapidamente, evitando impacto nos cidadãos.

## Conceitos Fundamentais

- **OCSP:** verifica se um certificado está válido, revogado ou desconhecido.
- **Monitoramento de disponibilidade:** garante que o responder esteja acessível.
- **Alertas de latência:** indicam problemas de desempenho.
- **Testes de ponta a ponta:** simulam clientes consultando o status.

## Práticas Reais

1. Configure sondas para consultar o responder OCSP periodicamente e registrar tempos de resposta.
2. Monitore certificados que dependem do OCSP e crie alertas para falhas prolongadas.
3. Documente procedimentos de fallback (CRL) em caso de indisponibilidade.
4. Integre logs de OCSP ao SIEM para detectar padrões anômalos.

## Próximos passos

Com o status em dia, vamos transformar dados em insights. No próximo capítulo veremos como métricas e dashboards em Grafana inspiram decisões rápidas e precisas.
