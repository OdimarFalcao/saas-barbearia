# SaaS Barbearia (MVP)

Sistema de gestão para barbearia com **clube de assinatura** e **comissão de barbeiros (Depote)**.
Single-tenant, focado em um cliente piloto.

**Stack:** Java 21 + Spring Boot (API REST) · React + TypeScript + Vite · PostgreSQL · Asaas.

## Documentação

- [`CLAUDE.md`](./CLAUDE.md) — manual de operação do Claude Code (ler primeiro).
- [`docs/REQUISITOS.md`](./docs/REQUISITOS.md) — requisitos do MVP (o quê).
- [`docs/SPEC.md`](./docs/SPEC.md) — especificação técnica (como).
- `docs/AMBIENTE.md` · `docs/TEST_PLAN.md` · `docs/SECURITY.md` · `docs/DEPLOY.md` · `docs/FUTURE.md` — a criar.

## Como trabalhamos

Spec-first, TDD, entregas incrementais e controle de escopo. Regra de negócio só na Service Layer; nada de "vibe coding". Detalhes em `CLAUDE.md`.

## Estado

Planejamento concluído (diagnóstico, SPEC, requisitos, manual). Próximo: ambiente isolado (`AMBIENTE.md`).
