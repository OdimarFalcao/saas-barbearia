# Documento de Requisitos — MVP (Sistema de Gestão de Barbearia)

> Versão 1.0 (MVP) · junho/2026
> Refinamento do `Requisitos_Sistema_Barbearia (1).docx` (entrevista de campo, out/2025) **com base nas decisões tomadas no planejamento**.
> Documento técnico complementar: `SPEC.md`.

---

## 1. Visão e contexto

Sistema de gestão para uma **barbearia cliente** que hoje usa um **site web apenas para agendamento** de clientes e serviços — **sem nenhuma gestão de pagamentos, assinatura, comissão ou relatórios**. O sistema preenche exatamente essa lacuna: a **camada de dinheiro e informação** do negócio.

**Problema que resolve.** O cliente consegue agendar, mas não controla recebimentos, nao tem sistema de assinanetes , distribuição de comissão (Depote) nem desempenho — isso hoje vive fora do sistema ou simplesmente não existe. A diferenciação é **entregar bem essa camada financeira e de relatórios**.

**Público-alvo.** Barbearias que desejem um sistema de gerenciamento, barbearias que utilizam clube de assinatura e barbeiros parceiros por comissão. O produto é **padrao e configurável**, pensado para ser **reaproveitado em vários clientes** (não amarrado aos dados de nenhum deles — nada será importado do sistema atual do piloto). A primeira barbearia (local, do desenvolvedor) é o **piloto** que valida a solução.

## 2. Decisões fixadas no planejamento

| Tema | Decisão | Motivo |
|---|---|---|
| Escopo da v1 | **MVP enxuto** | Primeiro SaaS, dupla de devs; validar rápido com qualidade. |
| Produto | **Padrao e configurável** | Reaproveitável em várias barbearias; nada hardcoded para o piloto. |
| Inquilinos | **Single-tenant por instância** | Cada cliente roda a própria instância. Multi-tenant verdadeiro é evolução futura. |
| Validação | **Cliente piloto real** | Barbearia local que já agenda online, mas não gere pagamentos, comissão nem relatórios. |
| Backend | **Java 21 + Spring Boot (API REST)** | Tipagem forte para regra financeira, ecossistema maduro. |
| Frontend | **React + TypeScript + Vite + Tailwind + shadcn/ui** | Front moderno e desacoplado; Java só no backend. |
| Banco | **PostgreSQL 16** | Transacional, adequado a dinheiro. |
| Pagamentos | **Asaas** (gateway único) | Brasileiro, recorrência nativa, taxas baixas. |
| Autenticação | **Sessão por cookie HttpOnly + CSRF** | Mais seguro que JWT em `localStorage`; SPA e API no mesmo domínio. |
| Processo | **Spec-first + TDD + entrega incremental** | Disciplina de engenharia; ver `CLAUDE.md`. |

## 3. Perfis de usuário

- **Administrador / Gestor** — acesso total; foco em dinheiro, planos, comissão e configurações. Usa desktop.
- **Recepção** — operação do dia: agenda, comanda e caixa. Usa desktop.
- **Profissional (Barbeiro)** — vê a própria agenda, produção e comissão. Usa celular.
- **Cliente (usuário final)** — agenda o próprio horário pela página pública de autoagendamento e acompanha/confirma seu agendamento. Usa celular.

## 4. Escopo do MVP — requisitos funcionais incluídos

IDs herdados do documento original de requisitos.

### 4.1 Cadastros e configuração
- **CF01** Cadastro de barbeiros (dados, serviços habilitados, jornada e intervalos).
- **CF02** Catálogo de serviços (nome, preço, duração, disponível online, incluso na assinatura, profissionais autorizados, **valor em fichas**).
- **CF03** Dados da empresa (nome, logo, cor primária, textos do painel).
- **CF04** Controle de acesso por perfil (Admin, Recepção, Profissional).
- **CF06** Formas de pagamento aceitas.

### 4.2 Clientes
- **CL01** Cadastro de clientes com campo de **origem** (Instagram, indicação, Google, fachada…).

### 4.3 Agenda e agendamentos
> ✅ **Confirmado:** o produto terá **agenda própria** no MVP, com **autoagendamento pelo cliente** (página pública) e o **barbeiro visualizando a própria agenda**.

- **AG01** Visualização da agenda por barbeiro (diária/semanal).
- **AG02** Códigos visuais essenciais (assinante, avulso, cliente novo).
- **AG03** Confirmação de agendamento (link ao cliente ou manual pela recepção) com ícone de status.
- **AG04** Notificação de chegada do cliente ao barbeiro responsável.
- **AG05** Ações sobre o agendamento: confirmar, finalizar, excluir, registrar falta.
- **AG06** Intervalos e folgas individuais por barbeiro como bloqueios na agenda.
- **AG07** **Bloqueio de assinante inadimplente** até regularização.
- **AG08** Painel resumido do dia (agendamentos, assinantes, avulsos, comandas abertas).
- **AG09** *(NOVO)* **Autoagendamento pelo cliente** — página pública onde o cliente escolhe serviço, barbeiro e horário disponível e cria o agendamento (entra como pendente para confirmação).

### 4.4 Comanda e caixa
- **CX01** Abrir comanda a partir de agendamento, adicionando serviços avulsos.
- **CX02** Desconto automático do plano em serviços extras e produtos para assinantes.
- **CX03** Formas de pagamento: dinheiro, PIX, débito, crédito, cartão de assinatura.
- **CX04** Fechar comanda com resumo de serviços, descontos e valor final.
- **CX05** Histórico de comandas com filtros (data, barbeiro, status).
- **CX06** *(MELHORIA)* **Editar comanda fechada** — somente Admin, **com log de auditoria completo**.
- **CX07** Caixa por turno (abrir/fechar com histórico de movimentações).
- **CX08** *(MELHORIA)* **Totais agregados no caixa** (por forma de pagamento e total geral).
- **CX09** Alerta de comanda aberta há mais de 1 dia.

### 4.5 Assinatura (módulo crítico)
- **AS01** Cadastro de planos (nome, valor, cor, % de desconto, serviços inclusos, máximo de assinantes, dias permitidos).
- **AS03** Dashboard de assinatura (total, em dia, inadimplentes, % de inadimplência).
- **AS04** Gestão de inadimplência (listar, recobrança individual ou em massa, limite de tentativas).
- **AS07** Limite de vagas por plano.
- *(Integração)* Cobrança recorrente via **Asaas** + recepção de **webhooks** de pagamento.

### 4.6 Comissão / Depote
- **AS09** **Sistema Depote** — comissão de assinatura por fichas (ver Regra RN-05).
- **AS10** Relatório de comissão por barbeiro em **PDF** (avulso + assinatura + produtos + total).

### 4.7 Estoque (básico)
- **ES01** Catálogo de produtos (nome, categoria, marca, preço, período de recompra, status).
- **ES02** Saída automática de estoque ao fechar comanda com produto.
- **ES03** Estoque mínimo configurável com alerta no dashboard.

### 4.8 Dashboard operacional (enxuto)
- **DB01** Visão do dia: profissionais ativos, comandas abertas, estoque mínimo, pagamentos incompletos. (Subconjunto do DB01 original.)

## 5. Fora do escopo do MVP (→ `FUTURE.md`)

Módulo Pump (PU01–07), mapa de calor (DB03), dashboard estratégico (DB02), relatórios/analytics avançados e exportações (RE01–08, exceto o PDF de comissão), marketing/clube/cupons/banners (MK01–05), cashback, presentes (CL06), avaliações (CL07), aniversariantes/top/inativos/recompra como features dedicadas (CL02–05), alteração de plano em massa (AS05), bloqueio pós-cancelamento e lista de espera (AS06), contrato com aviso prévio (AS11), fábrica de planos (AS12), **múltiplos gateways (AS02)**, **multi-unidade (CF05)**, financeiro completo — contas a pagar/bancárias/categorias/conciliação (FI02–05). A maior evolução futura é a **migração para multi-tenant**.

## 6. Requisitos não funcionais

- **NF01 Desempenho** — páginas/relatórios em < 3 s para base de até 5.000 clientes.
- **NF02 Responsividade** — desktop (recepção/admin) e celular (barbeiros).
- **NF03 Segurança** — autenticação com perfis e **logs de auditoria** de ações críticas (editar comanda, alterar assinatura, mudar permissão).
- **NF04 Disponibilidade** — alvo 99,5%; manutenções fora do pico.
- **NF05 Backup** — automático diário, retenção ≥ 30 dias.
- **NF06 Usabilidade** — agendar e fechar comanda em ≤ 3 cliques a partir da tela principal.
- **NF07 Escalabilidade** — base preparada para crescer (multi-tenant é evolução futura).

## 7. Regras de negócio

- **RN-01 Bloqueio de inadimplente** — assinante com cobrança ATRASADA não agenda serviço de assinatura até regularizar.
- **RN-02 Desconto automático** — itens extras de assinante recebem o % de desconto do plano na comanda.
- **RN-03 Edição de comanda fechada** — só Admin; sempre gera log de auditoria (o quê, quem, quando, antes/depois) e recalcula totais.
- **RN-04 Totais de caixa** — totais por forma de pagamento e total geral sempre calculados pelo sistema.
- **RN-05 Depote (distribuição da comissão de assinatura):**
  1. Cada serviço tem um **peso em fichas** configurável (ex.: corte = 40, barba = 30, pezinho = 20) — campo `fichas` no Serviço.
  2. Durante o mês, cada barbeiro acumula fichas pelos serviços realizados **em clientes assinantes**.
  3. `total de fichas` do mês = soma das fichas de todos os barbeiros.
  4. O gestor define o **percentual de comissão** sobre a **receita de assinatura** do mês (ex.: 50%) — configuração da empresa.
  5. `valor do pote (R$)` = percentual × receita de assinatura do mês.
  6. Comissão de cada barbeiro = `(fichas do barbeiro ÷ total de fichas) × valor do pote`.
  - *Exemplo:* pote de R$ 1.500; barbeiro A com 26% das fichas → recebe R$ 390.
  - **Regra mais sensível do sistema — cobertura de testes exaustiva (arredondamento, soma das partes = pote, caso de zero fichas).**
- **RN-06 Limite de vagas** — não criar assinatura ativa se o plano atingiu `maxAssinantes`.
- **RN-07 Estoque** — fechar comanda com produto gera saída e decrementa estoque; abaixo do mínimo, alerta.
- **RN-08 Frequência média** — monitorar visitas/mês do assinante; alertar ao se aproximar de 3 (limite de lucratividade).

## 8. Glossário

| Termo | Definição |
|---|---|
| Assinante / Clube | Cliente com plano mensal recorrente (cortes inclusos + descontos). |
| Avulso | Cliente sem plano; paga por serviço. |
| Comanda | Registro dos serviços de um atendimento, encerrado com pagamento. |
| Depote | Distribuição da comissão de assinatura entre barbeiros por fichas proporcionais aos serviços. |
| Ficha | Peso configurável de cada serviço no Depote (ex.: corte = 40, barba = 30, pezinho = 20). |
| Pote | Valor total das mensalidades de assinatura a distribuir como comissão no mês. |
| Recompra | Alerta de fim do ciclo de vida de um produto comprado pelo cliente. |

## 9. Pendências / decisões em aberto

- **[RESOLVIDO] Unidade da ficha (RN-05):** ficha é **peso configurável por serviço** (ex.: corte = 40, barba = 30, pezinho = 20). Definido.
- **[RESOLVIDO] Tratamento da agenda:** agenda **própria** no MVP, com autoagendamento do cliente (página pública) e o barbeiro vendo a própria agenda.
- **[ABERTO] Comissão de avulso e produtos:** o Depote cobre só a assinatura. Falta definir como se calcula a comissão de serviços avulsos e venda de produtos (ex.: % fixo por barbeiro) que compõe o relatório AS10.
- **[A confirmar] Autoagendamento do cliente:** o cliente agenda só com nome/telefone (sem conta) ou cria uma conta simples? E confirmação por link/WhatsApp?
- **[A confirmar] Notificação de chegada (AG04):** push real ou atualização na tela do barbeiro.
