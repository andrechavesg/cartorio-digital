# Módulo 10 &mdash; Projeto Final Integrado

O projeto final consolida todos os módulos anteriores em uma única aplicação full-stack, permitindo acompanhar o ciclo de vida completo de certificação digital: cadastro, emissão, automação ACME, integração com KMS/HSM, assinatura de artefatos, pipelines e observabilidade.

## Funcionalidades

- **Cadastro e Compliance:** validação de documentos e auditoria das emissões.
- **PKI completa:** certificados de cliente, servidor, revogação e simulação mTLS.
- **Automação ACME:** ordens e desafios automáticos para emissões TLS.
- **Integração KMS/HSM:** emissão de credenciais com chaves protegidas e assinatura via alias.
- **Assinatura de artefatos:** certificados de code signing e registros de assinaturas.
- **Pipeline CI/CD:** execução de builds, assinatura e deploy simulados com governança.
- **Observabilidade:** métricas, traces e logs agregados em uma API única.

## Execução

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload
```

Sirva o frontend integrado:

```bash
cd ../frontend
python -m http.server 3000
```

Acesse `http://localhost:3000` para interagir com todos os fluxos.

### Execução com Docker

```bash
# Backend
docker build -t cartorio-backend:latest backend/
docker run --rm -p 8000:8000 cartorio-backend:latest

# (opcional) Frontend estático
docker build -t cartorio-frontend:latest frontend/
docker run --rm -p 3000:80 cartorio-frontend:latest
```

> O frontend consome a API em `http://localhost:8000`, portanto mantenha o contêiner do backend ativo antes de iniciar o frontend.

### Execução com Docker Compose

```bash
cd modulo10_projeto_final/projeto
docker compose up --build
```

- A API ficará disponível em `http://localhost:8000`.
- O frontend estará em `http://localhost:3000`.

Para encerrar e remover os contêineres:

```bash
docker compose down
```

### Execução em Minikube

```bash
minikube start

# Construa as imagens diretamente no cluster
minikube image build -t cartorio-backend:latest backend/
minikube image build -t cartorio-frontend:latest frontend/

# Aplique os manifests com Kustomize
kubectl apply -k infra/k8s
```

Para expor os serviços localmente:

```bash
# Frontend
minikube service cartorio-frontend -n cartorio-mod10 --url

# API (alternativa com port-forward)
kubectl port-forward -n cartorio-mod10 svc/cartorio-backend 8000:8000
```

Finalize os recursos:

```bash
kubectl delete -k infra/k8s
minikube stop
```

## Validação sugerida

1. Cadastre um cidadão, emita certificado de cliente e valide o compliance.
2. Gere um certificado de servidor e realize um handshake mTLS usando os números de série retornados.
3. Crie uma ordem ACME, valide o token e inspecione as métricas geradas.
4. Emita credenciais protegidas pelo KMS, assine um payload e consulte as chaves disponíveis.
5. Execute um pipeline CI/CD e observe a criação automática de credenciais e artefatos assinados.
6. Consulte `/observability/snapshot` e `/observability/metrics/prometheus` para monitorar o sistema.

Cada etapa registra métricas e logs na camada de observabilidade, fornecendo rastreabilidade ponta a ponta.
