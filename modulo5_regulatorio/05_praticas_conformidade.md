# Práticas de Conformidade e Auditoria

Para fechar o módulo de normas, precisamos transformar a teoria em práticas concretas que garantam que o cartório digital opere dentro das exigências legais e regulatórias. Este capítulo apresenta um checklist de conformidade e sugestões para evoluir o projeto.

## Checklist de conformidade

- **Políticas de certificação**: garanta que sua CA interna e os certificados emitidos estejam alinhados às políticas ICP‑Brasil (DOC‑ICP‑05, DOC‑ICP‑15) e às normas ETSI EN 319 412. Defina e publique a Declaração de Práticas de Certificação (DPC) do seu cartório digital.
- **Gestão de chaves e backups**: use HSM ou AWS KMS para armazenar chaves privadas de CA e servidores; implemente rotação periódica e cópias de segurança com controle de acesso.
- **Validação de cadeia e revogação**: configure seu sistema para validar automaticamente a cadeia de confiança, verificar CRLs e OCSP de todas as CAs externas (ICP‑Brasil, QTSP europeus) e registrar as respostas.
- **Logs e carimbo do tempo**: registre todas as operações de emissão, assinatura e validação em logs imutáveis e use carimbo do tempo (RFC 3161 / ETSI EN 319 421) para comprovar a data e hora das ações.
- **Proteção de dados pessoais**: adeque‑se à LGPD (Lei Geral de Proteção de Dados) e GDPR, garantindo consentimento, finalidade e minimização no armazenamento de dados de clientes e signatários.

## Evoluindo o projeto

Além do checklist, considere as seguintes evoluções para o cartório digital:

- **Integração com autoridades externas**: implemente clientes API para consultar a EU Trusted List, validar certificados europeus e obter atualizações de CRL/OCSP.
- **Assinatura em nuvem**: avalie provedores de assinatura qualificada em nuvem (QSCD remoto) para reduzir a dependência de tokens físicos e facilitar o uso por clientes.
- **Auditorias periódicas**: agende auditorias técnicas e legais (internas ou por terceiros) para avaliar conformidade com ICP‑Brasil, eIDAS e ETSI. Documente resultados e implemente melhorias.
- **Interoperabilidade**: mantenha um mapeamento de OIDs e perfis de certificado aceitos (ICP‑Brasil, eIDAS, NIST) e atualize seu sistema conforme novas versões das normas sejam publicadas.
- **Treinamento contínuo**: ofereça capacitação regular para os funcionários sobre legislação, segurança e novas práticas, reforçando a cultura de conformidade no cartório.

## Atividades

1. Revise sua implementação de CA interna e verifique se todos os requisitos do DOC‑ICP‑05 (Política e Requisitos de Certificação) estão atendidos.
2. Configure monitoramento automatizado para checar expirations e revogações de certificados importados (ICP‑Brasil e QTSPs), alertando o time de operações.
3. Elabore um documento de DPC para o cartório digital, inspirado nas normas ETSI EN 319 401 e DOC‑ICP‑15, descrevendo como as chaves são geridas, como ocorrem as auditorias e como os certificados são emitidos.
4. Realize uma auditoria interna simulada: selecione um conjunto de operações de assinatura realizadas no seu ambiente de teste e verifique se todos os logs, carimbos do tempo e políticas foram aplicados corretamente.
5. Pesquise fornecedores de QSCD remoto e avalie prós e contras de adotá‑los no seu projeto.

Com isso, finalizamos o módulo de Normas e Regulamentações. Nos próximos capítulos, você colocará essas práticas em uso no projeto final integrador, garantindo que o cartório digital opere de forma segura, legalmente válida e interoperável.
