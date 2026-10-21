# PC Builder — IntraTech

Aplicação desktop em JavaFX para cadastrar componentes, montar computadores, verificar compatibilidade e comparar preço, consumo e benchmarks de jogos. Projeto educacional voltado à montagem e comparação de PCs; objetivos comerciais não foram definidos.

## Funcionalidades

- Login e cadastro com senhas protegidas por PBKDF2; troca de senha com confirmação da senha atual.
- Dashboard, builds e favoritas vinculados à conta na interface gráfica.
- Catálogo de CPU, placa-mãe, GPU, memória, SSD e fonte, com manutenção administrativa.
- Montagem guiada, uma peça por etapa, com retorno, resumo de preço e revisão; edição e exclusão de builds, filtros de socket/DDR e validação de fonte.
- Cálculo de preço e consumo, com análise de FPS por medições cadastradas.
- Comparação de duas ou três builds próprias e exportação em TXT ou HTML imprimível em PDF.
- Filtros de catálogo por termos e faixa de preço; recomendações por orçamento, resolução e FPS.
- Backup completo dos dados e restauração transacional por administrador, com confirmação da senha atual.
- Galeria de jogos com busca por nome/gênero, capas e recomendações para 1080p/60 FPS.
- Cadastro administrativo de jogos e benchmarks, incluindo fonte e 1% low opcional.
- Importação revisada de CPU, GPU, fonte, memória e SSD do dataset público `docyx/pc-part-dataset`.
- Persistência JDBC em PostgreSQL/Supabase ou MariaDB/MySQL.

## Tecnologias e estrutura

Java 21, JavaFX 21.0.8, Maven, HikariCP, Jackson, drivers PostgreSQL/MySQL e JUnit 5. As versões completas são mantidas no [pom.xml](pom.xml).

| Caminho | Responsabilidade |
| --- | --- |
| `src/DesktopLauncher.java` e `src/DesktopApp.java` | Inicialização gráfica |
| `src/*View.java` e `src/AppShell.java` | Telas e navegação |
| `src/*DAO.java` e `src/ConnectionFactory.java` | Persistência JDBC |
| `src/model`, `src/service`, `src/ui` | Domínio, serviços e utilitários de interface |
| `src/Main.java` | Terminal administrativo autenticado, com operações globais |
| `resources` | CSS, imagens e recursos |
| `test` | Testes unitários |
| `database` | Schemas dos dois bancos |
| `scripts` | Compilação, testes, diagnósticos, atualização e empacotamento |
| `figma-html` | Protótipo visual independente, sem persistência |

Há classes no pacote padrão e diretórios personalizados no Maven. Veja [arquitetura](docs/ARCHITECTURE.md).

## Começar

Use JDK 21 e Maven 3.9+ conforme a orientação do projeto. Abra a pasta que contém `pom.xml`.

1. Prepare **um** banco: [Supabase/PostgreSQL](SUPABASE.md) ou [schema MariaDB/MySQL](database/schema.sql).
2. Se ainda não possuir configuração, copie [.env.example](.env.example) para `.env.local` e preencha os valores. Preserve configurações locais já existentes.
3. Execute:

```powershell
mvn clean test
mvn javafx:run
```

O primeiro comando compila e testa; o segundo abre a aplicação e precisa do banco. Na primeira conta gráfica de uma base vazia, o perfil é administrador. Depois do login, a interface lista as builds e favoritas da conta.

Configuração, precedência de variáveis, VS Code, comandos alternativos, terminal e scripts Windows estão em [DEVELOPMENT.md](docs/DEVELOPMENT.md). A configuração de F5 incluída no ZIP inicia `DesktopLauncher`; o carregador Java lê o arquivo de ambiente. Não há variáveis de banco declaradas no `launch.json`.

Para banco existente, consulte o ciclo de atualização em [DATA_MODEL.md](docs/DATA_MODEL.md) e faça backup antes de mudanças. A abertura normal apenas verifica parte do esquema; não executa migrations automaticamente.

## Montar seu PC

Em **Minhas builds → + Montar novo PC**, escolha processador, placa-mãe, memória, placa de vídeo, SSD e fonte, nessa ordem. A sétima etapa revisa as peças, o nome e a opção de favorita antes de salvar. Cada etapa tem busca e mostra apenas sua categoria; placa-mãe, memória e fonte respeitam as escolhas anteriores.

Use **Voltar**, as etapas no topo ou o resumo lateral para rever peças. Trocas preservam as demais escolhas compatíveis; uma placa-mãe/memória incompatível ou fonte insuficiente é removida com aviso para nova seleção. Alternar entre as duas abas mantém o rascunho enquanto a tela estiver aberta. **Editar selecionada** abre uma build salva para revisão e troca de componentes, preservando sua identidade. Não é preciso alterar o banco para usar esse fluxo.

## Importar componentes

Na interface gráfica, somente administradores veem **Importar catálogo**:

1. Escolha CPU, GPU, fonte, RAM ou SSD e aguarde a lista pública.
2. Filtre por nome, fabricante ou especificação e selecione um resultado.
3. Revise os campos técnicos, complete os ausentes e informe o preço local em reais.
4. Clique em **Importar componente**.

O cliente baixa JSON do GitHub sem API key e oferece cache por instância e atualização manual. A importação é transacional e registra a origem para impedir duplicidade por categoria/origem/ID. Peças importadas ficam no banco, independentemente de uma nova consulta ao dataset.

## Limitações e segurança

Preços são cadastrados manualmente, sem cotação em tempo real. O README anterior informa preços externos em USD com referência de julho de 2025; a atualidade do dataset não foi validada nesta entrega. Alguns campos técnicos precisam ser preenchidos manualmente. FPS depende da quantidade e qualidade dos benchmarks; não é medição do computador em uso. Compatibilidade não cobre dimensões, BIOS, conectores ou lanes PCIe.

O login é próprio da aplicação. O desktop recebe credenciais JDBC, e o terminal agora exige login de administrador para permitir suas operações globais. Não interpretar os filtros da interface como garantia de isolamento contra quem possui a credencial do banco. Limitações, controles de senha, RLS e dados pessoais estão em [SECURITY.md](docs/SECURITY.md).

Não há recuperação de senha esquecida implementada. A troca com senha atual está em **Minha conta**. Senhas antigas em texto puro não são aceitas pelo verificador atual. A orientação anterior era criar nova conta antes de remover registros legados; isso precisa considerar propriedade de builds e privilégios, sem exclusão automática de dados. Há outras pendências em [TECH_DEBT.md](docs/TECH_DEBT.md).

## Distribuição

Para gerar o pacote portátil Windows x64, consulte os pré-requisitos e comandos de [DISTRIBUICAO.md](DISTRIBUICAO.md). A saída prevista pelo script é `dist/IntraTech-PC-Builder-1.8.1-windows-x64.zip`, com Java incluído. Este ZIP de projeto fonte não é o executável portátil. Preserve sua configuração privada e não publique cópias configuradas.

## Documentação

| Assunto | Fonte principal |
| --- | --- |
| Instalação, configuração e execução | [Desenvolvimento](docs/DEVELOPMENT.md) |
| Componentes e integrações | [Arquitetura](docs/ARCHITECTURE.md) |
| Funcionalidades, critérios e fórmulas | [Requisitos e regras](docs/REQUIREMENTS.md) |
| Tabelas, restrições e atualização | [Modelo de dados](docs/DATA_MODEL.md) |
| Acesso ao PostgreSQL/Supabase | [Guia Supabase](SUPABASE.md) |
| Controles e limitações de acesso | [Segurança](docs/SECURITY.md) |
| Execução e lacunas de testes | [Testes](docs/TESTING.md) |
| Cópias e restauração | [Backup e recuperação](docs/BACKUP_AND_RECOVERY.md) |
| Problemas conhecidos e decisões pendentes | [Débitos técnicos](docs/TECH_DEBT.md) |
| Trabalho de agentes e critérios de conclusão | [AGENTS.md](AGENTS.md) |
| Evolução do projeto | [Changelog](CHANGELOG.md) |
| Registro histórico de catálogo e janela | [Atualizações do catálogo](ATUALIZACOES-CATALOGO.md) |
| Protótipo HTML/CSS | [Guia do protótipo](figma-html/README.md) |

## Testes e roadmap

Foram executados 64 testes com JDK 21/JUnit, incluindo integração JDBC em H2 descartável e dez testes do estado da montagem guiada. O assistente foi exercitado em JavaFX headless e conferido nos temas claro/escuro e na largura mínima da janela. A verificação anterior do schema e dos comandos RLS usou PostgreSQL WASM/PGlite. Isso não substitui validação no Windows nem testes contra seu Supabase. O guia de [testes](docs/TESTING.md) registra detalhes e limites. Consulte [MELHORIAS.md](MELHORIAS.md) para localizar os recursos novos.

Evoluções pendentes: recuperação de senha esquecida, migrations versionadas, testes nativos PostgreSQL/MariaDB, compatibilidade física/BIOS, importação revisada de benchmarks, preços com fonte/data e recomendação por perfil de uso. O orçamento já filtra as configurações elegíveis por jogo; as preferências de peças do algoritmo permanecem documentadas nos requisitos.

## Licença

Projeto educacional, sem arquivo de licença explícito no pacote. O mantenedor deve definir a licença do código e avaliar direitos dos recursos antes de redistribuir publicamente.
