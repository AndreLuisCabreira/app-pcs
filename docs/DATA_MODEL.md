# Modelo de dados

Fontes normativas: [schema MySQL/MariaDB](../database/schema.sql), [schema PostgreSQL](../database/supabase.sql) e [atualizador Java](../src/DatabaseMigrationService.java). A descrição corresponde aos arquivos, não a uma inspeção do banco remoto.

## Entidades

Todas as tabelas têm `id` inteiro como chave primária. Os schemas usam auto incremento no MySQL/MariaDB e identidade no PostgreSQL.

| Tabela | Dados principais e relacionamentos |
| --- | --- |
| `usuario` | Nome, login único, hash de senha e perfil `ADMIN`/`USUARIO` |
| `processador` | Nome, fabricante, socket, núcleos, threads, consumo, preço e índice de desempenho |
| `placa_mae` | Nome, fabricante, socket, tipo de memória, consumo e preço |
| `placa_video` | Nome, fabricante, VRAM, consumo, preço e índice de desempenho |
| `memoria` | Nome, capacidade, frequência, geração DDR e preço |
| `ssd` | Nome, capacidade, leitura, escrita, tipo e preço |
| `fonte` | Nome, potência, certificação e preço |
| `jogo` | Nome único, exigências CPU/GPU e gênero, descrição e URL de imagem opcionais |
| `build` | Nome, favorita, usuário e uma referência obrigatória a cada uma das seis categorias de componentes |
| `benchmark_fps` | Jogo, CPU e GPU por FK; resolução, qualidade, FPS médio, 1% low opcional, fonte, observações e data |
| `componente_importado` | Tipo, ID do componente, origem, ID externo, nome original e data; unicidade de tipo/origem/ID externo |

Uma conta possui várias builds; um componente pode ser usado em várias builds. Um jogo possui vários benchmarks. `componente_importado.componente_id` é uma referência lógica polimórfica, sem FK para uma tabela única de componentes.

## Restrições e diferenças

Os schemas declaram campos obrigatórios, FKs e verificações numéricas: preços/consumos não negativos; capacidades, frequências e índices positivos nos campos aplicáveis; FPS médio positivo; 1% low positivo e não superior ao FPS médio. Os detalhes de tamanho e precisão permanecem nos SQLs. Não há `ON DELETE CASCADE` nas FKs declaradas de builds e benchmarks.

No PostgreSQL, preços usam `NUMERIC(12,2)`; no MySQL/MariaDB, `DECIMAL(10,2)`. Datas de benchmark/importação são `TIMESTAMPTZ` no schema PostgreSQL e `TIMESTAMP` no outro. O atualizador Java cria essas datas como `TIMESTAMP`, inclusive no PostgreSQL: instalações atualizadas podem divergir do schema novo.

O schema PostgreSQL declara índices de busca e propriedade de builds, ativa RLS nas 11 tabelas e revoga privilégios de `anon` e `authenticated`. O schema MySQL declara o índice de recomendação. Não presumir paridade de todos os índices entre os dois bancos. O bloqueio da Data API não representa isolamento por usuário JDBC; veja [SECURITY.md](SECURITY.md).

Os scripts iniciais semeiam três jogos, sem importar benchmarks ou componentes. `CREATE TABLE IF NOT EXISTS` não atualiza automaticamente uma tabela já existente.

## Ciclo de atualização

Em banco novo, usar o schema do banco escolhido. Para uma instalação existente, após backup e revisão administrativa:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

O atualizador adiciona/normaliza perfil, cria tabelas de benchmarks e importações quando ausentes, adiciona metadados dos jogos e índice de recomendação. Ele não cria toda a estrutura base e não mantém histórico versionado de migrations. No PostgreSQL, agora aplica RLS às 11 tabelas e revoga acesso das roles `anon`/`authenticated` existentes, em uma transação que envolve a atualização. Em MySQL/MariaDB, DDL pode produzir mudanças parciais devido ao commit implícito do servidor.

O verificador consulta metadados de tabelas e algumas colunas. Ele não valida todas as FKs, constraints, índices, permissões ou políticas RLS. A inicialização normal não chama o atualizador.

## Transferência MariaDB/MySQL → PostgreSQL

[DatabaseTransfer.java](../src/DatabaseTransfer.java) e [script PowerShell](../scripts/migrate-mariadb-to-supabase.ps1) têm prévia sem escrita de dados, execução explícita e substituição opcional. A execução trunca as tabelas de destino e copia em transação; verifica contagens e reajusta sequências. Não compara todo o conteúdo dos registros.

A lista de colunas de `jogo` agora inclui `genero`, `descricao` e `imagem_url`. Um teste JDBC descartável exercita a cópia desses campos; a transferência completa entre servidores nativos continua exigindo validação própria. Backups JSON reutilizam a mesma lista de colunas e não alteram o schema. Procedimentos e pré-condições de recuperação estão em [BACKUP_AND_RECOVERY.md](BACKUP_AND_RECOVERY.md).
