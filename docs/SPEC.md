# SPEC.md — Sistema de Gestão de Barbearia (MVP)

> Versão 0.1 — Especificação técnica do MVP. Base: `Requisitos_Sistema_Barbearia (1).docx` (entrevista de campo, out/2025).
> Decisões fixadas: **MVP enxuto · single-tenant · cliente piloto real · backend Java/Spring Boot (API REST) · frontend React SPA desacoplado**.

---

## 1. Visão geral

SaaS de gestão para uma **barbearia cliente** que hoje só **agenda online** clientes e serviços e **não tem gestão de pagamentos, assinatura, comissão ou relatórios**. O sistema entrega exatamente essa **camada de dinheiro e informação**: gerir **recorrência, inadimplência e a distribuição justa de comissão de assinatura (Depote)**, caixa e relatórios. A entrevista com uma usuária do Cash Barber foi **benchmark de domínio** — **não** estamos substituindo o Cash Barber.

O produto é **genérico e configurável** (não amarrado aos dados de nenhum cliente), pensado para ser reaproveitado em várias barbearias. Cada cliente roda a **própria instância single-tenant**; a barbearia piloto valida o MVP. Multi-tenant verdadeiro é evolução futura (ver §17).

## 2. Objetivos do MVP

1. Entregar a **camada de pagamentos** que falta ao cliente: atendimento → comanda → caixa, com os cadastros de apoio.
2. Entregar o **diferencial de assinatura**: planos, cobrança recorrente, controle de inadimplência e **Depote** (comissão por fichas) — correto e auditável.
3. Garantir, desde o dia 1, **edição de comanda fechada com auditoria** e **totais agregados no caixa** (boas práticas aprendidas no benchmark).
4. Ser **simples, seguro e testável** — código com testes, sem overengineering, pronto para crescer.

Critério de sucesso: a barbearia piloto registra os atendimentos, fecha o caixa com totais corretos e roda um ciclo de cobrança de assinatura com o **Depote calculado automaticamente** — sem planilha paralela.

## 3. Funcionalidades incluídas (MVP)

Referência aos IDs do doc de requisitos:

- **Cadastros:** empresa (CF03), barbeiros e jornada/intervalos (CF01, AG06), serviços com fichas (CF02), formas de pagamento (CF06), clientes com origem (CL01).
- **Acesso:** autenticação e controle por perfil Admin / Recepção / Profissional (CF04, NF03).
- **Agenda:** visão por barbeiro (AG01), códigos visuais essenciais (AG02 — assinante/avulso/novo), confirmação manual/por link (AG03), marcar chegada (AG04), ações sobre agendamento incl. falta (AG05), bloqueios de intervalo/folga (AG06), **bloqueio de inadimplente** (AG07), painel resumido do dia (AG08).
- **Comanda/Caixa:** abrir a partir de agendamento (CX01), desconto automático do plano (CX02), formas de pagamento (CX03), fechar comanda (CX04), histórico com filtros (CX05), **[MELHORIA] editar comanda fechada com log de auditoria** (CX06), caixa por turno (CX07), **[MELHORIA] totais agregados no caixa** (CX08), alerta de comanda aberta antiga (CX09).
- **Assinatura:** cadastro de planos (AS01), **gateway único integrado (Asaas)** com cobrança recorrente, dashboard de assinatura (AS03), gestão de inadimplência com recobrança (AS04), limite de vagas por plano (AS07).
- **Depote / Comissão:** cálculo de comissão por fichas (AS09) e relatório de comissão por barbeiro em PDF (AS10).
- **Estoque (básico):** catálogo (ES01), saída automática ao fechar comanda com produto (ES02), estoque mínimo com alerta (ES03).
- **Dashboard operacional enxuto** (DB01, subconjunto).

## 4. Funcionalidades fora do escopo inicial → `FUTURE.md`

Pump (PU01–07), mapa de calor (DB03), dashboard estratégico (DB02), relatórios avançados e exportações (RE01–08, exceto o PDF de comissão), marketing/clube/cupons/banners (MK01–05), cashback, presentes (CL06), avaliações (CL07), aniversariantes/top/recompra/inativos como features dedicadas (CL02–05), alteração de plano em massa (AS05), período de bloqueio pós-cancelamento e lista de espera (AS06, AS07-espera), contrato com aviso prévio (AS11), fábrica de planos (AS12), **multi-unidade (CF05)**, financeiro completo — contas a pagar, contas bancárias, categorias, conciliação (FI02–05), múltiplos gateways (AS02). **Multi-tenancy** é a maior evolução futura (§17).

## 5. Stack recomendada

| Camada | Tecnologia | Justificativa |
|---|---|---|
**Backend (API REST):**

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Linguagem | **Java 21 (LTS)** | Estável, tipagem forte — boa para regra financeira. |
| Framework | **Spring Boot 4.x** (Web, Security, Data JPA, Validation) | API REST JSON. Maduro, baterias incluídas, ótimo ecossistema de testes. Spring Boot 4.0.6 gerado na Etapa 4. |
| Banco | **PostgreSQL 16** | Robusto, transacional — adequado a dinheiro. |
| Migrations | **Flyway** | Versionamento de schema reproduzível. |
| Build | **Gradle (Kotlin DSL)** | Build incremental mais rápido; DSL concisa. Decidido na Etapa 4. |
| Pagamentos | **Asaas** (recorrência + webhooks) | Brasileiro, taxas baixas, recorrência nativa e *split* (conversa com a comissão). |
| PDF | **OpenPDF** ou **Flying Saucer** (HTML→PDF) | Relatório de comissão (AS10). |
| Doc API | **springdoc-openapi (Swagger UI)** | Contrato da API para o front e para testes. |
| Testes | **JUnit 5 + AssertJ + Mockito + Spring Boot Test (MockMvc) + Testcontainers** | Unidade (Service/Depote), web (MockMvc), integração com Postgres real. |
| Qualidade | **Spotless** + **Checkstyle** + **OWASP Dependency-Check** | Lint e dependências vulneráveis no CI. |

**Frontend (SPA):**

| Item | Tecnologia | Justificativa |
|---|---|---|
| Base | **React 18 + TypeScript + Vite** | Padrão moderno, build rápido, tipagem alinhada aos DTOs da API. |
| Estilo/UI | **Tailwind CSS + shadcn/ui** | Componentes prontos e acessíveis — acelera muito a dupla. |
| Dados/estado servidor | **TanStack Query** | Cache, revalidação e loading/error sem boilerplate. |
| Rotas | **React Router** | Navegação SPA por perfil. |
| Formulários/validação | **React Hook Form + Zod** | Validação no cliente espelhando as regras da API. |
| HTTP | **Axios** (instância com cookies + interceptors) | Sessão por cookie e tratamento central de erro 401/403. |
| Testes | **Vitest + React Testing Library** (unidade/componente) · **Playwright** (e2e) | Cobre componentes e fluxos de usuário. |

## 6. Arquitetura geral (diagrama textual)

```
Navegador (recepção desktop · barbeiro celular · admin)
        │  HTTPS
        ▼
   Caddy (proxy reverso + HTTPS automático)  ── mesmo domínio
   ├─ "/"      → arquivos estáticos do React SPA (build Vite)
   └─ "/api/*" → Spring Boot (API REST)
        │
        ▼
┌──────────── React SPA (TypeScript) ───────────┐
│  Páginas/rotas por perfil · componentes UI    │
│  TanStack Query  ─→ chama /api (Axios+cookie)  │
└───────────────────────────────────────────────┘
        │  JSON / cookie de sessão HttpOnly
        ▼
┌────────────────────── Spring Boot (API REST) ───────────────────────────────┐
│  Web Layer        @RestController (JSON) · WebhookController (Asaas)          │
│  ─────────────────────────────────────────────────────────────────────────  │
│  Service Layer    REGRA DE NEGÓCIO (Depote, inadimplência, desconto, caixa)   │
│  ─────────────────────────────────────────────────────────────────────────  │
│  Repository Layer Spring Data JPA (interfaces)                               │
│  ─────────────────────────────────────────────────────────────────────────  │
│  Domain           Entidades JPA + invariantes                               │
│  Integração       AsaasClient (HTTP) · PdfService · AuditService            │
└──────────────────────────────┬──────────────────────────────────────────────┘
                               ▼
                        PostgreSQL 16
                               ▲
                  Asaas (webhook de pagamento) ──┘
```

**Princípios inegociáveis:** (1) toda regra de negócio vive na **Service Layer** — controllers só serializam JSON, repositories só persistem, entidades guardam invariantes simples; (2) o front **nunca** contém regra de negócio financeira — ele consome a API e revalida no cliente apenas por UX; a fonte da verdade é o backend.

## 7. Estrutura de pastas

**Monorepo** com `backend/` (Spring) e `frontend/` (React). Backend organizado **por funcionalidade (package-by-feature)**:

```
saas-barbearia/
├─ backend/
│  ├─ src/main/java/com/barbearia/
│  │  ├─ BarbeariaApplication.java
│  │  ├─ common/        # config, segurança, auditoria, exceções, utils
│  │  │  ├─ security/   # SecurityConfig, UserDetailsService, perfis
│  │  │  ├─ audit/      # AuditLog, AuditService
│  │  │  └─ web/        # @ControllerAdvice, tratadores de erro
│  │  ├─ usuario/       # Usuario (auth): controller/service/repo/dto
│  │  ├─ cadastro/      # Empresa, Barbeiro, Servico, FormaPagamento
│  │  ├─ cliente/
│  │  ├─ agenda/        # Agendamento
│  │  ├─ comanda/       # Comanda, ItemComanda, Pagamento, Caixa
│  │  ├─ assinatura/    # Plano, Assinatura, Cobranca + AsaasClient
│  │  ├─ comissao/      # Depote, Ficha, cálculo, relatório PDF
│  │  └─ estoque/       # Produto, MovimentacaoEstoque
│  ├─ src/main/resources/
│  │  ├─ db/migration/  # Flyway V1__...sql
│  │  └─ application.yml
│  ├─ src/test/java/com/barbearia/   # espelha a estrutura acima
│  ├─ Dockerfile        # multi-stage (gera o jar)
│  └─ pom.xml
├─ frontend/
│  ├─ src/
│  │  ├─ main.tsx · App.tsx · routes.tsx
│  │  ├─ api/           # cliente Axios + hooks TanStack Query por recurso
│  │  ├─ components/    # UI compartilhada (shadcn/ui)
│  │  ├─ features/      # agenda/ comanda/ assinatura/ ... (telas por domínio)
│  │  ├─ lib/           # auth, formatadores, helpers
│  │  └─ types/         # tipos espelhando os DTOs da API
│  ├─ tests/            # Vitest/RTL · e2e Playwright
│  ├─ Dockerfile        # build Vite → estáticos servidos pelo Caddy
│  ├─ package.json · vite.config.ts · tailwind.config.ts
├─ compose.yaml         # backend + frontend + postgres (dev)
├─ .env.example
└─ SPEC.md · AMBIENTE.md · TEST_PLAN.md · SECURITY.md · DEPLOY.md · FUTURE.md
```

> Nota: o **repositório de código não deve ficar dentro do OneDrive/vault** (sync + `target/` causam conflito). O AMBIENTE.md define onde o repo vive; estes `.md` de planejamento ficam no vault para referência.

## 8. Modelos de dados (entidades do MVP)

- **Usuario** — `id, nome, email(único), senhaHash, perfil[ADMIN|RECEPCAO|PROFISSIONAL], ativo`. Pode referenciar um `Barbeiro`.
- **Barbeiro** — `id, nome, foto, ativo`; M:N `Servico` (habilitados); jornada e intervalos/folgas.
- **Servico** — `id, nome, preco, duracaoMin, disponivelOnline, inclusoAssinatura, fichas(int)`; M:N barbeiros autorizados.
- **Cliente** — `id, nome, telefone, email, dataNascimento, origem, criadoEm`.
- **Plano** — `id, nome, valor, cor, percentualDesconto, maxAssinantes, diasPermitidos, ativo`; M:N `Servico` inclusos.
- **Assinatura** — `id, cliente, plano, status[ATIVA|INADIMPLENTE|CANCELADA], dataInicio, dataCancelamento, asaasSubscriptionId`.
- **Cobranca** — `id, assinatura, valor, vencimento, status[PENDENTE|PAGA|ATRASADA], dataPagamento, asaasPaymentId`.
- **Agendamento** — `id, cliente, barbeiro, inicio, fim, status[AGENDADO|CONFIRMADO|CHEGOU|FINALIZADO|FALTOU|CANCELADO], origem[APP|RECEPCAO]`; M:N serviços.
- **Comanda** — `id, agendamento?, cliente, barbeiro, status[ABERTA|FECHADA], abertura, fechamento, totalBruto, totalDesconto, totalLiquido`.
- **ItemComanda** — `id, comanda, tipo[SERVICO|PRODUTO], referenciaId, descricao, quantidade, precoUnit, desconto, subtotal`.
- **Pagamento** — `id, comanda, forma[DINHEIRO|PIX|DEBITO|CREDITO|CARTAO_ASSINATURA], valor`.
- **Caixa** — `id, recepcionista, abertura, fechamento, valorAbertura, status[ABERTO|FECHADO]`. Pagamentos no intervalo do caixa compõem os totais.
- **Produto** — `id, nome, categoria, marca, preco, periodoRecompraDias, estoqueAtual, estoqueMinimo, ativo`.
- **MovimentacaoEstoque** — `id, produto, tipo[ENTRADA|SAIDA], quantidade, responsavel, data, comanda?`.
- **AuditLog** — `id, usuario, acao, entidade, entidadeId, dadosAntes(jsonb), dadosDepois(jsonb), timestamp`.
- **ComissaoBarbeiro** (calculada por período) — `barbeiro, periodo(mês), fichasAcumuladas, valorAssinatura, valorAvulso, valorProdutos, total`.
- **Configuracao/Empresa** — dados da barbearia (nome, logo, cor…) + `percentualComissaoAssinatura` (usado no Depote, ex.: 0,50).

Relacionamentos-chave: Cliente 1—N Assinatura 1—N Cobranca · Cliente/Barbeiro 1—N Agendamento 1—1 Comanda 1—N ItemComanda/Pagamento · Produto 1—N MovimentacaoEstoque.

## 9. Regras de negócio (núcleo)

1. **Bloqueio de inadimplente (AG07):** assinante com `Cobranca` ATRASADA não pode agendar serviço incluso na assinatura até regularizar.
2. **Desconto automático (CX02):** itens extras (serviços/produtos) de um assinante recebem o `percentualDesconto` do plano automaticamente na comanda.
3. **Edição de comanda fechada (CX06):** permitida **somente ao ADMIN** e **sempre** gera `AuditLog` (o quê, quem, quando, antes/depois). Recálculo de totais obrigatório.
4. **Totais de caixa (CX08):** ao consultar/fechar o caixa, exibir total por forma de pagamento e total geral, calculados — nunca soma manual.
5. **Depote (AS09) — distribuição da comissão de assinatura:** cada `Servico` tem um peso em `fichas` configurável (ex.: corte = 40, barba = 30, pezinho = 20). Ao longo do mês cada barbeiro acumula fichas pelos serviços realizados **em clientes assinantes**. `total de fichas` = soma das fichas de todos os barbeiros. `valor do pote (R$)` = `percentualComissaoAssinatura × receita de assinatura do mês`. Comissão do barbeiro = `(fichas do barbeiro / total de fichas) × valor do pote`. *Ex.:* pote R$ 1.500, barbeiro A com 26% das fichas → R$ 390. **Regra mais sensível do sistema — cobertura de testes exaustiva (arredondamento, soma das partes = pote, zero fichas; ver TEST_PLAN).** A comissão de avulsos e produtos é regra separada (ainda a definir — ver REQUISITOS §9).
6. **Limite de vagas (AS07):** uma `Assinatura` ATIVA não pode ser criada se o plano atingiu `maxAssinantes`.
7. **Estoque (ES02/ES03):** fechar comanda com produto gera `MovimentacaoEstoque` de SAÍDA e decrementa `estoqueAtual`; abaixo de `estoqueMinimo` gera alerta no dashboard.
8. **Frequência média do assinante (alerta):** monitorar visitas/mês; sinalizar quando se aproxima de 3 (limite de lucratividade do modelo).

## 10. Fluxos principais do usuário

- **Autoagendamento (cliente):** página pública → cliente escolhe serviço, barbeiro e horário livre → informa nome/telefone → agendamento criado como **pendente** → recepção/cliente confirma. O barbeiro vê o novo horário na própria agenda.
- **Atendimento (recepção):** abrir agenda → criar/confirmar agendamento → marcar "chegou" (notifica barbeiro) → abrir comanda → adicionar serviços/produtos (desconto do plano aplicado) → fechar comanda (forma de pagamento) → baixa no caixa. Meta NF06: ≤ 3 cliques da tela principal.
- **Fechamento de caixa (recepção):** abrir caixa no turno → ao final, ver totais por forma + total geral → fechar.
- **Ciclo de assinatura:** admin cria plano → cliente assina (cria `Assinatura` + cobrança recorrente no Asaas) → webhook do Asaas atualiza `Cobranca`/status → inadimplência dispara bloqueio e entra na recobrança.
- **Comissão (admin/barbeiro):** ao longo do mês o sistema acumula fichas → admin/barbeiro consultam projeção/realizado → fim do mês gera relatório PDF por barbeiro (AS10).

## 11. Estratégia de autenticação e autorização

Spring Security com **sessão por cookie HttpOnly** (`SameSite=Lax`, `Secure` em produção), senhas em **BCrypt**. SPA e API no **mesmo domínio** (via Caddy), evitando CORS e mantendo o token de sessão **inacessível ao JavaScript** — mais seguro que JWT em `localStorage`. **CSRF habilitado** (padrão cookie-to-header: o front lê o cookie `XSRF-TOKEN` e reenvia no header). Endpoints: `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/me`. Três `ROLE`s (ADMIN, RECEPCAO, PROFISSIONAL) com autorização nos endpoints **e** `@PreAuthorize` em Services para ações críticas; o front apenas oculta/mostra a UI conforme o perfil (a checagem real é sempre no backend). Menor privilégio: barbeiro só vê a própria produção/comissão. A **página de autoagendamento do cliente é pública** (sem login de staff): cria agendamento pendente, com validação e *rate limiting* contra abuso. Conta de cliente é opcional e fica como evolução futura.

## 12. Estratégia de persistência

JPA/Hibernate sobre PostgreSQL; schema versionado por **Flyway** (`ddl-auto: validate` — nunca `update`/`create` em produção). Valores monetários em `BigDecimal` (`NUMERIC`), nunca `double`. Operações de comanda/caixa/cobrança são **transacionais** (`@Transactional`). `AuditLog.dados*` em `jsonb`.

## 13. Estratégia de validação

Validação em duas camadas: **Jakarta Bean Validation** nos DTOs de entrada (`@NotNull`, `@Positive`, etc.) e **invariantes de negócio na Service Layer** (ex.: vagas do plano, inadimplência, recálculo de totais). Erros tratados por `@ControllerAdvice` com mensagens claras na view.

## 14. Estratégia de testes (resumo — detalhe no TEST_PLAN.md)

TDD onde o risco é maior. **Backend** — Unidade (JUnit5/AssertJ/Mockito) para Services, com foco obsessivo no **Depote** e nas regras de inadimplência/desconto/totais; Web (MockMvc) para controllers e proteção de endpoints por perfil; Integração (Testcontainers + Postgres real) para repositórios e fluxos transacionais. **Frontend** — Vitest + React Testing Library para componentes/hooks; Playwright para fluxos de usuário ponta a ponta (login → agendar → fechar comanda) contra a API real em ambiente de teste. A implementação busca **fazer os testes passarem**, não gerar código solto.

## 15. Estratégia de deploy (resumo — detalhe no DEPLOY.md)

**VPS simples** + **Docker Compose** com três serviços: backend (jar via imagem multi-stage), frontend (build Vite servido como estáticos) e PostgreSQL (volume persistente). **Caddy** como proxy reverso com **HTTPS automático**, servindo o SPA em `/` e roteando `/api/*` ao backend (mesmo domínio). Variáveis por `.env`/profile Spring. **Backup diário** via `pg_dump`, retenção ≥ 30 dias (NF05). Deploy só depois de a base passar nos testes.

## 16. Pontos de segurança (resumo — detalhe no SECURITY.md)

Segredos só em variáveis de ambiente (nunca no git); BCrypt; CSRF; validação de entrada; `@PreAuthorize`; `AuditLog` em ações críticas (NF03); sem dados sensíveis em log; OWASP Dependency-Check no CI; assinatura de webhook do Asaas verificada; LGPD (coleta mínima, base legal, futura gestão de consentimento).

## 17. Poss�