# Auditoria e Rotação de Chaves

## Introdução

Em um exercício de crise, o cartório simulou a revogação de uma chave comprometida. Graças aos relatórios de auditoria e ao plano de rotação automática, o incidente foi solucionado em minutos. A direção destacou o episódio como prova de que processos bem desenhados evitam danos reais.

## Conceitos Fundamentais

- **Rotação periódica:** reduz a janela de exposição em caso de vazamento.
- **Logs imutáveis:** garantem rastreabilidade e suporte a investigações.
- **Segregação de funções:** separa quem administra, quem opera e quem audita.
- **Planos de contingência:** definem passos claros diante de comprometimentos.

## Práticas Reais

1. Configure políticas de rotação automática em seu KMS e documente os intervalos definidos.
2. Armazene logs em repositórios imutáveis (WORM, SIEM) e crie alertas para eventos suspeitos.
3. Realize exercícios trimestrais de revogação e rotação, registrando lições aprendidas.
4. Implemente dashboards que mostrem chaves por idade, uso e responsáveis.

## Gancho para o Próximo Capítulo

Após dominar auditoria e rotação, é hora de aplicar todos os aprendizados em um projeto integrado. No próximo capítulo começaremos o desafio final deste módulo, conectando KMS, HSM e automações para proteger o cartório de ponta a ponta.
