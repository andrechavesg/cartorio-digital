# Objetivo do Módulo – TLS 1.3 e mTLS

## Introdução

Na inauguração do balcão digital, uma cidadã acessou o portal do cartório e, ao perceber o cadeado no navegador, comentou que finalmente confiava em enviar sua escritura pela internet. A equipe técnica sorriu: o certificado emitido no módulo anterior estava agora protegendo uma conexão TLS 1.3 impecável, com autenticação mútua para as APIs internas. Esse momento confirmou que segurança visível gera credibilidade imediata.

## Conceitos Fundamentais

- **TLS 1.3:** protocolo que cifra o tráfego entre cliente e servidor, reduzindo latência e removendo algoritmos inseguros.
- **mTLS (Mutual TLS):** extensão do TLS em que clientes também apresentam certificados, habilitando autenticação bilateral forte.
- **Certificados emitidos internamente:** fruto da PKI construída no módulo 2, essenciais para estabelecer confiança.
- **Melhores práticas complementares:** HSTS, OCSP stapling, curvas modernas e ajuste de ciphers garantem resiliência.

## Práticas Reais

1. Revise os certificados emitidos no módulo anterior e organize-os para uso em servidores e clientes.
2. Desenhe o mapa das conexões do cartório (portal público, APIs internas, integrações externas) e identifique onde TLS e mTLS serão aplicados.
3. Defina políticas de renovação e distribuição de certificados para garantir continuidade do serviço.
4. Prepare ambientes de teste que simulem ataques de downgrade ou acesso não autorizado para validar suas configurações.

## Gancho para o Próximo Capítulo

Com o objetivo claro, é hora de mergulhar nos detalhes do handshake. No próximo capítulo veremos, por meio de um exemplo real do cartório, como o TLS 1.3 estabelece sessões seguras e porque suas melhorias tornam a experiência dos cidadãos mais ágil e inspiradora.
