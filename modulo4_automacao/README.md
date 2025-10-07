# Módulo 4 – Automação do Ciclo de Vida de Certificados

Quando o cartório digital garante que cada ato eletrônico está protegido por um certificado válido, os resultados aparecem na forma de confiança pública, auditorias aprovadas e serviços contínuos. A automação via protocolo **ACME** é o motor invisível dessa conquista: ela elimina gargalos manuais e mantém os certificados sempre alinhados ao ritmo do negócio. Neste módulo você aprenderá a transformar essa automação em vantagem estratégica, combinando **Let’s Encrypt**, **Certbot** e **Smallstep** para que a experiência do cartório nunca pare.

Nos capítulos anteriores você estruturou a PKI e habilitou servidores TLS. Agora vamos fechar esse ciclo, garantindo que cada renovação aconteça sem fricção e com registros que comprovem a conformidade do serviço.

## Sumário dos capítulos

- [01_objetivo.md](01_objetivo.md) – Mostra como alinhar a automação de certificados à visão do cartório digital e definir metas mensuráveis.
- [02_protocolo_acme.md](02_protocolo_acme.md) – Desvenda o fluxo ACME e como cada desafio comprova a posse do domínio com segurança.
- [03_certbot_http_tls.md](03_certbot_http_tls.md) – Demonstra o uso do Certbot para cumprir os desafios HTTP-01 e TLS-ALPN-01 sem interromper os serviços.
- [04_dns_challenges_aws.md](04_dns_challenges_aws.md) – Ensina a orquestrar o desafio DNS-01 via Route 53 para cobrir domínios wildcard e infra em múltiplas regiões.
- [05_automacao_hooks.md](05_automacao_hooks.md) – Orienta a criar hooks e pipelines que instalam certificados renovados e documentam cada etapa para auditoria.

## Objetivos de aprendizagem

- Entender o protocolo **ACME (RFC 8555)** e seus desafios (HTTP‑01, DNS‑01, TLS‑ALPN‑01);
- Configurar um cliente ACME (Certbot ou `step-cli`) para emitir e renovar certificados;
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
- Blog AWS sobre uso da [ACM Private CA e certificados de uso restrito](https://aws.amazon.com/blogs/security/).
