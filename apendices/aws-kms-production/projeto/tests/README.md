# Estratégia de Testes

Diretório para testes automatizados que suportam certificação e requisitos contratuais.

- integration – Testes end-to-end dos fluxos da AC, utilizando ambientes provisionados via docker-compose ou clusters efêmeros.
- performance – Ensaios de carga, estresse e caos, atendendo aos SLAs definidos em PLAN.md.
- security – Testes de segurança automatizados (DAST, fuzzing, SAST complementares).

Resultados e evidências devem ser arquivados em evidences/ com referência no IMPLEMENTATION_PLAN.md.
