# Regulamento eIDAS e padrões ETSI

Enquanto a ICP‑Brasil regula certificados eletrônicos no Brasil, a União Europeia estabeleceu o Regulamento (UE) nº 910/2014, conhecido como eIDAS (Electronic Identification, Authentication and Trust Services), que cria um mercado único de serviços de confiança e identidades digitais.

## eIDAS

O eIDAS define três níveis de assinatura eletrônica:

- **Assinatura eletrônica simples** – Qualquer dado em formato eletrônico anexado ou associado a outros dados; tem baixo nível de garantia.
- **Assinatura eletrônica avançada** – Associa unicamente o signatário, permite sua identificação e é criada sob seu controle exclusivo; alterações nos dados são detectadas.
- **Assinatura eletrônica qualificada** – É uma assinatura avançada criada por um dispositivo qualificado (QSCD) e baseada em um certificado qualificado emitido por um prestador de serviços de confiança qualificado (QTSP). Tem equivalência à assinatura manuscrita em todos os Estados‑membros.

Para operar como QTSP, uma entidade deve cumprir requisitos técnicos e jurídicos verificados por organismos de avaliação e notificados à Comissão Europeia.

## Padrões ETSI

O European Telecommunications Standards Institute (ETSI) publica normas técnicas que dão suporte ao eIDAS. Algumas das mais relevantes são:

- **EN 319 401** – Requisitos gerais para prestadores de serviços de confiança (TSPs) que emitem certificados digitais.
- **EN 319 411‑1/2** – Requisitos específicos para TSPs que emitem certificados públicos; a parte 1 cobre “trust services” gerais e a parte 2 especifica certificados qualificados.
- **EN 319 412** – Definições e perfis de atributos em certificados de assinatura eletrônica; inclui a série de perfis “etsi‑qcStatement”.
- **EN 319 421** – Especifica o serviço de carimbo do tempo confiável (TSA).

Estas normas são complementadas por guias de implementação (Technical Specifications) como ETSI TS 119 312 (políticas de assinatura) e ETSI TR 119 441 (guidelines for non‑qualified certificates).

## Interoperabilidade com o Brasil

Embora a ICP‑Brasil não faça parte do eIDAS, compreender suas equivalências é importante para interoperabilidade internacional:

- O certificado A3 com token criptográfico atende a grande parte dos requisitos de um QSCD, mas o prestador (Autoridade Certificadora) deve cumprir requisitos adicionais de segurança (EN 319 411‑2) para ser considerado QTSP.
- Uma assinatura ICP‑Brasil pode ser reconhecida em âmbito europeu mediante acordos bilaterais, mas não tem presunção automática de validade. Inversamente, um certificado qualificado eIDAS pode ser utilizado no Brasil se o sistema do cartório digital implementar a validação de cadeias estrangeiras e manter um repositório de ACs europeias.

### Atividades

1. Acesse o texto do regulamento [eIDAS](https://eur-lex.europa.eu/eli/reg/2014/910/oj) e identifique as diferenças entre assinatura avançada e qualificada. Reflita sobre como essas categorias se relacionam com os termos “assinatura avançada” e “qualificada” definidos pela Lei 14.063/2020.
2. Pesquise as normas ETSI EN 319 411 e EN 319 412 e anote quais requisitos técnicos são adicionais para certificados qualificados em relação aos certificados “normais”.
