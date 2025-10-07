# Objetivo do Módulo 4 – Automção com ACME e Let's Encrypt

No módulo anterior você configurou TLS 1.3 e mTLS usando os certificados da sua própria CA. Para um cartório digital em produção, porém, a emissão e renovação manual não é prática: certificados têm prazos curtos e esquecer de renová‑los derruba serviços.

Neste módulo você vai aprender a **automatizar o ciclo de vida** dos certificados públicos usando o protocolo ACME (RFC 8555) e serviços como o **Let's Encrypt**, mantendo sua aplicação sempre protegida sem intervenção humana. Ao final, você será capaz de emitir, instalar e renovar certificados de forma automática, tanto via desafios HTTP‑​01 quanto DNS‑​01 em provedores como Route 53.

**O que você vai aprender?**

- Por que automatizar a emissão e renovação de certificados é crítico para sistemas online;
- Como funciona o protocolo ACME e seus atores (Cliente, Servidor ACME, Autorizações e Desafios);
- Quando usar desafios HTTP‑ 01, DNS‑ 01 e TLS‑ALPN‑ 01 e quais os prós e contras de cada um;
- Como instalar e usar clientes ACME como `certbot` e `step-cli`;
- Boas práticas para integrar a renovação automática no Nginx/ALB e notificar serviços.

**Conectando com o projeto do cartório digital**

Os certificados emitidos por CA pública como o Let's Encrypt são usados para expor seu serviço ao público (por exemplo, o site do cartório). Já os certificados emitidos pela sua CA interna continuam sendo usados para comunicação interna (mTLS). Aqui você vai aprender a automatizar a parte externa, mas pode adaptar os scripts para renovar certificados da CA privada usando `step-ca`.

**Tarefa inicial**

1. Garanta que você tem um domínio público ou subdomínio apontando para sua máquina de testes (pode usar um DNS dinâmico gratuito).
2. Instale `certbot` ou `step-cli` no servidor e verifique a versão instalada (`certbot --version`).
3. Leia o arquivo `README.md` deste módulo para entender os objetivos práticos que serão realizados.

Nos próximos capítulos você irá explorar o protocolo ACME em detalhes e colocar em prática a emissão automática de certificados.
