# ICP-Brasil e Legislação Nacional

Para que o cartório digital cumpra requisitos legais, é preciso conhecer a estrutura da Infraestrutura de Chaves Públicas Brasileira (ICP-Brasil). Este capítulo aprofunda a base normativa e os tipos de certificados disponíveis no Brasil.

## Normas e leis

- **MP 2.200-2/2001** — Institui a ICP-Brasil e cria o Comitê Gestor. Estabelece a fé pública dos documentos eletrônicos assinados digitalmente por certificados ICP-Brasil.
- **Lei 14.063/2020** — Dispõe sobre assinaturas eletrônicas (simples, avançada e qualificada) e define que a qualificada utiliza certificado ICP.
- **Lei 14.382/2022** — Moderniza os registros públicos e institui o Serp; determina que atos cartorários podem ser praticados eletronicamente mediante assinatura avançada ou qualificada.
- **DOC-ICP-05** — Declaração de Práticas de Certificação; define responsabilidades das Autoridades Certificadoras.

Leia# ICP-Brasil
 esses documentos e identifique requisitos de implementação, como políticas de armazenamento e procedimentos de revogação.

## Tipos de certificados

| Classe | Armazenamento | Validade | Uso típico |
| ------ | -------------- | -------- | ---------- |
| **A1** | Software, arquivo .p12 | 1 ano | Assinaturas em massa, servidores web |
| **A3** | Cartão ou token criptográfico | 1 a 5 anos | Assinatura de documentos com fé pública (Notários) |
| **A5** | Módulo de segurança (HSM) | 5 anos | Autoridades certificadoras, carimbadores do tempo |

Os certificados A3 e A5 são considerados **qualificados**, pois a chave privada é gerada e protegida em hardware certificado.

## Requisitos práticos para o projeto

- Armazenar as chaves privadas de oficiais do cartório em tokens (A3) ou HSM (A5) para garantir segurança e conformidade.
- Validar a cadeia de confiança dos certificados usando `openssl verify` e configurar OCSP ou CRL para revogação.
- Implementar políticas de acesso e senha nos dispositivos.
- Manter cópias de segurança da chave raiz em cofre físico ou HSM offline.

````bash
# Exemplo: verificando um certificado
openssl verify -CAfile cadeia_icp_brasil.pem cert_oficial.pem
```

## Atividades

1. Pesquise no site do ITI (Instituto Nacional de Tecnologia da Informação) os documentos DOC-ICP-03, DOC-ICP-05 e DOC-ICP-15 e anote as exigências técnicas para emissores e usuários.
2. Gere um par de chaves e CSR de teste com `openssl req -newkey rsa:2048 -keyout chave.pem -out pedido.csr` e observe as extensões necessárias.
3. Experimente importar um certificado ICP-Brasil no seu navegador e explore os campos (Subject, Issuer, Key Usage).
4. Liste as diferenças práticas entre certificados A1, A3 e A5 e discuta em qual situação cada um é adequado para o cartório digital.