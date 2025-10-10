# Evidência – Dockerização dos Serviços Identity/Enrollment

## Artefatos
- Dockerfiles específicos para cada microserviço Quarkus (`backend/services/identity/Dockerfile`, `backend/services/enrollment/Dockerfile`).
- Ajuste do `docker-compose.yml` para usar a raiz `./backend` como contexto e habilitar perfis opcionais para os demais serviços.

## Execução Tentada
- Comando: `docker compose up --build identity-service enrollment-service`
- Resultado: **falha** – ambiente sem permissão para acessar o daemon Docker (`permission denied while trying to connect to the Docker daemon socket`).

## Próximos Passos
- Executar o comando em ambiente com acesso ao daemon Docker para gerar imagens e iniciar os serviços.
- Alternativamente, solicitar habilitação de Docker no ambiente atual.
