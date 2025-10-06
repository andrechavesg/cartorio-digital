# Módulo 3 – TLS 1.3 e mTLS

Este módulo trata da proteção do transporte de dados do cartório digital.  Você irá configurar serviços usando TLS 1.3 e implementar autenticação mùtua (mTLS) entre clientes e servidores, garantindo confidencialidade e autenticidade nas conexões.

## Objetivos de aprendizagem

- Revisar o handshake e os algoritmos de criptografia suportados pelo TLS 1.3;
- Configurar servidores web (por exemplo, Nginx, Apache ou ALB da AWS) para usar TLS 1.3 e **OCSP stapling**;
- Implementar **mTLS** para autenticar mutuamente clientes e serviços internos;
- Compreender boas práticas como HSTS e Perfect Forward Secrecy.

## Entrega prática

Utilize os certificados emitidos no módulo 2 para:

1. Configurar um servidor web com TLS 1.3 ativo;
2. Ativar OCSP stapling e HSTS nos headers de resposta;
3. Configurar autenticação mùtua em um serviço interno (por exemplo, entre um backend e um microserviço);
4. Testar as conexões usando ferramentas como `curl`, `openssl s_client` e navegadores.

## Referências recomendadas

- **RFC 8446** – Especificação do TLS 1.3;
- Documentação do Nginx sobre [TLS 1.3 e OCSP stapling](https://docs.nginx.com/nginx/admin-guide/security-controls/terminating-ssl-http/);
- Artigo *Understanding mTLS* da Cloudflare (conceitos e casos de uso).
