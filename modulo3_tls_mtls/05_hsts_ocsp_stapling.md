# HSTS, OCSP Stapling e Melhores Práticas

## Exemplo Inspirador

Após habilitar TLS e mTLS, o cartório recebeu um relatório externo apontando oportunidades de endurecimento. A equipe reuniu-se em um war room, aplicou HSTS, ativou OCSP stapling e ajustou as cifras. Quando repetiram o scanner de segurança e viram a nota máxima, todos comemoraram — a jornada de proteção havia atingido um novo patamar de excelência.

## Conceitos Fundamentais

- **HSTS:** força navegadores a usar HTTPS, prevenindo ataques de downgrade.
- **OCSP stapling:** servidor fornece status de revogação assinado, reduzindo latência e dependência de consultas externas.
- **Ciphers modernos e PFS:** mantêm sigilo mesmo diante de comprometimentos futuros.
- **Segurança operacional:** monitoramento constante e rotação de certificados completam o ciclo.

## Práticas Reais

1. **Habilite HSTS com subdomínios:**
   ```nginx
   add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
   ```
   Solicite inclusão na lista de pré-carregamento apenas após confirmar que todos os subdomínios suportam HTTPS.

2. **Ative OCSP stapling no Nginx:**
   ```nginx
   ssl_stapling on;
   ssl_stapling_verify on;
   resolver 1.1.1.1 8.8.8.8 valid=300s;
   resolver_timeout 5s;
   ```
   Garanta que o certificado contenha o campo AIA com a URL do OCSP responder.

3. **Restrinja cifras e curvas:**
   ```nginx
   ssl_ciphers TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256;
   ssl_ecdh_curve X25519:P-256;
   ```
   Remova suporte a TLS 1.2 somente após verificar compatibilidade com todos os clientes.

4. **Implemente monitoramento:** configure alertas para expiração de certificados, falhas no stapling e ausência de cabeçalhos críticos.

## Próximos passos

Com o ambiente web fortificado, avançaremos para automação da emissão de certificados. No próximo módulo mergulharemos em ACME e fluxos automatizados, começando por um exemplo inspirador de como liberar certificados sem perder controle.
