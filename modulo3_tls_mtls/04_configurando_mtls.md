# Configurando mTLS (Autenticação Mútua)

## Exemplo Inspirador

Durante uma auditoria de integração, a secretaria estadual exigiu prova de que apenas sistemas autorizados acessavam a API de protestos. Em uma reunião ao vivo, a equipe do cartório habilitou mTLS, distribuiu certificados de cliente e demonstrou que requisições sem credenciais eram imediatamente bloqueadas. O auditor aplaudiu: a confiança ganhou uma camada extra de proteção tangível.

## Conceitos Fundamentais

- **mTLS:** requer certificados de cliente confiados pela CA intermediária.
- **Políticas de distribuição:** chaves privadas devem ser entregues com segurança e protegidas por senha ou hardware.
- **Configuração do servidor:** valida certificados apresentados, verificando cadeia, revogação e propósito (EKU clientAuth).
- **Experiência do usuário:** mensagens claras ajudam clientes a instalar certificados corretamente.

## Práticas Reais

1. **Emita certificados de cliente (revisite o módulo 2, capítulo 4):** garanta que o EKU contenha `clientAuth`.

2. **Configure o Nginx para exigir certificados:**
   ```nginx
   server {
       listen 8443 ssl http2;
       server_name api.cartorio.local;

       ssl_certificate     /etc/nginx/certs/cartorio.local.cert.pem;
       ssl_certificate_key /etc/nginx/certs/server.key.pem;
       ssl_trusted_certificate /etc/nginx/certs/ca-chain.cert.pem;

       ssl_protocols TLSv1.3;
       ssl_verify_client on;
       ssl_client_certificate /etc/nginx/certs/ca-chain.cert.pem;
       ssl_verify_depth 2;

       location / {
           proxy_pass http://localhost:8080;
       }
   }
   ```
   Defina políticas de rotação de certificados de cliente e registre os responsáveis pela distribuição.

3. **Teste o acesso autenticado:**
   ```bash
   curl https://api.cartorio.local:8443/saude \
       --cert usuario.exemplo.cert.pem \
       --key  usuario.exemplo.key.pem \
       --cacert ca-chain.cert.pem
   ```
   Documente o comportamento ao remover o certificado de cliente para evidenciar o bloqueio.

4. **Automatize verificação de revogação:** configure `ssl_crl` ou OCSP stapling para rejeitar certificados revogados.

## Próximos passos

Com TLS e mTLS operando, podemos elevar o nível adicionando cabeçalhos e mecanismos que reforçam a segurança contínua. No próximo capítulo integraremos HSTS, OCSP stapling e outras práticas inspiradoras que completam o arsenal do cartório digital.
