# CLAUDE.md — Manual de operação do Claude Code

> Este arquivo é a **memória e o contrato de trabalho** do Claude Code neste repositório.
> **Leia este arquivo inteiro antes de qualquer ação.** Em seguida, consulte `docs/SPEC.md` e `docs/REQUISITOS.md`.

---

## 1. O que é este projeto

SaaS de gestão para **uma barbearia cliente** (single-tenant) com **clube de assinatura** e **barbeiros parceiros por comissão**. O cliente já usa um **site só para agendamento** e **não tem gestão de pagamentos, assinatura, comissão nem relatórios** — é essa lacuna (a camada de dinheiro e informação) que preenchemos. A entrevista com uma usuária do Cash Barber foi apenas **benchmark de domínio**; **não** estamos substituindo o Cash Barber. O produto é **genérico e reaproveitável** entre clientes (nada hardcoded para o piloto), com **uma instância single-tenant por barbearia**.

**Objetivos da aplicação (em ordem):**
1. Entregar o **núcleo operacional**: agenda (com **autoagendamento do cliente**) → atendimento → comanda → caixa.
2. Entregar o **diferencial de assinatura**: planos, cobrança recorrente, inadimplência e **Depote** — correto e auditável.
3. Corrigir já no dia 1: **edição de comanda fechada com auditoria** e **totais agregados no caixa**.
4. Ser **simples, seguro e testável**, sem overengineering.

**Critério de sucesso do MVP:** a barbearia piloto roda um dia inteiro de operação e um ciclo de cobrança de assinatura sem recorrer a planilha paralela.

## 2. Stack

- **Backend:** Java 21 · Spring Boot 4 (Web, Security, Data JPA, Validation) · PostgreSQL 16 · Flyway · **Gradle (Kotlin DSL)** · springdoc-openapi.
- **Frontend:** React 18 · TypeScript · Vite · Tailwind · shadcn/ui · TanStack Query · React Router · React Hook Form + Zod · Axios.
- **Pagamentos:** Asaas (recorrência + webhooks).
- **Testes:** JUnit 5, AssertJ, Mockito, Spring Boot Test (MockMvc), Testcontainers · Vitest, React Testing Library, Playwright.
- **Auth:** sessão por cookie HttpOnly + CSRF (SPA e API no mesmo domínio).

Detalhes e justificativas: `docs/SPEC.md`.

## 3. Estrutura do repositório

```
saas-barbearia/
├─ CLAUDE.md            # este arquivo
├─ README.md
├─ docs/
│  ├─ REQUISITOS.md     # o quê (requisitos do MVP)
│  ├─ SPEC.md           # como (arquitetura, modelos, regras)
│  ├─ AMBIENTE.md       # setup do ambiente isolado (a criar)
│  ├─ TEST_PLAN.md      # plano de testes
│  ├─ SECURITY.md       # cuidados de segurança (a criar)
│  ├─ DEPLOY.md         # plano de publicação (a criar)
│  └─ FUTURE.md         # ideias/itens adiados (a criar)
├─ backend/             # Spring Boot (package-by-feature)
└─ frontend/            # React + Vite
```

Organização do backend **por funcionalidade** (`usuario`, `cadastro`, `cliente`, `agenda`, `comanda`, `assinatura`, `comissao`, `estoque`, `common`). Camadas: `controller → service → repository → domain`. Ver `docs/SPEC.md §7`.

## 4. Metodologia de trabalho (obrigatória)

Trabalhamos **spec-first, com TDD e em entregas incrementais**. A IA é apoio dentro de um processo controlado — **não** "vibe coding".

**Antes de codar qualquer coisa:** SPEC e REQUISITOS devem cobrir o que será feito.

**Para CADA funcionalidade nova, sempre nesta ordem:**
1. **Consultar/atualizar** `docs/SPEC.md` e `docs/REQUISITOS.md`.
2. **Avaliar impacto** na arquitetura e nas regras de negócio.
3. **Atualizar** `docs/TEST_PLAN.md` se necessário.
4. **Escrever os testes primeiro** (ou junto), cobrindo regras e casos de erro.
5. **Implementar a menor mudança possível** para os testes passarem.
6. **Rodar os testes** e verificar que nada anterior quebrou.
7. **Revisar** o código gerado (clareza, segurança, camadas corretas).
8. **Relatar** o que mudou (arquivos, decisões, riscos).

Trabalhe em **passos pequenos**. Nunca implemente várias funcionalidades grandes de uma vez.

### 4.1 Práticas-chave (XP com IA)
Lições de quem já levou projeto a produção com IA sob disciplina (Fabio Akita, *The M.Akita Chronicles* — +1.300 testes, software em produção):
- **One-shot prompt é mito.** Nenhum prompt entrega o sistema pronto; o software diverge da spec em horas. Trabalhamos por iteração curta, não por "prompt mágico".
- **Disciplina é freio e direção.** Sem XP, a IA acumula dívida técnica mais rápido ainda; com XP, o software evolui de verdade. Programar com IA sem processo é receita de desastre.
- **TDD é inegociável.** Os testes são a rede que deixa a IA andar rápido sem quebrar o que existe — e ajudam a achar bugs.
- **Refactoring contínuo.** Refatorar faz parte do ciclo, não é evento futuro.
- **Commits pequenos e frequentes.** Muitos commits pequenos > poucos grandes (rastreabilidade e rollback).
- **A spec evolui.** `SPEC.md`, `REQUISITOS.md` e este `CLAUDE.md` são documentos vivos — atualizar a cada feature.
- **A IA é o par júnior; o humano é o sênior.** Odimar dirige, questiona e **revisa todo o código** antes de aceitar. Nunca dar merge no escuro.
- **Software nunca está "pronto".** Planejar a vida pós-deploy: observabilidade, bugs reais, manutenção.

## 5. Regras obrigatórias (não violar)

- ❌ Não gerar código antes de entender o contexto (ler SPEC/REQUISITOS).
- ❌ Não inventar requisitos que não foram pedidos.
- ❌ Não reescrever o projeto inteiro sem necessidade.
- ❌ Não ignorar nem pular testes.
- ❌ Não expor chaves, tokens ou senhas (segredos só em variáveis de ambiente).
- ❌ Não misturar regra de negócio em controller, repositório, template ou no frontend.
- ❌ Não fazer otimização prematura.
- ❌ Não esconder limitações ou riscos.
- ✅ Sempre explicar decisões técnicas relevantes.
- ✅ Toda **regra de negócio vive na Service Layer** do backend. O frontend nunca é fonte da verdade financeira.
- ✅ Ações críticas (editar comanda fechada, alterar assinatura, mudar permissão) **sempre** geram log de auditoria.
- ✅ Valores monetários em `BigDecimal`/`NUMERIC`, nunca `double`/`float`.
- ✅ Ideia boa fora de escopo → registrar em `docs/FUTURE.md`, **não** implementar agora.
- ✅ Commits **pequenos e frequentes**; cada feature com testes antes do merge.
- ✅ Refatorar continuamente e manter `SPEC.md`/`REQUISITOS.md`/`CLAUDE.md` vivos a cada feature.

## 6. Controle de escopo

Estamos num **MVP enxuto**. Antes de adicionar qualquer coisa, pergunte: "isto está no escopo do MVP em `REQUISITOS.md §4`?" Se for §5 (fora do escopo) ou novo, **não implemente** — registre em `FUTURE.md` e siga. Evite abstrações complexas cedo demais; priorize simplicidade, clareza, segurança e manutenção.

## 7. Áreas de risco (atenção redobrada + testes)

- **Depote / comissão (RN-05):** dinheiro entre parceiros. Erro quebra a confiança da parceria. **Cobertura de testes exaustiva**, incluindo arredondamento e bordas.
- **Pagamento recorrente (Asaas):** webhooks idempotentes, conciliação, inadimplência. Não escrever gateway próprio.
- **Inadimplência/bloqueio (RN-01), desconto (RN-02), totais de caixa (RN-04):** regras testáveis e sensíveis.

> **Depote definido (RN-05):** ficha é **peso configurável por serviço** (ex.: corte = 40, barba = 30, pezinho = 20); `pote (R$)` = percentual de comissão × receita de assinatura do mês; comissão = `(fichas do barbeiro ÷ fichas totais) × pote`. Fichas contam só para serviços em **assinantes**.
> **Pendências abertas:** **comissão de avulso/produtos** e detalhes do **autoagendamento do cliente** (conta vs. só nome/telefone; confirmação por link/WhatsApp). Agenda própria já confirmada. Ver `REQUISITOS.md §9`.

## 8. Convenções

- **Backend:** nomes em português no domínio (Comanda, Assinatura); DTOs separados das entidades; validação Jakarta nos DTOs + invariantes no Service; `@Transactional` em operações de comanda/caixa/cobrança; migrations Flyway versionadas (`ddl-auto: validate`).
- **Frontend:** tipos em `types/` espelhando os DTOs da API; chamadas via hooks TanStack Query em `api/`; validação com Zod espelhando as regras (apenas UX — o backend valida de verdade).
- **Commits:** pequenos e descritivos (sugestão: Conventional Commits — `feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
- **Idioma:** docs e discussão em pt-BR.

## 9. Comandos

> A preencher quando o ambiente estiver montado (`docs/AMBIENTE.md`). Esperado:

```bash
# Backend
cd backend && ./mvnw spring-boot:run      # rodar API
cd backend && ./mvnw test                 # testes backend

# Frontend
cd frontend && npm run dev                 # rodar SPA
cd frontend && npm test                    # testes frontend

# Ambiente completo (dev)
docker compose up
```

## 10. Definition of Done (uma feature só está pronta quando)

1. Atende ao requisito descrito em `REQUISITOS.md`.
2. Tem testes que cobrem regra de negócio e casos de erro, e **todos passam**.
3. Não quebrou nada existente.
4. Regra de negócio está na camada correta (Service).
5. Sem segredos no código; entradas validadas; ação crítica auditada.
6. Documentação relevante (SPEC/TEST_PLAN) atualizada.
7. Mudança relatada com decisões e riscos.

## 11. Estado atual do projeto

- ✅ Etapa 1 — Diagnóstico.
- ✅ Etapa 2 — `SPEC.md`.
- ✅ Documento de requisitos do MVP (`REQUISITOS.md`).
- ✅ Manual do Claude Code (este arquivo).
- ✅ Etapa 3 — `AMBIENTE.md` (WSL2, .env, Postgres na porta 5433, GitHub via SSH).
- ✅ Etapa 4 — arquitetura detalhada / scaffolding (Gradle Kotlin DSL, Spring Boot 4.0.6, monorepo).
- ✅ Etapa 5 — `TEST_PLAN.md` (casos D01–D12 do Depote, webhooks, comanda, caixa; rastreabilidade por ID).
- ⬜ Etapas 6+ — implementação incremental (TDD), segurança, CI/CD, deploy.

**Próximo passo objetivo:** iniciar a Etapa 6 — primeira fatia vertical com TDD (sugestão: autenticação/perfis + migrations iniciais, base para todo o resto). Antes de implementar o Depote, fixar na SPEC a regra de arredondamento (proposta no TEST_PLAN §4.1) e resolver as pendências de `REQUISITOS.md §9`.
