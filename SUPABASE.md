# Conexão com o Supabase

O PC Builder aceita PostgreSQL/Supabase por JDBC e continua compatível com o
MariaDB local. A escolha é automática a partir de `DB_URL`.

## 1. Proteja as credenciais

Uma chave `sb_secret_...` nunca deve ser incluída no código, no `.env` de uma
aplicação desktop ou no executável. Se uma chave secreta foi compartilhada em
texto aberto, revogue-a em **Project Settings > API Keys** e crie outra apenas
se algum backend protegido realmente precisar dela.

Este projeto não utiliza API keys para a conexão JDBC. Ele usa a senha do banco
PostgreSQL, obtida do ambiente do processo ou do arquivo local selecionado.
`.env` e `.env.local` estão no `.gitignore`. A precedência completa está em
[DEVELOPMENT.md](docs/DEVELOPMENT.md). O login da aplicação é próprio, na tabela
`usuario`; não utiliza Supabase Auth.

> A conexão direta é adequada para desenvolvimento e demonstração em uma
> máquina controlada. Para distribuir o aplicativo a terceiros, é necessário
> projetar uma fronteira confiável, por exemplo Auth + Data API com políticas
> apropriadas, ou um backend. Essas alternativas não estão implementadas.
> Veja [SECURITY.md](docs/SECURITY.md).

## 2. Crie as tabelas

No painel do Supabase, abra **SQL Editor > New query**, copie todo o conteúdo de
[`database/supabase.sql`](database/supabase.sql) e clique em **Run**.

O script cria as tabelas, relacionamentos, índices e jogos iniciais. Ele também
ativa RLS e bloqueia o acesso pelas roles públicas `anon` e `authenticated`,
pois a aplicação atual usa JDBC e mantém autenticação própria. Isso descreve
o SQL fornecido, não uma verificação do banco remoto. Não há políticas que
associem a conta do aplicativo à identidade JDBC por usuário.

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

Se não houver configuração existente, copie [.env.example](.env.example) para
`.env.local` no desenvolvimento, ou para `.env` ao lado do executável portátil.
Substitua os valores do exemplo pelos dados reais:

```dotenv
DB_URL=jdbc:postgresql://HOST_DO_POOLER:5432/postgres?sslmode=require
DB_USER=postgres.PROJECT_REF
DB_PASSWORD=SUA_SENHA_DO_BANCO
DB_ALLOW_EMPTY_PASSWORD=false
```

Não coloque colchetes nos valores e não envie `.env` ou `.env.local` ao GitHub.
Se ambos existirem no desenvolvimento, `.env.local` tem preferência; variáveis
do ambiente não vazias têm prioridade. No pacote portátil, o launcher define
explicitamente o caminho de `.env`.

## 5. Instale e execute

Na pasta que contém `pom.xml`, com JDK 21 e Maven preparados:

```powershell
mvn clean test
mvn javafx:run
```

O primeiro comando compila e testa sem provisionar o banco. O segundo abre a
interface e exige a estrutura e as credenciais configuradas. No VS Code, a
configuração incluída é **Executar PC Builder (configuração .env.local)**.

Os scripts `install-dependencies.ps1` e `compile.ps1` continuam disponíveis,
mas precisam de JDK 21+ e dependências; agora preferem JAVA_HOME/PATH e Maven,
com a extensão Java apenas como alternativa. Veja [DEVELOPMENT.md](docs/DEVELOPMENT.md).

Depois da validação inicial e do login, a barra lateral pode mostrar o indicador
`Supabase conectado`. O nome é usado pelo código para URLs PostgreSQL; não é
uma auditoria de políticas ou de permissões.

## 6. Atualizações do banco

O aplicativo não altera mais a estrutura do Supabase durante a inicialização.
Para atualizar um projeto existente depois de receber uma nova versão do
PC Builder, execute na raiz do projeto:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

Antes de executar, faça backup e revise [DATA_MODEL.md](docs/DATA_MODEL.md).
O primeiro comando aplica alterações conhecidas e, no PostgreSQL, envolve a
atualização em transação, ativa RLS nas 11 tabelas e revoga privilégios das
roles `anon`/`authenticated` existentes. O segundo verifica parte da estrutura
sem escrever no banco; o executor pode recompilar e escrever saídas locais.
Não há histórico versionado de migrations, e o atualizador não cria toda a
base de um banco vazio.

Execute a migration com uma role que possa alterar as tabelas. Em projeto
novo, use `database/supabase.sql`. A proteção foi validada em uma instância
PostgreSQL WASM descartável; nenhuma mudança foi executada no seu Supabase.
O verificador normal continua sem auditar RLS/privilégios. Para backup privado
pelo aplicativo, use Minha conta com uma conta administradora e veja
[BACKUP_AND_RECOVERY.md](docs/BACKUP_AND_RECOVERY.md).

## Diagnóstico

- `password authentication failed`: confira `DB_USER` e `DB_PASSWORD`.
- `connection refused` ou timeout: use o **Session pooler**, não a conexão
  direta IPv6.
- `relation ... does not exist`: confirme banco/schema e diferencie instalação
  nova de atualização antes de executar SQL; consulte o modelo de dados.
- `O banco de dados não está pronto`: verifique se falta a base inicial ou uma
  atualização; faça backup e siga o fluxo administrativo adequado antes de reabrir.
- erro de SSL: confirme `?sslmode=require` no final de `DB_URL`.
