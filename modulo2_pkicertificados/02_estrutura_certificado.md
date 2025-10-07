# Estrutura de um certificado X.509

Um certificado digital é um arquivo que associa a chave pública de alguém (ou de um servidor) a uma identidade verificada. No padrão X.509, o certificado contém vários campos que precisamos conhecer:

| Campo        | Descrição |
|-------------|-----------|
| **Subject** | Identidade do titular do certificado (pessoa, servidor, ou entidade). |
| **Issuer**  | Autoridade Certificadora (CA) que emitiu o certificado. |
| **Validity**| Intervalo de datas em que o certificado é válido (Not Before / Not After). |
| **Public Key** | A chave pública do titular, com algoritmo e tamanho. |
| **Extensions** | Campos adicionais que especificam usos permitidos, nomes alternativos e políticas.|

Algumas extensões importantes:

- **Key Usage**: indica operações permitidas (assinatura digital, cifra de chave, assinatura de certificado, etc.).
- **Extended Key Usage (EKU)**: define finalidades adicionais (autenticação de servidor, cliente, assinatura de código, e‑mail seguro).
- **Subject Alternative Name (SAN)**: lista domínios ou IPs adicionais para os quais o certificado é válido.
- **CRL Distribution Points / Authority Information Access**: URLs para verificar revogação via CRL ou OCSP.

### Explorando um certificado na prática

1. Quando o time precisou revisar um incidente envolvendo um certificado provisório, percebemos que faltava um laboratório rápido para experimentar alterações. **Problema (auditar SAN/EKU para APIs do cartório):** validar ajustes sem arriscar certificados reais. **Solução (inspeção preparatória):** gerar um certificado autoassinado efêmero com `openssl req -x509` para conduzir os testes de auditoria.

   ```bash
   openssl req -x509 -newkey rsa:4096 -keyout tmp.key -out tmp.crt -days 30 -nodes -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=TI/CN=exemplo.local"
   ```

2. Em outra auditoria, a equipe de conformidade tinha dificuldade em enxergar rapidamente campos como SAN e EKU nos certificados emitidos. **Problema (auditar SAN/EKU para APIs do cartório):** ausência de visibilidade detalhada para comprovar as extensões exigidas. **Solução (inspeção analítica):** usar `openssl x509` para inspecionar o arquivo e desbloquear essas informações de forma legível.

   ```bash
   openssl x509 -in tmp.crt -noout -text
   ```

   Observe os campos *Subject*, *Issuer*, *Validity* e as extensões como *Key Usage* e *SAN*. Quando precisamos garantir que todos os domínios alternativos estivessem documentados, reiteramos o desafio principal — **problema (auditar SAN/EKU para APIs do cartório):** cumprir o checklist das APIs internas. **Solução (inspeção configurada):** criar uma configuração dedicada com `openssl-san.conf` que explicita cada extensão exigida.

   ```bash
   # Crie um arquivo openssl.conf minimal com uma seção req e extensions:
   cat > openssl-san.conf <<'EOF'
   [req]
   distinguished_name = dn
   x509_extensions = ext
   prompt = no

   [dn]
   C = BR
   ST = Sao Paulo
   L = Santo Andre
   O = Cartorio Digital
   OU = TI
   CN = exemplo.local

   [ext]
   subjectAltName = @alt_names
   keyUsage = critical, digitalSignature, keyEncipherment
   extendedKeyUsage = serverAuth, clientAuth

   [alt_names]
   DNS.1 = exemplo.local
   DNS.2 = www.exemplo.local
   IP.1  = 127.0.0.1
   EOF
   ```

   O próximo desafio foi confirmar se a nova SAN aparecia corretamente para a auditoria. **Problema (auditar SAN/EKU para APIs do cartório):** provar que a extensão exigida ficou registrada na emissão final. **Solução (inspeção validatória):** gerar novamente o certificado apontando para o arquivo de configuração e usar `openssl x509` com `grep` para validar o ajuste.

   ```bash
   # Gere outro certificado autoassinado com SAN:
   openssl req -x509 -newkey rsa:4096 -keyout san.key -out san.crt -days 30 -nodes -config openssl-san.conf
   openssl x509 -in san.crt -noout -text | grep -A1 'Subject Alternative Name'
   ```

3. Experimente criar certificados EC (ECDSA) com curvas `prime256v1` ou `secp384r1` e compare os tamanhos das chaves e extensões.

Compreender a estrutura de um certificado é essencial para definir os campos corretos ao emitirmos nossos próprios certificados no cartório digital. No próximo capítulo, criaremos uma CA raiz e uma CA intermediária para começar a emitir certificados confiáveis.
