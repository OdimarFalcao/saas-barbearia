# AMBIENTE.md — Ambiente de desenvolvimento isolado

> Etapa 3 da metodologia. Objetivo: um ambiente **isolado, reprodutível e fora do OneDrive**, pronto para versionar no GitHub.
> Decisão: **WSL2 (Ubuntu 24.04)** + VSCode via Remote-WSL. Motivo: ambiente Linux limpo, igual ao de produção (VPS), sem o conflito de sincronização do OneDrive com `target/`/`node_modules`.

---

## 0. Por que NÃO desenvolver no OneDrive
O projeto de planejamento hoje está em `OneDrive/Documentos/programacao/saas-barbearia/` (ótimo para os `.md`). Mas o **repositório de código** vai para o WSL (`~/projects/saas-barbearia`). O OneDrive tenta sincronizar milhares de arquivos de build em tempo real → lentidão, locks e corrupção. Os docs serão **copiados** para o repo (que passa a ser a fonte da verdade); o vault Obsidian guarda a cópia de referência.

## 1. Pré-requisitos no Windows
1. **WSL2 + Ubuntu** (no PowerShell como admin):
   ```powershell
   wsl --install -d Ubuntu-24.04
   ```
   Reinicie se pedir, crie usuário/senha do Linux.
2. **VSCode** + extensão **WSL** (`ms-vscode-remote.remote-wsl`). Recomendadas: *Extension Pack for Java*, *Spring Boot Tools*, *ESLint*, *Prettier*, *Tailwind CSS IntelliSense*.
3. **Docker Desktop** com integração WSL2 ligada (Settings → Resources → WSL Integration → habilitar Ubuntu-24.04).

> A partir daqui, **tudo roda dentro do Ubuntu/WSL** (terminal do Ubuntu).

## 2. Ferramentas no Ubuntu (WSL)
```bash
sudo apt update && sudo apt install -y curl git unzip zip build-essential

# Java 21 + Maven via SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.4-tem      # Temurin 21 LTS
sdk install maven

# Node LTS via nvm
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash
source ~/.bashrc
nvm install --lts && nvm use --lts

# Conferir
java -version && mvn -version && node -v && npm -v && docker --version
```

## 3. Configurar o Git (uma vez)
```bash
git config --global user.name "Odimar Falcão"
git config --global user.email "SEU-EMAIL-DO-GITHUB"
git config --global init.defaultBranch main
```

## 4. Criar o repositório local
```bash
mkdir -p ~/projects && cd ~/projects
mkdir saas-barbearia && cd saas-barbearia
git init
mkdir -p backend frontend docs
```

Copie os documentos de planejamento (do Windows para o WSL). No terminal do Ubuntu, o disco C aparece em `/mnt/c`:
```bash
SRC="/mnt/c/Users/xboxf/OneDrive/Documentos/programacao/saas-barbearia"
cp "$SRC/CLAUDE.md" "$SRC/README.md" "$SRC/.gitignore" .
cp "$SRC/docs/"*.md docs/
```

## 5. Variáveis de ambiente e segredos
Princípio: **segredo nunca entra no Git**. Use um `.env` (ignorado) e um `.env.example` (versionado, sem valores reais).

Crie `.env.example` na raiz:
```dotenv
# Banco
POSTGRES_DB=barbearia
POSTGRES_USER=barbearia
POSTGRES_PASSWORD=troque-em-producao
DATABASE_URL=jdbc:postgresql://localhost:5432/barbearia

# Backend
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080

# Asaas (sandbox) — preencher no .env real, nunca aqui
ASAAS_API_KEY=
ASAAS_BASE_URL=https://sandbox.asaas.com/api/v3
```
Depois: `cp .env.example .env` e preencha o `.env` com os valores reais (esse fica fora do Git).
No Spring, os segredos são lidos de variáveis de ambiente (ex.: `${ASAAS_API_KEY}` no `application.yml`) — nunca hardcoded.

## 6. Banco de dados em Docker (dev)
Crie `compose.yaml` na raiz (só o Postgres por enquanto; backend/frontend entram na Etapa 4):
```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
volumes:
  pgdata:
```
Subir: `docker compose --env-file .env up -d`. O volume `pgdata` está no `.gitignore`.

## 7. Versionar no GitHub
O `.gitignore` já está na raiz (cobre `.env`, `target/`, `node_modules/`, etc.). Confira que o `.env` **não** aparece em `git status`.

Primeiro commit:
```bash
git add .
git commit -m "chore: estrutura inicial e documentação de planejamento"
```

Criar o repositório remoto (escolha um):
- **Via GitHub CLI** (instale `gh` e `gh auth login` uma vez):
  ```bash
  gh repo create saas-barbearia --private --source=. --remote=origin --push
  ```
- **Via site:** crie um repo **privado** vazio em github.com → depois:
  ```bash
  git remote add origin git@github.com:SEU-USUARIO/saas-barbearia.git
  git push -u origin main
  ```

> Repositório **privado** (contém regra de negócio e futuramente integrações). Segredos ficam só no `.env` local e, em produção, nas variáveis de ambiente do servidor.

## 8. Abrir no VSCode (Remote-WSL)
No terminal do Ubuntu, dentro de `~/projects/saas-barbearia`:
```bash
code .
```
O VSCode abre conectado ao WSL (canto inferior esquerdo mostra "WSL: Ubuntu"). Pronto: você edita no Windows, mas tudo executa no Linux.

## 9. Checklist final
- [ ] WSL2 + Ubuntu instalados; VSCode com extensão WSL.
- [ ] Java 21, Maven, Node LTS, Docker funcionando no Ubuntu.
- [ ] Repo em `~/projects/saas-barbearia` (fora do OneDrive) com docs copiados.
- [ ] `.env` criado e **fora** do Git; `.env.example` versionado.
- [ ] Postgres sobe com `docker compose up -d`.
- [ ] Primeiro commit feito e repo **privado** no GitHub.
- [ ] `code .` abre o projeto no Remote-WSL.

## 10. Próximo passo
Etapa 4 — scaffolding do monorepo: gerar o projeto Spring Boot (Spring Initializr) em `backend/` e o app Vite/React em `frontend/`, com o `compose.yaml` completo. **Sem regra de negócio ainda** — só o esqueleto que compila e sobe.
