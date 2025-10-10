# Evidência – Modelagem de Domínio dos Serviços Backend

## Resumo
- Modelagem inicial dos agregados principais para os serviços `identity`, `enrollment`, `issuance`, `revocation`, `validation`, `audit` e `publisher` conforme capítulo 3 do plano (`backend/services/*/src/main/java/br/com/cartoriodigital/**/domain`).
- Criação de testes unitários focados em regras de negócio críticas (dual control, transições de estado, contadores monotônicos) em `backend/services/*/src/test/java`.

## Execução de Testes
- Comando: `mvn -f backend/pom.xml test`
- Resultado: **falha** – ambiente não possui `mvn` instalado (`bash: mvn: command not found`). Não há wrapper `mvnw` no repositório. Necessário disponibilizar Maven para validar a suíte.

## Observações
- Nenhuma alteração aplicada fora dos diretórios de domínio/testes dos serviços backend.
- É recomendada a configuração do Maven Wrapper (`mvn -N io.takari:maven:wrapper`) em execução futura para garantir reprodutibilidade.
