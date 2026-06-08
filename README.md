# Barber SaaS

Sistema web para gestão de barbearias com **clube de assinatura**, **controle financeiro** e **distribuição de comissão entre barbeiros (Depote)**. Construído para substituir planilhas e sistemas que não controlam pagamentos, recorrência nem relatórios.

## Sobre o projeto

Barbearias que usam sistemas de agendamento online frequentemente não têm controle de pagamentos, assinaturas, comissões nem relatórios — essa lacuna é o que o Barber SaaS resolve.

O sistema entrega:
- Agenda própria com autoagendamento pelo cliente (página pública)
- Comanda e caixa por turno com totais calculados automaticamente
- Clube de assinatura com cobrança recorrente e gestão de inadimplência
- Distribuição de comissão por fichas entre barbeiros (Depote)
- Controle de estoque básico e dashboard operacional

Produto genérico e configurável — cada barbearia roda sua própria instância (single-tenant).

## Tecnologias

**Backend**
- Java 21
- Spring Boot 4
- Spring Security (sessão por cookie HttpOnly + CSRF)
- Spring Data JPA + Flyway
- PostgreSQL 16
- Gradle (Kotlin DSL)
- springdoc-openapi (Swagger UI)

**Frontend**
- React 18 + TypeScript
- Vite + Tailwind CSS v4 + shadcn/ui
- TanStack Query + React Router
- React Hook Form + Zod
- Axios

**Pagamentos**
- Asaas (recorrência + webhooks)

**Infraestrutura**
- Docker + Docker Compose
- Caddy (proxy reverso + HTTPS automático em produção)

## Funcionalidades

- Cadastro de barbeiros, serviços, clientes e formas de pagamento
- Agenda por barbeiro com bloqueio de inadimplente
- Autoagendamento pelo cliente via página pública
- Comanda com desconto automático para assinantes
- Edição de comanda fechada com log de auditoria (somente Admin)
- Caixa por turno com totais por forma de pagamento
- Planos de assinatura com cobrança recorrente (Asaas)
- Dashboard de inadimplência e recobrança
- Sistema Depote: distribuição de comissão por fichas entre barbeiros
- Relatório de comissão por barbeiro em PDF
- Controle de estoque com alertas de mínimo
- Controle de acesso por perfil: Admin, Recepção, Profissional

## Pré-requisitos

- Docker (com integração WSL2 ligada no Docker Desktop)
- Java 21
- Node.js LTS (v20+)
- Git

## Como executar

### 1. Clone o repositório

```bash
git clone git@github.com:OdimarFalcao/saas-barbearia.git
cd saas-barbearia
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
# edite o .env com seus valores reais
```

### 3. Suba o banco de dados

```bash
docker compose --env-file .env up -d
```

### 4. Backend

```bash
cd backend
./gradlew bootRun --no-daemon
```

API disponível em `http://localhost:8080`
Swagger UI em `http://localhost:8080/swagger-ui.html`

### 5. Frontend

```bash
cd frontend
npm install
npm run dev
```

App disponível em `http://localhost:5173`

## Variáveis de ambiente

Crie o `.env` a partir do `.env.example`:

```dotenv
# Banco
POSTGRES_DB=barbearia
POSTGRES_USER=barbearia
POSTGRES_PASSWORD=sua-senha-local
DATABASE_URL=jdbc:postgresql://localhost:5433/barbearia

# Backend
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080

# Asaas (sandbox)
ASAAS_API_KEY=sua-chave-sandbox
ASAAS_BASE_URL=https://sandbox.asaas.com/api/v3
```

> O arquivo `.env` nunca é versionado. Segredos ficam apenas no ambiente local e, em produção, nas variáveis do servidor.

## Testes

```bash
# Backend
cd backend
./gradlew test --no-daemon

# Frontend
cd frontend
npm test
```

## Estrutura de pastas

```
saas-barbearia/
├── backend/                    # Spring Boot (package-by-feature)
│   └── src/main/java/com/barbearia/
│       ├── common/             # segurança, auditoria, tratamento de erros
│       ├── usuario/            # autenticação e perfis
│       ├── cadastro/           # barbeiros, serviços, formas de pagamento
│       ├── cliente/
│       ├── agenda/
│       ├── comanda/            # comanda, caixa, pagamentos
│       ├── assinatura/         # planos, cobrança, Asaas
│       ├── comissao/           # Depote, fichas, PDF
│       └── estoque/
├── frontend/                   # React SPA
│   └── src/
│       ├── api/                # hooks TanStack Query por recurso
│       ├── components/         # UI compartilhada (shadcn/ui)
│       ├── features/           # telas por domínio
│       ├── lib/                # helpers, formatadores
│       └── types/              # tipos espelhando os DTOs da API
├── docs/                       # SPEC, REQUISITOS, AMBIENTE, TEST_PLAN...
├── compose.yaml
├── .env.example
└── CLAUDE.md                   # manual de operação do Claude Code
```

## Status

**MVP em desenvolvimento** — scaffolding concluído, implementação em andamento.

| Etapa | Status |
|---|---|
| Diagnóstico e documentação | Concluído |
| Especificação técnica (SPEC.md) | Concluído |
| Configuração de ambiente | Concluído |
| Scaffolding do monorepo | Concluído |
| TEST_PLAN.md e implementação incremental | Em andamento |

## Autor

Desenvolvido por **Odimar Falcão**.
