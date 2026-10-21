# Backup e recuperação

## Backup pelo aplicativo

Em **Minha conta → Backup administrativo**, uma conta ADMIN pode salvar um JSON com contas (incluindo hashes), componentes, builds, jogos, benchmarks e registros de importação. O perfil e a senha atual são novamente verificados no banco. O arquivo não contém a senha JDBC, mas contém dados privados e hashes: guarde-o em local protegido. O aplicativo não criptografa o arquivo.

`DatabaseBackupService` exporta as 11 tabelas permitidas em transação de leitura repetível, com formato versão 1. Grava um arquivo temporário no mesmo diretório e o move para o destino após concluir. O limite atual é 64 MiB. Nome, gênero, descrição e URL de capa dos jogos são preservados. A definição de colunas é compartilhada com `DatabaseTransfer`.

## Restaurar pelo aplicativo

1. Faça uma cópia atual antes de restaurar e combine uma janela sem outras gravações.
2. Confira se o banco de destino possui o schema completo da mesma versão. O backup é de dados; não cria tabelas nem aplica migrations.
3. Entre com uma conta administradora, abra **Minha conta → Restaurar backup** e selecione o JSON.
4. Leia a confirmação de substituição integral dos dados e confirme sua senha atual.
5. Após sucesso, a sessão é encerrada. Entre com uma conta presente no backup e verifique as informações.

A restauração valida versão, nomes de tabelas, quantidade/tipo estrutural dos valores e presença de pelo menos uma conta ADMIN com login e hash PBKDF2 válidos. Não executa SQL recebido do arquivo. Insere valores com `PreparedStatement`, respeitando a ordem das FKs. Usa DELETE e INSERT em transação; uma falha de integridade provoca rollback. PostgreSQL reinicia identidades de maneira transacional; em MySQL/MariaDB, exige tabelas InnoDB e mantém os contadores do servidor, que podem continuar maiores que o maior ID restaurado.

Se a conexão cair durante COMMIT, o resultado pode ser incerto: confira o banco antes de repetir. Não é uma recuperação automática de desastre. Se nenhuma conta administrativa atual puder autenticar, ou se o banco/schema estiver indisponível, um operador precisa usar as ferramentas nativas e credenciais administrativas apropriadas. O aplicativo não cria um atalho de autenticação para esse caso.

## Verificações realizadas e limites

Testes JDBC H2 descartáveis verificam ida e volta de contas/builds/metadados, senha/perfil exigidos, rejeição de formato ou hash inválido e rollback integral após erro de FK. H2 não substitui testes de recuperação em servidores PostgreSQL e MariaDB/MySQL nativos. Datas, permissões e identidades precisam de conferência no ambiente de destino; não foi executada restauração no Supabase do usuário.

## Antes de alterações estruturais

Para uma cópia de recuperação independente do aplicativo, use ferramenta nativa compatível com o banco, como `pg_dump` ou `mariadb-dump`/`mysqldump`. Não existe um comando universal validado no repositório. Registre data, banco, versão, escopo e local protegido; restaure em uma instância isolada e verifique conteúdo, relações, contas e permissões antes da operação real. Copiar apenas o executável não faz backup do banco.

Responsável operacional, frequência, destino protegido, retenção, RPO (perda aceitável) e RTO (tempo aceitável de recuperação) continuam pendentes de decisão. Não há agendamento automático de backups.

## Transferência MariaDB/MySQL → PostgreSQL

A ferramenta [migrate-mariadb-to-supabase.ps1](../scripts/migrate-mariadb-to-supabase.ps1) continua disponível, separada do backup JSON:

| Variáveis/argumentos | Comportamento |
| --- | --- |
| `MIGRATION_SOURCE_URL` | Origem; padrão MySQL local `pcbuilder` |
| `MIGRATION_SOURCE_USER` | Usuário da origem; padrão `root` |
| `MIGRATION_SOURCE_PASSWORD` | Senha da origem; padrão vazio |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Destino, pela configuração JDBC normal |
| Sem `-Execute` | Prévia de contagens; compila fontes, mas não copia dados |
| `-Execute` | Transfere se o destino estiver vazio |
| `-Execute -Replace` | Permite substituir dados existentes; exige avaliação e backup prévios |

`MIGRATION_SOURCE_*` é lido diretamente do ambiente, não do dotenv. A transferência completa usa TRUNCATE no destino e transação, ajusta sequências e compara contagens; não compara todo o conteúdo. Agora copia também os metadados novos de jogos, com regressão JDBC. Valide uma migração real em ambiente isolado antes de usá-la em produção.
