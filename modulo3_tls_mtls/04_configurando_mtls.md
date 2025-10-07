# Configurando mTLS (Autenticação Mútua)

TLS assegura a identidade do servidor; mTLS estende isso exigindo que o cliente também se autentique com um certificado. Isso é útil em APIs internas, comunicação entre microserviços ou para acesso restrito a funcionários.

## Emitindo certificados de cliente

Considere o cenário do cartório digital em que APIs internas sensíveis só podem ser invocadas por escreventes autenticados. Sem controles fortes, um atacante poderia obter credenciais simples (usuário/senha) e automatizar fraudes em registros eletrônicos. Para impedir esse abuso, o setor de TI exige que cada dispositivo apresente um certificado individual, e os comandos abaixo criam o material criptográfico que permite bloquear qualquer requisição sem o selo oficial do cartório:

```bash
# Gerar chave privada do cliente (garante que apenas o escrevente possua o segredo de autenticação)
openssl genpkey -algorithm RSA -out client.key -pkeyopt rsa_keygen_bits:2048

# Criar CSR com a identidade do escrevente (solicita formalmente um certificado contendo o DN aprovado pelo cartório)
openssl req -new -key client.key -out client.csr -subj "/CN=usuario1/O=Cartorio Digital"

# Assinar o CSR com a CA intermediária (a resposta oficial do cartório, validando a identidade e habilitando o acesso às APIs)
openssl ca -config openssl_intermediate.cnf -extensions usr_cert \
    -days 365 -in client.csr -out client.crt -batch
```

Como parte do plano de continuidade, vários escreventes utilizam navegadores ou estações gerenciadas que só aceitam pacotes PKCS#12. Sem essa conversão, surgem chamados de indisponibilidade que atrasam o atendimento e empurram o time para liberar acessos sem verificação. O comando seguinte empacota certificado e chave de forma segura, eliminando a brecha operacional que poderia forçar a desativação temporária do mTLS:

```bash
openssl pkcs12 -export -out client.p12 -inkey client.key -in client.crt -certfile intermediate_ca.crt
```

## Configurando o Nginx para mTLS

A ameaça que queremos mitigar é a fraude: clientes não autorizados tentando registrar escrituras ou consultar bases internas sem qualquer vínculo com o cartório. Um endpoint sem verificação derrubaria a confiança do sistema. Para reduzir esse risco, o Nginx passa a validar os certificados emitidos pela nossa CA, usando as diretivas abaixo no bloco `server` configurado no capítulo anterior e derrubando automaticamente conexões suspeitas:

```nginx
server {
    # ... configuração TLS já existente ...

    # Caminho para o certificado da CA que emitiu os certificados de cliente
    ssl_client_certificate /etc/nginx/certs/intermediate_ca.crt;
    # Exigir e verificar certificado do cliente (on|off|optional)
    ssl_verify_client on;

    # Opcional: Controle de acesso baseado em DN do cliente
    # if ($ssl_client_s_dn !~* \"O=Cartorio Digital\") {
    #     return 403;
    # }

    # ...
}
```

Para evitar uma degradação silenciosa, valide a configuração antes de recarregar: um erro de sintaxe poderia derrubar o serviço e forçar a equipe a desabilitar mTLS às pressas. Os comandos seguintes testam e aplicam a mudança com segurança:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## Testando com o curl e o openssl

Falhas de configuração podem degradar o serviço, recusando clientes legítimos ou, pior, aceitando acessos não certificados. Os testes a seguir fornecem evidências operacionais de que o controle de acesso está funcionando: a chamada `curl` comprova que uma aplicação do cartório consegue consumir a API apenas quando apresenta o certificado aprovado; sem ele, a negociação falha e impede o abuso automatizado.

Para acessar o servidor que exige mTLS, o cliente deve apresentar seu certificado:

```bash
# Usar o PKCS#12 com o curl (digite a senha de exportação se houver)
curl -v https://cartorio.local --cert client.p12 --cert-type P12 --key client.key

# Ou com o par PEM (certificado + chave)
curl -v https://cartorio.local --cert client.crt --key client.key
```

Uma auditoria externa pode questionar se o servidor está, de fato, exigindo certificados antes de liberar operações críticas. Para evitar essa dúvida e reagir rapidamente a qualquer degradação, use `openssl s_client` e capture a prova criptográfica do handshake mTLS (cadeia de certificados, verificação de cliente e sessão estabelecida):

```bash
openssl s_client -connect cartorio.local:443 -tls1_3 \
    -cert client.crt -key client.key -servername cartorio.local
```

Se a verificação falhar, o servidor encerrará a conexão com um erro TLS de handshake. Você pode acompanhar os logs do Nginx para depurar (`error_log`).

### Uso em microserviços e APIs

Em ambientes de microserviços (Kubernetes, service meshes), mTLS é frequentemente ativado por padrão para assegurar que apenas serviços legítimos se comuniquem. Plataformas como Istio e Linkerd automatizam a geração e rotação de certificados de cliente. Mesmo assim, o conceito é o mesmo: cada parte autentica a outra com certificados emitidos por uma autoridade confiável.

No próximo capítulo veremos como reforçar a segurança habilitando **HSTS**, **OCSP stapling** e outros detalhes importantes.
