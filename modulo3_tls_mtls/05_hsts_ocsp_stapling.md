# HSTS, OCSP Stapling e Melhores Práticas

Além do uso de TLS 1.3 e mTLS, existem cabeçalhos e mecanismos adicionais que fortalecem a segurança e a privacidade das conexões.

## HSTS – HTTP Strict Transport Security

Durante os testes de acesso às certidões digitais, identificamos uma tentativa de downgrade forçado para HTTP simples, explorando uma configuração temporariamente exposta e permitindo a captura das requisições. Como contramedida imediata, o time respondeu com o cabeçalho HSTS para assegurar que os navegadores voltem a consumir o portal exclusivamente por TLS.

HSTS obriga o navegador a acessar um domínio *apenas* via HTTPS, prevenindo ataques de downgrade e de ‘stripping’ de TLS. Ao configurar o cabeçalho imediatamente após detectar a tentativa de abuso, garantimos que nenhum navegador aceite voltar a HTTP durante a janela de mitigação.

Adicione o cabeçalho no Nginx (após o bloco `ssl_...`) para selar o comportamento seguro:

```nginx
server {
    # ... configurações TLS ...

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;
}
```

- `max-age`: tempo (em segundos) que o navegador lembrará de usar apenas HTTPS. Um ano (31536000) é recomendado.
- `includeSubDomains`: aplica a política a todos os subdomínios.
- `preload`: indica que você deseja que seu domínio seja incluído na lista de pré-carregamento mantida pelos navegadores. Só adicione esta flag após garantir que HTTPS está configurado corretamente em todos os subdomínios.

Para submeter seu domínio ao preload, consulte <https://hstspreload.org/>.

## OCSP Stapling

OCSP (Online Certificate Status Protocol) é um mecanismo para verificar se um certificado foi revogado. Com o stapling, o servidor obtém e “grampeia” (staple) uma resposta OCSP válida ao handshake TLS, evitando que cada cliente precise consultar a CA. Isso responde à reclamação recorrente dos usuários, que percebiam lentidão na revogação de certificados quando dependíamos da consulta direta à autoridade certificadora.

Se o servidor deixar para cada cliente consultar o status de revogação, qualquer instabilidade na CA volta em forma de degradação percebida — requisições ficam lentas e alguns navegadores exibem avisos de segurança, derrubando a confiança no portal. Ao ativar o stapling, o próprio Nginx carrega respostas atualizadas e elimina esse gargalo.

No Nginx, ative:

```nginx
server {
    ssl_stapling on;
    ssl_stapling_verify on;
    resolver 1.1.1.1 8.8.8.8 valid=300s;
    ssl_trusted_certificate /etc/nginx/certs/intermediate_ca.crt;
}
```

- `ssl_stapling on` permite que o próprio servidor apresente o status de revogação, eliminando a espera pelo veredito remoto.
- `ssl_stapling_verify on` garante que a resposta grampeada seja verificada antes de ser enviada, evitando a distribuição de respostas inválidas.
- O `resolver` define servidores DNS a serem usados para buscar o endereço do responder OCSP.
- `ssl_trusted_certificate` deve apontar para a cadeia da CA que assina seu certificado, para que o Nginx possa verificar a resposta OCSP.

### Testando o stapling

Mesmo após a configuração, precisamos comprovar que não há regressão: uma resposta OCSP expirada faria os navegadores apresentarem avisos novamente. Use `openssl s_client` com a opção `-status` para coletar a evidência de que o cartório digital está entregando o status de revogação atualizado diretamente no handshake:

```bash
openssl s_client -connect cartorio.local:443 -servername cartorio.local -tls1_3 -status
```

Procure pela seção `OCSP Response Status: successful`, acompanhada de um bloco `OCSP Response` contendo o `Cert Status: good` carimbado recentemente. Se aparecer `no response sent`, verifique se a CA suporta OCSP e se a diretiva `ssl_trusted_certificate` está correta.

## Outras boas práticas

- **Redirecionamento HTTP→HTTPS**: configure uma porta 80 que redirecione permanentemente para `https://` para garantir que todos os acessos sejam protegidos.
- **Headers de segurança**: além do HSTS, considere usar `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` e `Content-Security-Policy` apropriados.
- **Proteção contra downgrade**: mantenha somente versões seguras (TLS 1.2+ se for suportar clientes antigos) e desative ciphers inseguros.

Com essas configurações, seu ambiente de cartório digital terá conexões robustas, com validação de revogação e proteção contra ataques comuns.
