# Estrutura de um certificado X.509

## Introdução

Durante uma auditoria surpresa, o time de conformidade solicitou que o cartório digital comprovasse, em minutos, que todas as APIs internas estavam cobertas por certificados com SAN e EKU corretos. Em vez de entrar em pânico, a equipe de segurança abriu um laboratório, gerou um certificado temporário e exibiu cada campo na tela, mostrando como a estrutura X.509 torna visível a confiança que sustentamos. A demonstração arrancou aplausos e reforçou a importância de dominar cada detalhe.

## Conceitos Fundamentais

- **Subject:** identifica quem é o titular (pessoa, servidor ou sistema).
- **Issuer:** indica qual Autoridade Certificadora (CA) emitiu o certificado.
- **Validity:** define o intervalo de tempo em que o certificado é aceito.
- **Public Key:** apresenta o algoritmo e o tamanho da chave pública associada.
- **Extensions:** ampliam o comportamento, informando usos permitidos e pontos de validação.

Extensões relevantes:

- **Key Usage:** delimita operações (assinatura digital, cifra de chave, assinatura de certificados).
- **Extended Key Usage (EKU):** especifica finalidades adicionais (autenticação de servidor, cliente, assinatura de código, e-mail seguro).
- **Subject Alternative Name (SAN):** lista domínios e IPs adicionais aceitos.
- **CRL Distribution Points / Authority Information Access:** revelam onde consultar revogação via CRL ou OCSP.

## Práticas Reais

1. **Monte um certificado de laboratório para auditorias rápidas:**
   ```bash
   openssl req -x509 -newkey rsa:4096 -keyout tmp.key -out tmp.crt -days 30 -nodes \
       -subj "/C=BR/ST=Sao Paulo/L=Santo Andre/O=Cartorio Digital/OU=TI/CN=exemplo.local"
   ```

2. **Inspecione campos e extensões com clareza:**
   ```bash
   openssl x509 -in tmp.crt -noout -text
   ```
   Anote os elementos que precisam aparecer obrigatoriamente nos certificados oficiais.

3. **Crie um arquivo de configuração dedicado às extensões exigidas:**
   ```bash
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

4. **Valide se as extensões foram aplicadas corretamente:**
   ```bash
   openssl req -x509 -newkey rsa:4096 -keyout san.key -out san.crt -days 30 -nodes -config openssl-san.conf
   openssl x509 -in san.crt -noout -text | grep -A1 'Subject Alternative Name'
   ```

5. **Experimente curvas elípticas:** gere certificados com `prime256v1` ou `secp384r1` e registre as diferenças percebidas nos campos.

## Gancho para o Próximo Capítulo

Com a anatomia do certificado dominada, estamos prontos para construir a espinha dorsal da nossa PKI. Na Introdução do próximo capítulo levantaremos uma CA raiz e uma intermediária, destacando o impacto dessa estrutura na confiança do cartório digital.
