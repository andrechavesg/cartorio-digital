# Frontend – Portais de Operação da AC

O módulo frontend concentra os portais usados por agentes de registro, operadores e auditores. Cada portal utiliza Next.js 14 com React, TypeScript, Tailwind e integrações mTLS com o backend.

## Portais

- operator-portal – Gestão de emissões, monitoramento e fluxo de aprovação dual-control.
- ra-portal – Onboarding de titulares, conferência de dossiês, registro de evidências conforme DOC-ICP-05.
- auditor-portal – Visualização de trilhas imutáveis, relatórios e indicadores de conformidade.
- shared – Componentes de design system, autenticação e providers utilizados pelos portais.

## Requisitos

- Autenticação forte (WebAuthn + MFA) com verificação mTLS.
- Consentimentos e logs assinados com carimbo do tempo.
- Acessibilidade AA (WCAG 2.1) e internacionalização PT-BR/EN.

## Próximas ações

1. Inicializar monorepo com Turborepo e workspaces para cada portal.
2. Implementar temas e componentes críticos no diretório shared.
3. Construir fluxos descritos em 04_frontend_ux.md incluindo testes E2E (Playwright).
4. Automatizar verificações de acessibilidade e segurança no pipeline ci.
