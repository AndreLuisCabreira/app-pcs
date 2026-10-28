# Ambiente de desenvolvimento

## Escopo e pré-requisitos

Execute na pasta que contém `pom.xml`. O caminho recomendado é Maven com JDK 21 e Maven 3.9+ conforme o README original. O POM exige release 21 e configura `src`, `test` e `resources`. Não há Maven Wrapper no projeto. O banco pode ser PostgreSQL/Supabase ou MariaDB/MySQL; não é preciso instalar ambos. Para PostgreSQL, consulte [SUPABASE.md](../SUPABASE.md).

```powershell
java -version
mvn -version
```

Confira o Java informado por `mvn -version`. O comando `mysql --version` só é pertinente se usar o cliente MySQL/MariaDB local.

## Configuração

Copie o exemplo somente se ainda não possuir configuração; preserve seu arquivo atual:

```powershell
Copy-Item .env.example .env.local
```

Edite o arquivo e preencha a senha. A aplicação não lê `.env.example` diretamente. Fontes: [ConfiguracaoAplicacao.java](../src/ConfiguracaoAplicacao.java) e [ConnectionFactory.java](../src/ConnectionFactory.java).

O `Copy-Item` acima deve ser usado somente quando `.env.local` não existir; não o execute sobre uma configuração de trabalho. Os exemplos deste guia são fictícios e não exigem publicar valores privados.

| Variável | Uso e padrão |
| --- | --- |
| `DB_URL` | JDBC; padrão MySQL local na porta 3306, banco `pcbuilder` |
| `DB_USER` | Usuário JDBC; padrão `pcbuilder` |
| `DB_PASSWORD` | Senha JDBC; padrão vazio, rejeitado salvo permissão explícita |
| `DB_ALLOW_EMPTY_PASSWORD` | Padrão `false`; `true` admite senha vazia para ambiente local controlado |
| `DB_POOL_SIZE` | Máximo de conexões; padrão 4 |
| `DB_CONNECTION_TIMEOUT_MS` | Espera do pool; padrão 10000 ms |

A precedência por chave é: variável de ambiente não vazia → arquivo selecionado → padrão do código. O arquivo é selecionado por `-Dintratech.config=CAMINHO`, quando definido; senão, `.env.local`, caso exista; senão, `.env`, relativos ao diretório de execução. Os arquivos não são mesclados. O carregador remove aspas duplas externas e ignora linhas vazias/comentários; não promete interpolação de variáveis ou toda a sintaxe de bibliotecas dotenv. Falhas de leitura são ignoradas pelo carregador e podem levar ao uso dos padrões.

Exemplo de ambiente para MariaDB/MySQL local, com valores fictícios:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/pcbuilder?useSSL=false&serverTimezone=UTC'
$env:DB_USER = 'pcbuilder'
$env:DB_PASSWORD = 'SUBSTITUA_LOCALMENTE'
$env:DB_ALLOW_EMPTY_PASSWORD = 'false'
```

Para um laboratório que já use `root` sem senha, o código aceita `DB_USER=root` e `DB_ALLOW_EMPTY_PASSWORD=true`. Isso não é necessário nem recomendado para um ambiente compartilhado. Não mantenha variáveis antigas apontando para outro banco.

## Preparação do banco

Os comandos desta seção alteram o banco e são procedimentos administrativos, não parte da simples compilação.

- Banco novo MariaDB/MySQL: executar [database/schema.sql](../database/schema.sql) com usuário de provisionamento.
- Banco novo PostgreSQL: seguir [SUPABASE.md](../SUPABASE.md).
- Banco existente: fazer backup e avaliar [DATA_MODEL.md](DATA_MODEL.md) antes do atualizador.

O README anterior sugeria o seguinte usuário local, exemplo que não cobre todas as permissões exigidas pelo atualizador:

```sql
CREATE USER 'pcbuilder'@'localhost' IDENTIFIED BY 'SUBSTITUA_LOCALMENTE';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON pcbuilder.* TO 'pcbuilder'@'localhost';
FLUSH PRIVILEGES;
```

Separe operação normal de provisionamento. O atualizador também executa `ALTER TABLE` e `CREATE INDEX`; as permissões devem ser avaliadas pelo responsável pelo banco, sem elevar indiscriminadamente as credenciais do aplicativo.

## Compilar, testar e executar com Maven

Os comandos abaixo foram conferidos por leitura de [pom.xml](../pom.xml) e dos scripts em 28/10/2026; não foram executados nesta revisão documental.

| Objetivo | Comando | Efeito |
| --- | --- | --- |
| Compilar | `mvn compile` | Gera classes/recursos locais; não inicia o aplicativo |
| Testar | `mvn test` | Compila e executa a suíte com fixtures de teste |
| Limpar e testar | `mvn clean test` | Remove saídas em `target` antes de compilar/testar |
| Abrir interface | `mvn javafx:run` | Inicia `DesktopLauncher`; acessa o banco configurado |
| Abrir pelo Exec Plugin | `mvn exec:java` | Alternativa para `DesktopLauncher`, após compilar |

Para um teste específico já existente: `mvn "-Dtest=MontagemPCTest" test`. Relatórios e limites das execuções estão em [TESTING.md](TESTING.md). Não confunda os comandos de teste com os procedimentos administrativos de banco.

```powershell
mvn clean test
mvn javafx:run
```

O primeiro comando compila fontes/testes e executa testes; o segundo abre o aplicativo e requer banco configurado. Alternativamente, após compilar:

```powershell
mvn exec:java
```

O POM configura `DesktopLauncher`. Para o terminal legado:

```powershell
mvn exec:java "-Dexec.mainClass=Main"
```

O terminal agora exige login de uma conta ADMIN para abrir as operações globais; contas comuns usam a interface gráfica. O cadastro usa o mesmo serviço. Veja [SECURITY.md](SECURITY.md).

## VS Code e scripts Windows

Abra a raiz do projeto, instale as extensões Java, configure JDK 21 e aguarde a importação Maven. A configuração incluída em `.vscode/launch.json` usa `DesktopLauncher` e o diretório do projeto. Selecione **Executar PC Builder (configuração .env.local)** no F5. Ela não declara variáveis do banco; o carregador Java faz a leitura do arquivo. `.vscode/` está ignorada no Git, portanto clones podem não receber essa configuração.

Os scripts abaixo são alternativas locais, dependentes de Windows e do cache Maven:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-dependencies.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\compile.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
```

`java-tools.ps1` procura JDK 21+ em JAVA_HOME, PATH e, como alternativa, na extensão Red Hat do VS Code. Se Maven estiver no PATH, `compile.ps1` e `test.ps1` o usam e resolvem o POM; sem Maven, preservam a compilação manual com bibliotecas em `.m2`. `install-dependencies.ps1` baixa bibliotecas auxiliares, incluindo H2 para testes, mas não instala Maven, JavaFX, MySQL Connector/J ou o JDK. A extensão do VS Code deixou de ser obrigatória quando há JDK/Maven normais configurados.

## Diagnóstico e limites

- Dependência ausente: confira a resolução Maven e versões do POM; o script auxiliar não é um bootstrap completo.
- Tela de esquema incompleto: diferencie banco novo de atualização; veja [DATA_MODEL.md](DATA_MODEL.md).
- Login inválido: confirme credenciais da conta do aplicativo, diferentes das credenciais JDBC.
- Configuração inesperada: confira diretório de execução, `intratech.config` e variáveis do processo, sem imprimir valores privados.
- Empacotamento: veja [DISTRIBUICAO.md](../DISTRIBUICAO.md).

O registro histórico de 21/10/2026 documenta uma primeira execução com 54 testes e outra, após a montagem guiada, com 64 aprovados, por JUnit Console/JDK 21. Naquele ambiente o Maven não resolveu dependências pela rede Java e os JARs foram obtidos separadamente. Esse diagnóstico de rede é histórico, não uma falha verificada na sua instalação atual. Na etapa exclusivamente documental de 28/10/2026 não houve execução. Na implementação posterior do mesmo dia, fontes/testes foram compilados com Java 21 e 71 testes passaram por JUnit Console, além do smoke JavaFX/H2. Maven, scripts PowerShell, executável Windows e bancos nativos não foram executados nesta implementação; veja [TESTING.md](TESTING.md).
