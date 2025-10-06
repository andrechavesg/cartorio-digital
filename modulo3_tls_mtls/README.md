# Módulo 3 – TLS 1.3 e mTLS

Ao final do módulo 2, você construiu sua própria Infraestrutura de Chaves Públicas (PKI) e emitiu certificados. Neste módulo, você irá utilizá‑los para configurar servidores com TLS 1.3, implementar autenticação mútua (mTLS) entre serviços e aprofundar temas como OCSP stapling, HSTS e Perfect Forward Secrecy. Essas práticas garantem que as conexões entre clientes e servidores tenham confidencialidade e autenticidade.

## Sumário dos capítulos

1. [Objetivo do módulo](01_objetivo.md) – panorama e motivação do uso de TLS e mTLS no cartório digital.
2. [TLS 1.3: Conceitos e Handshake](02_tls_1_3_conceitos.md) – entender handshake, suites modernas e exercícios com OpenSSL.
3. [Configurando servidor TLS 1.3](03_configurando_tls_servidor.md) – configurar Nginx (ou ALB) com certificados da CA do módulo 2.
4. [Configurando mTLS](04_configurando_mtls.md) – emitir certificados de cliente e habilitar autenticação mútua.
5. [HSTS, OCSP stapling e PFS](05_hsts_ocsp_stapling.md) – boas práticas de segurança para reforçar a proteção e detectar revogação.

## Objetivos de aprendizagem

- Revisar o handshake e os algoritmos de criptografia suportados pelo TLS 1.3;
- Configurar servidores web (por exemplo, Nginx, Apache ou ALB da AWS) para usar TLS 1.3 e **OCSP stapling**;
- Implementar **mTLS** para autenticar mutuamente clientes e serviços internos;
- Compreender boas práticas como HSTS e Perfect Forward Secrecy.

## Entrega prática

Utilize os certificados emitidos no módulo 2 para:

1. Configurar um servidor web com TLS 1.3 ativo;
2. Ativar OCSP stapling e HSTS nos headers de resposta;
3. Configurar autenticação múlta em um serviço interno (por exemplo, entre um backend e um microserviço);
4. Testar as conexões usando ferramentas como `curl`, `openssl s_client` e navegadores.

## Referências recomendadas

- **RFC 8446** – Especificação do TLS 1.3;
- Documentação do Nginx sobre [TLS 1.3 e ocsp stapling](https://nginx.org/en/docs/http/ngx_http_ssl_module.html#ocsp_stapling);
- Artigo *Understanding mTLS* da Cloudflare (conceitos de mTLS).
