# Estratégia de testes e verificação

## Infraestrutura existente

JUnit Jupiter 5.11.4, Maven Surefire 3.5.2 e fontes em `test/`, conforme [pom.xml](../pom.xml). A base anterior tinha 30 métodos `@Test` em nove arquivos. A versão de 21/10/2026 tem 64 testes em 16 classes, executados conforme o registro abaixo. H2 2.3.232 foi adicionado somente ao escopo de testes.

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

`BancoTeste` cria uma instância H2 em memória por teste, usando o schema MySQL do projeto sem os comandos de criação/seleção do banco. Não usa `.env`, não abre o banco do usuário e não depende de Docker.

## Executar

Com JDK 21 e Maven preparados conforme [DEVELOPMENT.md](DEVELOPMENT.md):

```powershell
mvn clean test
```

Os testes usam objetos locais, DAO falso e bancos H2 descartáveis; não precisam de credenciais reais. O Maven pode baixar dependências na primeira execução. Confira resultado e relatórios em `target/surefire-reports` depois da execução; o ZIP original não os forneceu.

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

Não há cobertura quantitativa, suíte E2E identificada, configuração de lint ou CI incluída. Não introduzir novas ferramentas apenas para preencher esta lista.

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
