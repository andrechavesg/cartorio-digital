# Construindo uma Autoridade Certificadora (CA)

## Exemplo Inspirador

O primeiro conselho consultivo do cartório digital exigiu uma prova concreta de que a confiança não estaria nas mãos de terceiros. A equipe de segurança levou o grupo para a sala-cofre, apresentou o plano de criação da CA raiz isolada e mostrou como uma intermediária operacional permitiria emissões rápidas sem comprometer a pedra fundamental. Quando a luz verde acendeu no painel indicando a geração bem-sucedida do certificado raiz, todos entenderam que o cartório tinha acabado de erguer o coração da sua PKI.

## Conceitos Fundamentais

- **CA raiz:** autoridade suprema da cadeia; deve permanecer offline e hiperprotegida.
- **CA intermediária:** executa as emissões do dia a dia, herdando a confiança da raiz.
- **Estrutura de diretórios organizada:** garante rastreabilidade, auditoria e backups consistentes.
- **Políticas de assinatura (`openssl.cnf`):** definem extensões, prazos e usos aceitos.
- **Cadeia de certificados:** concatenação da intermediária com a raiz para validações externas.

## Práticas Reais

1. **Estruture o repositório da PKI:**
   ```bash
   mkdir -p ~/pki/root/{certs,crl,newcerts,private}
   mkdir -p ~/pki/intermediate/{certs,crl,csr,newcerts,private}
   chmod 700 ~/pki/root/private ~/pki/intermediate/private
   touch ~/pki/root/index.txt ~/pki/intermediate/index.txt
   echo 1000 > ~/pki/root/serial
   echo 1000 > ~/pki/intermediate/serial
   ```
   Crie arquivos `openssl.cnf` específicos para a raiz e a intermediária.

2. **Gere a chave e o certificado da CA raiz:**
   ```bash
   cd ~/pki/root
   openssl genrsa -aes256 -out private/ca.key.pem 4096
   chmod 400 private/ca.key.pem
   openssl req -config openssl.cnf \
         -key private/ca.key.pem \
         -new -x509 -days 3650 -sha256 -extensions v3_ca \
         -out certs/ca.cert.pem
   openssl x509 -noout -text -in certs/ca.cert.pem
   ```
   Documente a senha em cofre seguro e registre quem tem acesso autorizado.

3. **Crie a CA intermediária operacional:**
   ```bash
   cd ~/pki/intermediate
   openssl genrsa -aes256 -out private/intermediate.key.pem 4096
   chmod 400 private/intermediate.key.pem
   openssl req -config openssl.cnf -new -sha256 \
         -key private/intermediate.key.pem \
         -out csr/intermediate.csr.pem

   cd ~/pki/root
   openssl ca -config openssl.cnf -extensions v3_intermediate_ca \
         -days 1825 -notext -md sha256 \
         -in ~/pki/intermediate/csr/intermediate.csr.pem \
         -out ~/pki/intermediate/certs/intermediate.cert.pem
   chmod 444 ~/pki/intermediate/certs/intermediate.cert.pem
   openssl x509 -noout -text -in ~/pki/intermediate/certs/intermediate.cert.pem
   ```

4. **Monte a cadeia de confiança:**
   ```bash
   cat ~/pki/intermediate/certs/intermediate.cert.pem \
       ~/pki/root/certs/ca.cert.pem > ~/pki/intermediate/certs/ca-chain.cert.pem
   chmod 444 ~/pki/intermediate/certs/ca-chain.cert.pem
   ```
   Distribua essa cadeia para todas as equipes que precisarão validar certificados.

5. **Explore alternativas ágeis com `step-ca`:**
   ```bash
   step ca init --name "Cartório Digital" --dns localhost --address :9000
   ```
   Utilize a ferramenta para laboratórios rápidos e compare os fluxos gerados automaticamente com a estrutura manual.

## Próximos passos

Com a raiz guardada e a intermediária pronta para emitir, é hora de colocar essa estrutura em ação. No próximo capítulo emitiremos certificados de servidor e de cliente, começando por um exemplo inspirador que mostra como uma nova API do cartório ganhou vida graças à nossa própria CA.
