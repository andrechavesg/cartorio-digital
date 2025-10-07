# 2. Criptografia Simétrica e Assimétrica

## Exemplo Inspirador

Durante o mutirão mensal de emissão de certidões, o cartório digital precisa enviar lotes completos para a central estadual enquanto atende, ao mesmo tempo, um pedido judicial urgente que exige confidencialidade extrema. A coordenadora de TI conduz a equipe por um plano em duas fases: primeiro, proteger o lote com criptografia simétrica para ganhar velocidade; em seguida, distribuir novas chaves através de um par assimétrico para que apenas as unidades autorizadas acessem o conteúdo. O brilho nos olhos do time mostra que a combinação correta de estratégias transforma desafios logísticos em vitórias inspiradoras.

## Conceitos Fundamentais

### Criptografia Simétrica

- A mesma chave cifra e decifra.
- Ideal para grandes volumes e processamento rápido.
- Depende de canais seguros para o compartilhamento da chave.
- Algoritmos de destaque: **AES** (128, 192 e 256 bits) e **ChaCha20-Poly1305**.

### Criptografia Assimétrica

- Utiliza um par de chaves: **pública** (compartilhável) e **privada** (secreta).
- Ótima para troca de chaves simétricas e assinaturas digitais.
- Permite autenticação sem revelar o segredo principal.
- Algoritmos de destaque: **RSA**, **ECC** (P-256, Curve25519) e **Ed25519**.

### Combinação Vitoriosa

Em sistemas modernos, usamos a força da simetria para cifrar dados e a elegância da assimetria para distribuir chaves e garantir autenticidade. Essa dupla é o motor que sustenta transações seguras em cartórios digitais.

## Práticas Reais

1. **Proteja um lote confidencial:**
   ```bash
   openssl enc -aes-256-cbc -salt -in mensagem.txt -out mensagem.enc
   ```
   Documente quem conhece a senha compartilhada e como o acesso será auditado.

2. **Valide a recuperação do conteúdo:**
   ```bash
   openssl enc -d -aes-256-cbc -in mensagem.enc -out mensagem.dec
   ```
   Compare os arquivos e registre a evidência no diário do projeto.

3. **Gere um par de chaves institucional:**
   ```bash
   openssl genpkey -algorithm RSA -out rsa_private.pem -pkeyopt rsa_keygen_bits:2048
   openssl rsa -pubout -in rsa_private.pem -out rsa_public.pem
   ```
   Compartilhe apenas a chave pública com a equipe e anote o motivo da escolha do tamanho da chave.

4. **Explore curvas elípticas para desempenho:**
   ```bash
   openssl genpkey -algorithm EC -out ec_private.pem -pkeyopt ec_paramgen_curve:P-256
   openssl pkey -pubout -in ec_private.pem -out ec_public.pem
   ```
   Registre a percepção de performance e como isso impactará o atendimento aos cidadãos.

## Gancho para o Próximo Capítulo

Proteger o acesso é poderoso, mas ainda precisamos de uma bússola que acuse qualquer alteração inesperada em nossos documentos. No próximo capítulo vamos descobrir, guiados por outro exemplo do cartório, como as funções hash se tornam guardiãs da integridade, preparando-nos para assinaturas digitais impecáveis.
