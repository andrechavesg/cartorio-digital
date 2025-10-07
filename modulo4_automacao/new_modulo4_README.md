# Módulo 4 – Automação do Ciclo de Vida de Certificados

Automatizar a emissão e renovação de certificados reduz falhas humanas e mantém os serviços do cartório digital sempre protegidos. Neste módulo você irá trabalhar com o protocolo **ACME** e ferramentas como **Let’s Encrypt**, **Certbot** e **Smallstep** para automatizar esse processo.

Nos capítulos anteriores você aprendeu a estruturar uma PKI, emitir certificados e configurá-los em servidores TLS com mTLS. Este capítulo é a continuidade natural: agora vamos automatizar a emissão e renovação desses certificados para que seu cartório digital funcione sem interrupções.

## Objetivos de aprendizagem

- Entender o protocolo **ACME (RFC 8555)** e seus desafios (HTTP‑01, DNS‑01, TLS‑ALPN‑01);
- Configurar um cliente ACME (Certbot ou `step-cli`) para emitir e renovar certificados;
- Automatizar a renovação via DNS automatizado (Route 53) ou APIs de cloud;
- Incorporar hooks de deploy para atualizar serviços sem downtime.

## Entrega prática

No contexto do cartório digital, vamos implementar:

1. Emissão de certificados de servidor usando Let’s Encrypt em ambiente de staging;
2. Configuração de desafio DNS via Route 53 (AWS) ou via `dnsimple` (se preferir);
3. Scripts de renovação automatizada que reiniciam o serviço web ao instalar o novo certificado;
4. Registro das atividades de renovação para auditoria.

## Sumário dos capítulos

- [Objetivo do módulo](01_objetivo.md) – Introdução sobre por que automação é essencial, retomando o que foi visto nos módulos anteriores.
- [Protocolo ACME](02_protocolo_acme.md) – Conceitos, fluxo e desafios do protocolo que permite emitir certificados automaticamente.
- [Certbot e desafios HTTP/TLS](03_certbot_http_tls.md) – Como usar Certbot para os desafios HTTP‑01 e TLS‑ALPN‑01 na emissão de certificados.
- [Desafio DNS e AWS Route 53](04_dns_challenges_aws.md) – Configuração do desafio DNS‑01 usando Route 53, incluindo wildcard e credenciais.
- [Automação e hooks de deploy](05_automacao_hooks.md) – Criar scripts e hooks para renovação automática e instalação de certificados em servidores e AWS.

## Referências recomendadas

- **RFC 8555** – Protocolo ACME;
- Documentação oficial do [Let’s Encrypt](https://letsencrypt.org/docs/);
- Blog AWS sobre uso da [ACM Private CA e certificados de uso restrito](https://aws.amazon.com/blogs/security/).
