# Conexão com o Supabase

O PC Builder aceita PostgreSQL/Supabase por JDBC e continua compatível com o
MariaDB local. A escolha é automática a partir de `DB_URL`.

## 1. Proteja as credenciais

Uma chave `sb_secret_...` nunca deve ser incluída no código, no `.env` de uma
aplicação desktop ou no executável. Se uma chave secreta foi compartilhada em
texto aberto, revogue-a em **Project Settings > API Keys** e crie outra apenas
se algum backend protegido realmente precisar dela.

Este projeto não utiliza API keys para a conexão JDBC. Ele usa a senha do banco
PostgreSQL, armazenada somente no arquivo local `.env`, que já está no
`.gitignore`.

> A conexão direta é adequada para desenvolvimento e demonstração em uma
> máquina controlada. Para distribuir o aplicativo a terceiros, use Supabase
> Auth + Data API com RLS, ou coloque o acesso ao banco atrás de um backend.

## 2. Crie as tabelas

No painel do Supabase, abra **SQL Editor > New query**, copie todo o conteúdo de
[`database/supabase.sql`](database/supabase.sql) e clique em **Run**.

O script cria as tabelas, relacionamentos, índices e jogos iniciais. Ele também
ativa RLS e bloqueia o acesso pelas roles públicas `anon` e `authenticated`,
pois a aplicação atual usa JDBC e mantém autenticação própria.

## 3. Obtenha a conexão do banco

No projeto Supabase, clique em **Connect** e escolha **Session pooler**, porta
`5432`. Essa opção funciona em redes IPv4 e é apropriada para uma aplicação
desktop persistente.

Você precisará de três valores:

- host do Session pooler;
- usuário no formato `postgres.PROJECT_REF`;
- senha do banco definida ao criar o projeto.

Se não souber a senha, redefina-a em **Project Settings > Database**. As chaves
`sb_publishable_...` e `sb_secret_...` não são a senha do PostgreSQL.

## 4. Configure o `.env`

Copie `.env.example` para `.env` e preencha os dados reais:

```dotenv
DB_URL=jdbc:postgresql://HOST_DO_POOLER:5432/postgres?sslmode=require
DB_USER=postgres.PROJECT_REF
DB_PASSWORD=SUA_SENHA_DO_BANCO
DB_ALLOW_EMPTY_PASSWORD=false
```

Não coloque colchetes nos valores e não envie o `.env` ao GitHub.

## 5. Instale e execute

No PowerShell, dentro de `Montagem-PCs`:

```powershell
.\scripts\install-dependencies.ps1
.\scripts\compile.ps1
```

Depois use **Executar e Depurar > Executar PC Builder (configuração .env)** no
VS Code. Quando a conexão estiver correta, a barra lateral mostrará
`Supabase conectado`.

## 6. Atualizações do banco

O aplicativo não altera mais a estrutura do Supabase durante a inicialização.
Para atualizar um projeto existente depois de receber uma nova versão do
PC Builder, execute na raiz do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

O primeiro comando aplica somente alterações idempotentes conhecidas; o segundo
faz uma verificação de leitura. Em um projeto Supabase novo, continue usando
`database/supabase.sql`, que contém a estrutura completa.

## Diagnóstico

- `password authentication failed`: confira `DB_USER` e `DB_PASSWORD`.
- `connection refused` ou timeout: use o **Session pooler**, não a conexão
  direta IPv6.
- `relation ... does not exist`: execute `database/supabase.sql` por completo.
- `O banco de dados não está pronto`: execute `scripts\migrate-database.ps1`
  e depois `scripts\check-database.ps1` antes de reabrir o aplicativo.
- erro de SSL: confirme `?sslmode=require` no final de `DB_URL`.
