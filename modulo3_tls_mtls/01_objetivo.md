# Objetivo do Módulo – TLS 1.3 e mTLS

No módulo 2 construímos uma Autoridade Certificadora (CA) e emitimos certificados X.509 para servidores e clientes. Chegou a hora de *usar* esses certificados para proteger a comunicação. Este módulo foca no protocolo TLS 1.3 e na autenticação mútua (mTLS) — componentes fundamentais para transportar dados sensíveis do cartório digital com segurança.

Ao final deste capítulo, você será capaz de:

- Entender a evolução do TLS e as mudanças no handshake da versão 1.3.
- Configurar um servidor web (como Nginx) com suporte a TLS 1.3 usando seu certificado da CA.
- Habilitar mTLS para autenticar não só o servidor, mas também o cliente — garantindo que apenas usuários autorizados se conectem.
- Aplicar políticas como HSTS e stapling OCSP para reforçar a segurança e privacidade.
- Testar conexões TLS com ferramentas como `openssl s_client` e `curl` para inspecionar o handshake.

Este aprendizado prepara o caminho para o módulo 4, onde automatizaremos a emissão e renovação de certificados. O projeto do cartório digital agora terá uma base segura para que todas as requisições — seja de emissão de certidões, assinatura de documentos ou consulta a registros — trafeguem de forma confidencial e autêntica pela internet.
