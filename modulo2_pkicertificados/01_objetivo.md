# Objetivo do módulo

Neste primeiro capítulo do Módulo 2, você descobrirá por que precisamos de uma Infraestrutura de Chaves Públicas (PKI) no cartório digital. No capítulo anterior, você aprendeu a gerar chaves, calcular hashes e assinar digitalmente arquivos. Mas, sem uma cadeia de confiança, qualquer pessoa poderia gerar um par de chaves e se passar por outra.

Uma PKI permite:
- Associar uma identidade (pessoa ou entidade) a uma chave pública por meio de um certificado digital, emitido por uma Autoridade Certificadora (CA) confiável.
- Provar a autenticidade de assinaturas digitais: ao verificar a assinatura, o sistema confia na cadeia de certificados que liga o certificado do assinante a uma raiz confiável.
- Revogar credenciais comprometidas e controlar o ciclo de vida dos certificados.

### Por que isso importa para o cartório digital?

No cartório digital, os cidadãos solicitarão serviços e assinarão documentos eletronicamente. É imprescindível que os servidores e os clientes saibam com quem estão se comunicando e possam confiar nas assinaturas. Para isso, precisaremos construir nossa própria CA interna, emitir certificados para serviços e clientes, e configurar aplicações para confiar nessa cadeia. Este módulo guiará você por essas etapas.

Ao final deste módulo, você será capaz de:

- Ler um certificado X.509 e entender seus principais campos.
- Criar uma CA raiz e uma CA intermediária.
- Emitir certificados de servidor e de cliente, e revogá‑los quando necessário.
- Validar uma cadeia de confiança e preparar as bases para usar esses certificados no TLS (módulo seguinte).

Vamos começar construindo a base: entendendo a estrutura de um certificado X.509.
