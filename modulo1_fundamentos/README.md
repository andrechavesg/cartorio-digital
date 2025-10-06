# Módulo 1 – Fundamentos Criptográficos

Este módulo estabelece a base conceitual para todo o restante do curso.  Você estudará a diferença entre criptografia simétrica e assimétrica, algoritmos de hash e assinaturas digitais.  Esses conhecimentos são indispensáveis para entender como funcionam os certificados X.509 e como proteger dados e chaves no cartório digital.

## Sumário dos capítulos

1. [Objetivo do módulo](01_objetivo.md) – Por que criptografia é essencial no cartório digital.  
2. [Criptografia simétrica vs. assimétrica](02_simetrica_assimetrica.md) – Conceitos e exemplos práticos com AES e RSA/ECDSA.  
3. [Funções hash e integridade](03_hashes_integridade.md) – Propriedades de um hash seguro e exercícios com SHA-256/HMAC.  
4. [Assinaturas digitais](04_assinaturas.md) – O que são, como funcionam e como aplicá-las no projeto.

## Objetivos de aprendizagem

- Entender cifras simétricas (AES, ChaCha20) e assimétricas (RSA, ECDSA, Ed25519);
- Compreender funções hash e mecanismos de integridade (SHA‑2, SHA‑3, HMAC);
- Aprender como são geradas e protegidas as chaves criptográficas.

## Entrega prática

No diretório `scripts/` há exemplos de geração de chaves, cálculo de hashes e medição de desempenho entre algoritmos.  Ao concluir este módulo, você deverá:

1. Gerar chaves RSA (2048 e 4096 bits) e curvas elípticas com `openssl`;
2. Calcular hashes de arquivos e mensagens com SHA‑256 e HMAC;
3. Comparar tempos de assinatura e verificação entre RSA, ECDSA e Ed25519;
4. Documentar suas conclusões em um arquivo `relatorio.md`.

## Referências recomendadas

- NIST SP 800‑57 – **Recomendação de gerenciamento de chaves** (tamanhos mínimos de chave e ciclos de vida);
- **Serious Cryptography**, Jean‑Philippe Aumasson – Livro que aborda fundamentos e práticas modernas de criptografia;
- Manuais do **OpenSSL** – Documentação oficial sobre geração de chaves e operações criptográficas.
