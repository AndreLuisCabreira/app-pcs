# Segurança e privacidade — estado observado

Fontes: código Java, schemas e scripts do pacote de 19/10/2026. A análise foi estática; não verificou credenciais, permissões efetivas, serviços remotos ou vulnerabilidades atuais das dependências.

## Fronteiras de confiança

A interface desktop conecta diretamente ao banco por JDBC. A autenticação usa `usuario`, não Supabase Auth. Um usuário com acesso à credencial JDBC pode agir fora das telas; as restrições da interface não substituem permissões do banco ou de um backend confiável. O uso em computadores controlados já está previsto nos guias. Distribuição pública requer uma decisão arquitetural e implementação próprias antes de prometer isolamento resistente a clientes alterados.

## Controles presentes e limites

| Controle | Evidência | Limite |
| --- | --- | --- |
| Senhas de contas | `service/PasswordService`: PBKDF2-HMAC-SHA-256, 210000 iterações, salt de 16 bytes e chave de 256 bits | Não é autenticação remota independente do cliente |
| Sessão gráfica | `AutenticacaoService` remove hash do objeto retornado | Outros métodos de DAO podem carregar hashes; não serializar esses objetos |
| Falha de login | Mensagem genérica para credenciais inválidas | Não foi identificado rate limiting centralizado |
| Propriedade de builds | Métodos `DoUsuario` e listagem filtrada no `BuildDAO` | Métodos globais continuam presentes; menu do terminal exige ADMIN |
| Perfil administrativo | Shell e telas verificam perfil | Não protege contra uso direto da credencial JDBC |
| SQL e transações | Valores por `PreparedStatement`; importação com commit/rollback | DDL administrativo também usa `Statement` com identificadores controlados; não afirmar que todo SQL é preparado |
| Configuração | `.env`/`.env.local` ignorados pelo Git; senha vazia exige opção explícita | `.gitignore` não impede inclusão manual em ZIP |
| Transporte PostgreSQL | URL recebe `sslmode=require` se ausente | Um valor explícito é preservado; não há imposição absoluta nem auditoria do certificado nesta revisão |

O terminal `Main` exige login e ADMIN antes de listar, alterar ou excluir builds globalmente, escolher proprietário ou cadastrar componentes. O cadastro passa pelo mesmo serviço da interface gráfica. Isso fecha a entrada sem autenticação, mas não cria uma fronteira confiável contra quem possui acesso JDBC direto.

A primeira conta gráfica vira administrador por contagem e inserção separadas. Há risco de concorrência em banco vazio; não foi reproduzido. Não existe procedimento de recuperação de administrador validado nesta entrega.

## PostgreSQL/Supabase

O [schema inicial](../database/supabase.sql) ativa RLS nas 11 tabelas e revoga acesso de `anon`/`authenticated`. Ele não cria políticas de propriedade por conta do aplicativo. A identidade JDBC é compartilhada por conexões da aplicação; privilégios reais e eventual bypass de RLS dependem da role utilizada.

O [atualizador Java](../src/DatabaseMigrationService.java) agora chama `DatabaseSecurityService` para ativar RLS nas 11 tabelas e revogar privilégios de `anon`/`authenticated`, quando essas roles existem. DDL e proteção são envolvidos em transação no PostgreSQL. Os comandos gerados foram exercitados em PostgreSQL WASM/PGlite; não foram executados no banco do usuário. O verificador de abertura continua sem auditar essas proteções. Privilégios de outras roles e configurações do ambiente exigem avaliação própria.

## Credenciais e pacotes

Use [.env.example](../.env.example) com valores fictícios. Não publique arquivos configurados, chaves privadas ou hashes de contas. O ZIP de entrada contém configuração local preenchida, embora fora do índice Git; sua validade não foi testada. Se uma credencial real tiver sido disponibilizada a terceiros sem autorização, o responsável deve substituí-la no serviço correspondente e atualizar suas cópias privadas.

Antes de preparar uma cópia para compartilhamento público, exclua da seleção de empacotamento `.env`, `.env.local`, configurações privadas e históricos com segredos. Não apague a configuração de trabalho do usuário. O pacote devolvido nesta tarefa preserva os arquivos originais e não deve ser confundido com uma distribuição sanitizada para publicação.

`check-env-safe.ps1` classifica valores de `.env` sem imprimi-los, mas não examina `.env.local` nem certifica a segurança de um ZIP. O empacotamento Windows copia o exemplo e verifica ausência de `.env`; não é um scanner completo de segredos.

## Dados pessoais e recuperação

A tabela de contas mantém nome, login e hash; builds ligam preferências e peças à conta. Não foram identificados fluxo de exclusão de conta, política de retenção ou consentimento. Existe exportação das builds próprias; o backup administrativo contém dados de todas as contas e não é uma exportação pessoal. Essas políticas dependem do contexto de uso e da decisão do mantenedor; este documento não declara conformidade legal. Restringir acesso a dumps e cópias, que também contêm hashes. Veja [BACKUP_AND_RECOVERY.md](BACKUP_AND_RECOVERY.md).

Correções pendentes, prioridades e evidências ficam em [TECH_DEBT.md](TECH_DEBT.md). A entrega de 21/10/2026 também implementa os controles descritos para terminal, troca de senha, exportação e backup.

## Troca de senha e arquivos exportados

A troca de senha reautentica a conta atual, grava novo hash e condiciona a atualização ao hash anterior. Não implementa recuperação de senha esquecida nem invalidação central de sessões já abertas. O backup revalida perfil ADMIN e senha atual no banco, evitando confiar apenas no perfil carregado na sessão.

Backups são privados, contêm hashes e não são criptografados pelo aplicativo. Não são abertos como SQL: somente tabelas/colunas permitidas e valores parametrizados são restaurados. Versão, estrutura e presença de conta ADMIN com hash/login válidos são verificados antes de substituir dados. Limite de 64 MiB. Uma falha de conexão durante COMMIT pode deixar o resultado incerto; verificar o banco antes de repetir uma restauração.

Os HTMLs de builds escapam texto e não incluem credenciais, scripts ou imagens remotas. A exportação de fonte via `scripts/package-source.ps1` exclui ambientes privados, saídas e nomes usuais de backup; revisar outros arquivos privados continua necessário antes de publicação.
