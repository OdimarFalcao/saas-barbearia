# TEST_PLAN.md — Plano de Testes (MVP)

> Versão 1.0 · junho/2026 · Etapa 5 do planejamento.
> Base: `REQUISITOS.md` (regras RN-01 a RN-08) e `SPEC.md` §14.
> Documento vivo: cada feature nova atualiza a seção correspondente **antes** da implementação (TDD).

---

## 1. Objetivo e princípios

1. **TDD onde o risco é maior.** Testes escritos antes (ou junto) da implementação; a implementação existe para fazer os testes passarem.
2. **Pirâmide de testes:** muitos testes de unidade (Service), alguns de web/integração, poucos e2e. O e2e valida fluxo, não regra.
3. **Regra de negócio se testa na Service Layer**, sem subir contexto Spring quando possível (JUnit puro + Mockito).
4. **Dinheiro é o centro do risco.** Depote (RN-05), desconto (RN-02), totais de caixa (RN-04) e inadimplência (RN-01) têm cobertura exaustiva, incluindo bordas e arredondamento.
5. **Nenhum merge sem testes passando** (Definition of Done, `CLAUDE.md` §10).

## 2. Camadas de teste e ferramentas

### Backend

| Camada | Ferramentas | O que cobre | Banco |
|---|---|---|---|
| **Unidade** | JUnit 5 + AssertJ + Mockito | Services (toda regra de negócio), cálculo do Depote, validadores | Nenhum (mocks) |
| **Web** | MockMvc (`spring-boot-starter-webmvc-test`) + Spring Security Test | Controllers: contrato JSON, validação de DTO, status HTTP, autorização por perfil | Nenhum (Service mockado) |
| **Integração** | Spring Boot Test + **Testcontainers (PostgreSQL 16)** | Repositórios, migrations Flyway, fluxos transacionais (comanda→caixa, webhook→cobrança), `@PreAuthorize` | Postgres real efêmero |

- **Testcontainers é o padrão** para integração: container Postgres 16 efêmero por execução, migrations Flyway aplicadas do zero. Reproduzível local e no CI, sem depender do compose.
- O banco local `barbearia_test` (porta 5433, profile `test`) fica reservado para **execução manual da API em modo teste** e para o e2e do Playwright — não para os testes de integração JUnit.

### Frontend

| Camada | Ferramentas | O que cobre |
|---|---|---|
| **Unidade/componente** | Vitest + React Testing Library | Componentes, hooks, formatadores (ex.: dinheiro), validação Zod |
| **E2E** | Playwright | Fluxos críticos ponta a ponta contra a API real em profile `test` |

> O frontend **não testa regra financeira** — valida UX (formulário, exibição, navegação). A fonte da verdade é o backend.

### Tooling pendente (adicionar no primeiro teste de cada tipo)

- `backend/build.gradle.kts`: `org.testcontainers:postgresql` + `org.springframework.boot:spring-boot-testcontainers` (ou equivalente no Boot 4) quando o primeiro teste de integração for escrito.
- `frontend/package.json`: `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `jsdom` no primeiro teste de componente; `@playwright/test` no primeiro e2e. Adicionar script `"test": "vitest"`.

## 3. Convenções

- **Estrutura espelhada:** `src/test/java/com/barbearia/<feature>/` espelha `src/main/java/com/barbearia/<feature>/`.
- **Nomes:** classe `XxxServiceTest` (unidade), `XxxControllerTest` (web), `XxxRepositoryIT` / `XxxFluxoIT` (integração). Métodos descritivos em português: `deveBloquearAgendamentoDeAssinanteInadimplente()`.
- **Dados de teste:** builders/fábricas simples por feature (ex.: `ComandaTestFactory`) — sem fixtures globais mágicas.
- **Dinheiro:** asserts de `BigDecimal` sempre com `isEqualByComparingTo` (nunca `equals`, que compara escala).
- **Um comportamento por teste.** Casos de erro têm o mesmo peso que o caminho feliz.

## 4. Mapa de cobertura por área de risco

Prioridade: 🔴 exaustiva · 🟡 completa · 🟢 essencial.

### 4.1 🔴 Depote / comissão de assinatura (RN-05, AS09)

A regra mais sensível do sistema — dinheiro entre parceiros. Testes de unidade puros sobre o serviço de cálculo (`comissao/`), sem Spring.

**Casos obrigatórios:**

| # | Caso | Verificação |
|---|---|---|
| D01 | Exemplo da spec | Pote R$ 1.500, barbeiro com 26% das fichas → R$ 390,00 exato |
| D02 | **Soma das partes = pote** | Para qualquer distribuição de fichas, a soma das comissões é exatamente o valor do pote (centavo a centavo) |
| D03 | Dízima periódica | 3 barbeiros com fichas iguais, pote R$ 100,00 (1/3 cada) → soma ainda fecha em R$ 100,00 |
| D04 | Zero fichas no mês | Nenhum serviço em assinante → nenhuma comissão, sem divisão por zero, pote não distribuído sinalizado |
| D05 | Barbeiro com zero fichas | Recebe R$ 0,00 e aparece (ou não) no relatório conforme regra; demais recebem o total |
| D06 | Um único barbeiro | Recebe 100% do pote |
| D07 | Receita de assinatura zero | Pote R$ 0,00 → todas as comissões R$ 0,00 |
| D08 | Percentual de comissão 0% e 100% | Bordas da configuração `percentualComissaoAssinatura` |
| D09 | Só serviços em **assinantes** contam | Serviço idêntico em cliente avulso não gera ficha |
| D10 | Competência mensal | Serviço no dia 1º e no último dia entram; mês anterior/seguinte não |
| D11 | Pesos de ficha distintos | Corte=40, barba=30, pezinho=20 acumulados corretamente por barbeiro |
| D12 | Valores quebrados | Pote com centavos (ex.: R$ 1.333,33) → distribuição fecha sem perder/criar centavo |

**Decisão de arredondamento (fixar na SPEC antes de implementar):** proposta — comissão de cada barbeiro truncada/arredondada a 2 casas e a **diferença residual distribuída por maior resto** (ordem determinística, ex.: maior fração e desempate por id), garantindo D02/D03/D12. Registrar a regra escolhida em `SPEC.md` §9.5 quando a feature entrar em desenvolvimento.

### 4.2 🔴 Assinatura, cobrança e webhooks Asaas (AS01–AS07, RN-01, RN-06)

- **W01** Webhook de pagamento confirmado atualiza `Cobranca` para PAGA e reativa assinatura inadimplente.
- **W02** **Idempotência:** o mesmo evento entregue 2+ vezes não duplica efeito (mesmo `asaasPaymentId`).
- **W03** Webhook com assinatura/token inválido → rejeitado (401/403), nada persistido.
- **W04** Evento de cobrança vencida marca `Cobranca` ATRASADA e `Assinatura` INADIMPLENTE.
- **W05** Evento desconhecido/fora de ordem não corrompe estado (ex.: pagamento de cobrança já paga).
- **A01 (RN-06)** Criar assinatura com plano lotado (`maxAssinantes`) → erro de negócio claro; com vaga → sucesso; cancelamento libera vaga.
- **A02** Condição de corrida na última vaga (integração): duas criações simultâneas → só uma vence.
- **B01 (RN-01)** Assinante com cobrança ATRASADA não agenda serviço incluso na assinatura; pode voltar a agendar após regularizar; avulso nunca é bloqueado por inadimplência.

### 4.3 🔴 Comanda e desconto (CX01–CX06, RN-02, RN-03)

- **C01 (RN-02)** Item extra de assinante recebe o `percentualDesconto` do plano automaticamente; avulso não recebe.
- **C02** Serviço **incluso** na assinatura entra com valor coberto pelo plano (sem cobrança dupla).
- **C03** Totais da comanda: `totalBruto`, `totalDesconto`, `totalLiquido` recalculados a cada item; soma de pagamentos deve cobrir o líquido para fechar.
- **C04** Fechar comanda exige forma(s) de pagamento; comanda fechada não aceita itens por fluxo normal.
- **C05 (RN-03)** Editar comanda fechada: permitido só a ADMIN; RECEPCAO/PROFISSIONAL → 403. **Sempre** gera `AuditLog` com antes/depois (jsonb) e recalcula totais. Teste de integração verifica o registro persistido.
- **C06** Edição que altera item com produto ajusta o estoque coerentemente (ou regra definida na SPEC no momento da feature).

### 4.4 🔴 Caixa (CX07, CX08, RN-04)

- **X01** Totais por forma de pagamento calculados pelo sistema: comandas em dinheiro/PIX/débito/crédito/cartão-assinatura no turno → cada total e o geral conferem.
- **X02** Pagamento fora do intervalo do caixa não entra no total do turno.
- **X03** Caixa fechado não aceita novos pagamentos; reabertura (se permitida) é auditada.
- **X04** Comanda com pagamento misto (ex.: parte PIX, parte dinheiro) soma em cada forma corretamente.

### 4.5 🟡 Agenda e autoagendamento (AG01–AG09)

- **G01** Conflito de horário: não agendar sobre horário ocupado, intervalo ou folga do barbeiro (AG06).
- **G02** Transições de status válidas (AGENDADO→CONFIRMADO→CHEGOU→FINALIZADO; FALTOU; CANCELADO) e inválidas rejeitadas.
- **G03** Autoagendamento público (AG09): cria agendamento **pendente** com serviço disponível online + barbeiro habilitado + horário livre; entrada inválida → 400; rate limiting ativo no endpoint público.
- **G04** Barbeiro (PROFISSIONAL) só enxerga a própria agenda.

### 4.6 🟡 Estoque (ES01–ES03, RN-07)

- **E01** Fechar comanda com produto gera `MovimentacaoEstoque` SAÍDA e decrementa `estoqueAtual` (transação única com o fechamento).
- **E02** Estoque abaixo do mínimo após a saída → alerta no dashboard.
- **E03** Venda com estoque insuficiente → regra definida na SPEC (bloquear ou permitir negativo com alerta) testada conforme decisão.

### 4.7 🟡 Segurança e acesso (CF04, NF03, §11 da SPEC)

- **S01** Endpoint protegido sem sessão → 401; com perfil errado → 403 (matriz perfil × endpoint nos testes MockMvc de cada controller).
- **S02** Login/logout/`me`: sucesso, senha errada, usuário inativo.
- **S03** CSRF: mutação sem token → 403; com token → ok.
- **S04** PROFISSIONAL não acessa produção/comissão de outro barbeiro (menor privilégio).
- **S05** Ações críticas (RN-03, alterar assinatura, mudar permissão) geram `AuditLog` — verificado nos testes da feature correspondente.

### 4.8 🟢 Cadastros e dashboard (CF01–CF06, CL01, DB01)

- CRUDs: validação de DTO (campos obrigatórios, preço positivo, fichas ≥ 0), unicidade (email de usuário), soft-delete/inativação onde houver.
- Dashboard do dia (AG08/DB01): agregações batem com os dados inseridos no teste de integração.

## 5. Testes e2e (Playwright) — fluxos mínimos

Rodam contra backend em profile `test` (banco `barbearia_test` resetado) + frontend buildado. São poucos e estáveis:

1. **Login → agenda → criar agendamento → confirmar.**
2. **Atendimento completo:** marcar chegada → abrir comanda → adicionar serviço extra (assinante: desconto aparece) → fechar comanda → conferir no caixa.
3. **Fechamento de caixa:** abrir caixa → registrar pagamentos → fechar → totais por forma exibidos conferem.
4. **Autoagendamento público:** cliente agenda sem login → aparece pendente na recepção.

## 6. Execução e CI

```bash
# Backend — unidade + web + integração (Testcontainers exige Docker ativo)
cd backend && ./gradlew test --no-daemon

# Frontend — unidade/componente
cd frontend && npm test

# E2E (exige API em profile test + frontend)
cd frontend && npx playwright test
```

**CI (GitHub Actions, a configurar na etapa de CI/CD):** em todo push/PR → build + `gradlew test` + `npm test` (+ lint). E2e em PR para `main`. Merge bloqueado com teste vermelho.

## 7. Metas de cobertura

- **Sem meta numérica global** (cobertura alta artificial é teatro). Metas qualitativas:
  - Services com regra de negócio: **todos os casos das tabelas acima** implementados.
  - `comissao/` (Depote): **100% de branches** do serviço de cálculo.
  - Todo bug encontrado em produção/piloto ganha teste de regressão antes do fix.

## 8. Rastreabilidade

Cada caso (D01…, W01…, C01…) é referenciado no nome ou no `@DisplayName` do teste correspondente, permitindo auditar a cobertura contra este plano. Ao implementar uma feature, marcar os casos cobertos aqui com ✅.
