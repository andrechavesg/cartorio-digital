# Infraestrutura — Módulo 1 (Fundamentos)

Scripts de provisionamento utilizando AWS CLI para construir a infraestrutura
mínima do módulo.

## Pré-requisitos
- AWS CLI configurado com credenciais válidas
- Imagem do backend publicada em um repositório ECR acessível
- Build do frontend disponível em `../frontend/dist`

## Uso
1. Execute `./01-deploy-core.sh` para criar/atualizar a stack principal.
2. Execute `./02-sync-frontend.sh` para publicar os artefatos estáticos.
