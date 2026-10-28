# Estratégia de testes e verificação

## Infraestrutura existente

JUnit Jupiter 5.11.4, Maven Surefire 3.5.2 e fontes em `test/`, conforme [pom.xml](../pom.xml). A base anterior tinha 30 métodos `@Test` em nove arquivos. Em 28/10/2026 foram contados estaticamente 64 métodos anotados com `@Test` em 16 classes do ZIP recebido. A execução registrada em 21/10/2026 permanece nas seções históricas abaixo; os testes não foram executados novamente nesta revisão documental. H2 2.3.232 está somente no escopo de testes. **Após essa etapa documental**, as correções de código de 28/10/2026 elevaram o total para 71 testes em 18 classes, executados conforme a última seção deste documento.

| Arquivo em `test/` | Quantidade | Escopo |
| --- | --- | --- |
| `AutenticacaoServiceTest.java` | 6 | Credenciais, normalização, perfil inicial, duplicidade e remoção do hash da sessão com DAO falso |
| `AutorizacaoServiceTest.java` | 3 | Administrador, usuário comum e sessão ausente |
| `model/BuildTest.java` | 2 | Soma de preços e componentes ausentes |
| `PcPartDatasetClientTest.java` | 4 | Parser/normalização e identidade externa; não prova disponibilidade da rede |
| `RecomendacaoJogoServiceTest.java` | 2 | Configuração compatível com 60 FPS e indisponibilidade |
| `service/CompatibilidadeServiceTest.java` | 4 | Socket, DDR e build incompleta |
| `service/ConsumoServiceTest.java` | 3 | Soma, margem, fonte insuficiente/ausente |
| `service/FPSServiceTest.java` | 5 | Médias, referência próxima, cenário e ausência de dados |
| `service/PasswordServiceTest.java` | 1 | Hash, salt e verificação de senha |

## Testes adicionados

| Classe | Quantidade | Escopo |
| --- | --- | --- |
| `JdbcIntegrationTest` | 8 | Isolamento de builds, SQL parametrizado, duplicidade/rollback de importação, troca de senha e concorrência, cópia de jogos, atualizador idempotente em H2 |
| `RecomendacaoFiltrosTest` | 3 | Orçamento exato/insuficiente, resolução, FPS e limites inválidos |
| `service/BuildExportServiceTest` | 3 | HTML com escape, ausência de credenciais, propriedade e comparação |
| `service/CatalogoFiltroTest` | 2 | Acentos, fabricante/socket e preços inclusivos |
| `DatabaseSecurityServiceTest` | 2 | Comandos de RLS/revogação e ausência de roles Supabase em PostgreSQL comum |
| `DatabaseBackupServiceTest` | 6 | Restauração fiel, rollback de FK, reautenticação, perfil atual e arquivos inválidos |
| `service/MontagemPCTest` | 10 | Ordem das etapas, retorno sem perda, filtros, invalidação de dependências, validação final e edição com identidade/proprietário preservados |
| `BuildsOperacoesTest` | 6 | Gravação/edição/exclusão confirmadas com falha de lista; falha de gravação; propriedade e consulta da conta, com H2/fake |
| `ui/UiSupportMoedaTest` | 1 | Formatação concorrente em oito threads, com valores esperados fixos |

`BancoTeste` cria uma instância H2 em memória por teste, usando o schema MySQL do projeto sem os comandos de criação/seleção do banco. Não usa `.env`, não abre o banco do usuário e não depende de Docker.

## Executar

Com JDK 21 e Maven preparados conforme [DEVELOPMENT.md](DEVELOPMENT.md):

```powershell
mvn clean test
```

Os testes usam objetos locais, DAO falso e bancos H2 descartáveis; não precisam de credenciais reais. O Maven pode baixar dependências na primeira execução. Confira resultado e relatórios novos em `target/surefire-reports` depois da execução Maven. A execução histórica direta pelo JUnit Console usou outro diretório (`target/verification-reports`); relatórios incluídos em um ZIP são artefatos anteriores, não prova de nova execução.

Para preservar saídas existentes, prefira `mvn test` quando não houver necessidade de limpeza. Para a montagem: `mvn "-Dtest=MontagemPCTest" test`. Execute na raiz, pois `BancoTeste` lê `database/schema.sql` por caminho relativo.

Alternativa Windows: `powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1`. Ela procura JDK 21+ em JAVA_HOME/PATH, com extensão Java como alternativa; usa Maven quando disponível ou bibliotecas de `.m2` no fallback. Recria saídas de compilação. Não é um teste somente de leitura.

O script `smoke-test-package.ps1` inicia o executável, aguarda cinco segundos, verifica se ele continua ativo e o encerra. Isso não comprova conexão, login ou fluxo completo de usuário.

## Plano manual proposto

Executar em base de teste, com contas/dados descartáveis e alterações autorizadas. O roteiro manual completo continua pendente; parte dos comportamentos foi validada pelos testes automatizados e renderizações descritos abaixo.

| Cenário | Resultado esperado |
| --- | --- |
| Abrir com esquema ausente/incompleto | Mensagem de inicialização; nenhuma migration automática |
| Primeiro cadastro gráfico em base vazia, depois segundo cadastro | Primeiro ADMIN, segundo USUARIO; sessão sem hash |
| Login incorreto e logout | Mensagem genérica; saída retorna à autenticação |
| Conta A e conta B com builds | A lista somente suas builds na interface gráfica; B não altera/exclui a build de A pelo fluxo filtrado |
| Salvar peças incompatíveis/fonte insuficiente | Rejeição com mensagem, sem persistência da build inválida |
| Montagem guiada e trocas | Uma categoria por etapa; retorno preserva escolhas; troca invalida apenas dependências incompatíveis; revisão antes de salvar |
| Benchmark exato, próximo e inexistente | Classificação e FPS conforme [REQUIREMENTS.md](REQUIREMENTS.md) |
| Importar item duas vezes | Segunda importação rejeitada; sem registro parcial |
| Catálogo sem capa, com URL inválida ou imagem indisponível | Identificação textual mantida; falha de imagem não impede reconhecer o jogo |
| Busca por nome/gênero com acentos; foco por teclado | Filtragem normalizada e nome acessível |
| Windows: arraste, Win + setas, duplo clique, minimizar/restaurar | Comportamento da janela nativa conferido no Windows |

## Lacunas e próximos testes

Priorizar integração dos DAOs com PostgreSQL e MariaDB/MySQL descartáveis, isolamento por proprietário, rollback de importações e atualização de banco antigo. Testar transferência por conteúdo, inclusive os novos campos de jogo, sem depender apenas de contagens. Validar permissões/RLS nas tabelas novas e atualizadas com roles apropriadas. Parte desses cenários agora está coberta em H2 e no gerador SQL; testes completos nos servidores nativos ainda são pendentes.

Não há cobertura quantitativa, lint ou CI incluída. Os harnesses JavaFX de 21/10 eram registros históricos externos. A implementação de 28/10 acrescenta `test/BuildsUiSmoke.java`, um smoke executável versionado, separado dos testes JUnit. Ele cobre o fluxo de builds em H2 descartável, sem constituir cobertura E2E de toda a aplicação. Não introduzir novas ferramentas apenas para preencher esta lista.

## Resultado desta entrega documental

Em 19/10/2026 foram verificadas correspondência de arquivos, referências locais, nomes de classes, variáveis e comandos contra o código. Código Java, testes, scripts, SQL e recursos foram preservados. Build, JUnit, interface, banco, migrations e empacotamento Windows não foram executados: a mudança é documental. As alegações de 30 testes aprovados e inspeção visual em [ATUALIZACOES-CATALOGO.md](../ATUALIZACOES-CATALOGO.md) são registros históricos, não resultados desta entrega.

## Validação da implementação — 21/10/2026

- Compilação de todos os fontes e testes com Zulu JDK 21.0.6, `javac --release 21`, usando as versões declaradas no POM.
- JUnit Platform Console 1.11.4: **54 testes executados, 54 aprovados, zero ignorados**. Inclui os 30 testes anteriores.
- H2 2.3.232: persistência, isolamento, transações, cópia de jogos e restauração de dados em bases descartáveis.
- PostgreSQL WASM via PGlite 0.5.11: execução do schema, simulação de grants públicos antigos e aplicação dos comandos produzidos por `DatabaseSecurityService`. Confirmadas 11 tabelas com RLS e 22 leituras negadas (11 por role `anon`/`authenticated`). Também aceita sintaxe transacional de reinício de identidade.
- JavaFX 21.0.8 com Monocle: renderização headless de Minha conta/backup, catálogo com resolução e comparação. Snapshots internos conferidos; não são testes ponta a ponta de todos os botões.

O Maven não conseguiu acessar o repositório pela rede Java do ambiente. Para não declarar execução inexistente, a compilação e o JUnit foram rodados diretamente com os JARs dessas versões. A obtenção separada dos JARs não altera a configuração de execução normal por Maven.

Não foram executados comandos no banco do usuário. Permanecem pendentes: executável/encaixe e scripts PowerShell no Windows, fluxo completo de diálogos de arquivos, integração nativa PostgreSQL/MariaDB e testes de recuperação de desastre. O teste PGlite valida o SQL e as permissões, não todo o protocolo JDBC/Supabase.

## Validação da montagem guiada — 21/10/2026

- Compilação Java 21 de todos os fontes e testes; JUnit Console: **64 executados, 64 aprovados, zero ignorados**, incluindo os 54 anteriores e dez testes de `MontagemPC`.
- Harness JavaFX/Monocle com catálogo e DAO falsos: escolha das seis peças, bloqueio de avanço sem escolha, retorno, busca que oculta e volta a exibir a peça sem perder a seleção, revisão/nome, rascunho mantido entre abas e abertura de build para edição com o mesmo ID.
- Harness adicional com `BuildDAO` real e H2 descartável: criação pelo assistente, retorno à tabela, abertura para edição e salvamento de novo nome; confirmado um único registro com o mesmo ID/proprietário e bloqueio das abas durante o salvamento assíncrono.
- Snapshots internos conferidos em claro e escuro, incluindo área de conteúdo de 756 × 680 para a largura mínima da aplicação. Não usa o banco do usuário e não comprova integração nativa do executável Windows.
- Nenhuma mudança no schema, migration ou dependência de produção. O salvamento usa os mesmos DAOs com filtro por proprietário; os testes JDBC existentes continuam passando em H2 descartável.

## Verificação documental — 28/10/2026

Foram conferidos por leitura o POM, scripts e classes citados, contagem de anotações, caminhos e referências locais. Fontes, testes e configurações não foram alterados. Não foram executados Maven, JUnit, JavaFX, banco, migrations ou scripts de empacotamento. Os resultados de 19/10 e 21/10 acima são preservados com suas datas, sem serem revalidados por esta edição.

| Fluxo crítico | Evidência disponível no pacote | Validação adicional proposta, não executada agora |
| --- | --- | --- |
| Login/perfil/troca de senha | `AutenticacaoServiceTest`, `AutorizacaoServiceTest`, `JdbcIntegrationTest` | Exercitar a interface e concorrência de primeiro ADMIN em base isolada |
| Etapas/trocas/edição | `service/MontagemPCTest` testa o estado, identidade e invalidação de dependências | Interação JavaFX completa, falha assíncrona e retorno ao rascunho |
| Isolamento e gravação | `JdbcIntegrationTest` usa DAO real/H2 e duas contas | Repetir no motor nativo escolhido, sem usar a base do usuário |
| Backup/restauração | `DatabaseBackupServiceTest` usa H2 e falhas de integridade | Recuperação de desastre, permissões e identidades nos servidores nativos |
| FPS, orçamento e catálogo | Testes de FPS, recomendação, filtros e exportação | Conferência visual e bases com cenários reais revisados |
| Distribuição | Scripts e roteiro Windows disponíveis | Executável Windows, instalação/configuração e fluxos do usuário |

Detalhes do escopo e das verificações documentais estão no [relatório da atualização](DOCUMENTATION_REPORT_2026-10-28.md).


## Validação das correções de código — 28/10/2026

Esta seção é posterior à etapa documental do mesmo dia. Foram compilados todos os fontes e testes com Zulu JDK 21.0.6 (`javac --release 21`, UTF-8), usando os JARs das versões do POM. JUnit Platform Console 1.11.4: **71 testes executados, 71 aprovados, zero ignorados**. São os 64 anteriores e sete novos, em 18 classes JUnit.

O teste de formatação concorrente falhou antes da correção com `NullPointerException` no estado compartilhado de `DecimalFormat`; depois passou. A correção usa `ThreadLocal<NumberFormat>` e não modifica o modelo de preços. Referência: [NumberFormat, sincronização no Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/NumberFormat.html).

`BuildsOperacoesTest` confirma em H2 descartável que inserção, atualização e exclusão permanecem concluídas quando apenas a listagem posterior lança `DataAccessException`; erro na escrita continua sendo propagado. Também verifica propriedade e lista filtrada da conta.

O novo [BuildsUiSmoke.java](../test/BuildsUiSmoke.java) passou com JavaFX 21.0.8 no module path e Monocle 21.0.2 em modo headless, usando H2 e falhas injetadas. Verificou JDBC inicial/alterações fora da thread FX, construção de controles nela, escolha das seis peças, save confirmado com falha na lista, atualização, falha real de gravação sem perder rascunho, nova tentativa, edição preservando ID e exclusão com recuperação da lista. Não acessa configuração privada. Referência: [Platform do JavaFX 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/application/Platform.html).

Para repetir o smoke com GUI, compile os testes e execute o método `main` de `BuildsUiSmoke` na IDE com classpath de testes/recursos e JavaFX configurado para o sistema operacional. Ele abre/fecha janelas e confirma somente seus próprios diálogos, usando exclusivamente um H2 descartável. O smoke não é um método JUnit e não roda automaticamente com `mvn test`. Monocle foi utilizado apenas no ambiente de verificação; não foi acrescentado ao POM nem é necessário para a execução com GUI.

### Organização do smoke e correção para a IDE

O smoke fica em `test/BuildsUiSmoke.java`, no pacote padrão, assim como `BancoTeste`, `BuildDAO` e as telas que utiliza. A localização anterior em `test/manual/` causava diagnóstico de pacote incompatível na IDE e erros de tipos não resolvidos em cascata, embora a compilação direta por `javac` tivesse aceitado o arquivo. Adicionar `package manual` não resolveria o acesso às classes do pacote padrão.

Ao atualizar uma cópia existente, prefira extrair o ZIP em uma pasta nova. Se substituir arquivos na mesma pasta, confirme a presença de `test/BuildsUiSmoke.java` e remova somente a cópia antiga `test/manual/BuildsUiSmoke.java`; os dois arquivos não devem coexistir. A execução normal do aplicativo continua sendo `DesktopLauncher` ou `mvn javafx:run`, não o smoke de testes.

Após a correção, a verificação de pacote/diretório confirmou os 91 arquivos Java em `src/` e `test/`; fontes e testes foram compilados novamente com Java 21, e os 71 testes JUnit e o smoke JavaFX/H2 passaram. Essa verificação não constitui execução da extensão Java do VS Code nem do aplicativo Windows.

Não foram executados Maven, empacotamento ou scripts PowerShell, teste nativo Windows ou integração com PostgreSQL/MariaDB remotos. Não houve execução de migrations. O construtor de conveniência de `BuildsView` continua síncrono; o caminho assíncrono testado é o usado na navegação normal. As fábricas legadas das outras páginas não foram migradas nesta alteração. Não há medição percentual de cobertura.
