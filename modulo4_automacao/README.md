# Módulo 4 – Automação do Ciclo de Vida de Certificados

Automatizar a emissão e renovação de certificados reduz falhas humanas e mantém os serviços do cartório digital sempre protegidos.  Neste módulo você irá trabalhar com o protocolo **ACME** e ferramentas como **Let’s Encrypt**, **Certbot** e **Smallstep** para automatizar esse processo.

## Objetivos de aprendizagem

- Entender o protocolo **ACME (RFC 8555)** e seus desafios (HTTP‑01, DNS‑01, TLS‑ALPN‑01);
- Configurar um cliente ACME (Certbot ou `step‑cli`) para emitir e renovar certificados;
- Automatizar a renovação usando DNS automatizado (Route 53) ou APIs de cloud;
- Incorporar hooks de deploy para atualizar serviços sem downtime.

## Entrega prática

No contexto do cartório digital, implemente:

1. Emissão de certificados de servidor usando Let’s Encrypt em ambiente de staging;
2. Configuração de desafio DNS‑01 via Route 53 (AWS) ou via `dnsimple` (se preferir);
3. Scripts de renovação automática que reiniciam o serviço web ao instalar o novo certificado;
4. Registro das atividades de renovação para auditoria.

## Referências recomendadas

- **RFC 8555** – Protocolo ACME;
- Documentação oficial do [Let’s Encrypt](https://letsencrypt.org/docs/);
- Blog AWS sobre uso da [ACM Private CA e certificados de uso restrito](https://aws.amazon.com/blogs/security/).
