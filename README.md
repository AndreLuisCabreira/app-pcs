# PC Builder — IntraTech

Aplicação desktop em JavaFX para cadastrar componentes, montar computadores, verificar compatibilidade e comparar custo, consumo e benchmarks de jogos.

## Funcionalidades

- login e cadastro de contas com senha protegida por PBKDF2;
- dashboard individual com as builds e favoritas da conta autenticada;
- catálogo de processadores, placas-mãe, placas de vídeo, memórias, SSDs e fontes;
- criação, edição e exclusão de builds isoladas por usuário;
- filtragem de placa-mãe por socket e de memória por geração DDR;
- validação de socket, memória e capacidade da fonte;
- cálculo do preço e consumo total;
- análise de FPS baseada em medições reais por jogo, resolução e qualidade;
- galeria de jogos com recomendações de configuração para 1080p a 60 FPS;
- cadastro administrativo de jogos e benchmarks com FPS médio, 1% low e fonte;
- listagem e importação administrativa pelo dataset público `docyx/pc-part-dataset`;
- persistência em Supabase/PostgreSQL, MariaDB ou MySQL com consultas parametrizadas.

## Limitações conhecidas

- preços são cadastrados manualmente e não são cotações em tempo real;
- os preços do dataset estão em dólar, foram atualizados em julho de 2025 e não são cotações atuais;
- CPU não traz socket/threads, GPU não traz consumo e SSD não traz leitura/escrita;
- a referência de FPS depende da quantidade e da qualidade dos benchmarks cadastrados;
- ainda não há recuperação de senha;
- a compatibilidade não cobre dimensões físicas, BIOS, conectores ou lanes PCIe.

## Tecnologias

- Java 21;
- JavaFX 21;
- Maven 3.9+;
- Supabase/PostgreSQL, MariaDB ou MySQL;
- JDBC / pgJDBC e MySQL Connector/J;
- HikariCP para reutilização eficiente das conexões remotas;
- Jackson 2.21;
- JUnit 5.

## Estrutura

```text
src/
├── DesktopLauncher.java       ponto de entrada da interface
├── DesktopApp.java            configuração da janela JavaFX
├── AutenticacaoView.java      tela de login e criação de conta
├── AutenticacaoService.java   validação de credenciais e cadastro
├── Main.java                  interface de terminal mantida como alternativa
├── *View.java                 telas da aplicação JavaFX
├── AppShell.java              navegação e estrutura visual
├── ui/                        recursos de marca e utilitários visuais
├── *DAO.java                  persistência JDBC
├── ConnectionFactory.java     configuração e abertura da conexão
├── DatabaseSchemaValidator.java verificação somente leitura na abertura
├── DatabaseMigrationLauncher.java comando administrativo de migration
├── model/                     entidades do domínio
└── service/                   compatibilidade, consumo, FPS e senhas
resources/styles.css           tema da aplicação
test/                          testes unitários
database/schema.sql            tabelas, jogos e estrutura de benchmarks
database/supabase.sql          esquema PostgreSQL para o SQL Editor
scripts/migrate-database.ps1   atualização explícita de bancos existentes
```

O projeto usa uma separação simples entre interface, modelos, DAOs e serviços. As classes ainda estão no pacote padrão para preservar compatibilidade com a base acadêmica original.

## Configuração inicial

### Supabase

Para usar o banco em nuvem, siga o guia [SUPABASE.md](SUPABASE.md) e execute
[`database/supabase.sql`](database/supabase.sql) no SQL Editor. A aplicação usa
a conexão PostgreSQL do **Session pooler**; as API keys do Supabase não são
gravadas no aplicativo.

### 1. Pré-requisitos

Confirme as versões instaladas:

```powershell
java -version
mvn -version
mysql --version
```

Use Java 21 ou superior.

### 2. Criar o banco

Execute [`database/schema.sql`](database/schema.sql) no HeidiSQL ou no cliente de sua preferência.

Em um ambiente real, crie um usuário exclusivo para a aplicação:

```sql
CREATE USER 'pcbuilder'@'localhost' IDENTIFIED BY 'uma-senha-forte';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON pcbuilder.* TO 'pcbuilder'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurar o banco

No PowerShell, para a sessão atual:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/pcbuilder?useSSL=false&serverTimezone=UTC'
$env:DB_USER = 'pcbuilder'
$env:DB_PASSWORD = 'uma-senha-forte'
$env:DB_ALLOW_EMPTY_PASSWORD = 'false'
```

Para o MariaDB local descrito no projeto, com `root` sem senha:

```powershell
$env:DB_USER = 'root'
$env:DB_PASSWORD = ''
$env:DB_ALLOW_EMPTY_PASSWORD = 'true'
```

O aplicativo lê o arquivo `.env` da pasta `Montagem-PCs`, mantendo as variáveis do sistema como prioridade. A configuração de execução do VS Code já define as variáveis do banco para o ambiente local. Não envie o `.env` para o Git; ele já está ignorado pelo projeto.

### 4. Preparar ou atualizar a estrutura

Em um banco novo, execute `database/schema.sql` no MariaDB/MySQL ou
`database/supabase.sql` no SQL Editor do Supabase.

Ao atualizar uma instalação existente, execute uma vez:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\migrate-database.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\check-database.ps1
```

Esse é um procedimento administrativo. A abertura normal do PC Builder apenas
verifica a estrutura, sem executar `CREATE`, `ALTER` ou atualizações de
manutenção. Se algo estiver ausente, o aplicativo informa que a migration deve
ser executada e não abre uma sessão sobre um esquema incompleto.

## Executar

### Build portátil para outro PC

Para gerar uma versão Windows 64 bits com Java e JavaFX incluídos:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

O ZIP será criado em `dist\IntraTech-PC-Builder-1.8.1-windows-x64.zip`. O arquivo `.env` real não é incluído. Consulte [DISTRIBUICAO.md](DISTRIBUICAO.md) para configurar e executar o aplicativo no computador de destino.

### VS Code

1. Abra a pasta `projeto` ou `Montagem-PCs`.
2. Pressione `F5`.
3. Selecione **Executar PC Builder (configuração .env)**.

Ao abrir a pasta pai `projeto`, o VS Code executa primeiro o script de compilação e inicia `DesktopLauncher` com JavaFX e o driver do banco no classpath.

Na primeira execução de um banco vazio, use a aba **Criar conta**. A primeira conta recebe o perfil de administrador. Depois do login, a aplicação mostra somente as builds e favoritas vinculadas àquela conta.

### Importar componentes

Somente administradores veem a opção **Importar catálogo**:

1. escolha CPU, GPU, fonte, RAM ou SSD;
2. aguarde o carregamento direto da lista pública;
3. filtre localmente por nome, fabricante ou especificação;
4. selecione um resultado;
5. revise todos os campos técnicos;
6. informe o preço local em reais;
7. clique em **Importar componente**.

A categoria é baixada de `raw.githubusercontent.com` em segundo plano e mantida em memória enquanto o programa estiver aberto. Não há chave, cadastro ou URL para configurar. O componente só é salvo após revisão e permanece disponível sem internet. A tabela `componente_importado` registra a origem e impede a importação repetida do mesmo item na mesma categoria.

### Maven

Com as variáveis de banco definidas:

```powershell
mvn clean test
mvn javafx:run
```

Também é possível iniciar com:

```powershell
mvn exec:java
```

Para usar a interface antiga de terminal:

```powershell
mvn exec:java -Dexec.mainClass=Main
```

## Regras de negócio

### Compatibilidade

- socket da CPU deve ser igual ao socket da placa-mãe;
- tipo da RAM deve ser igual ao tipo aceito pela placa-mãe;
- potência da fonte deve ser igual ou superior ao consumo recomendado.

### Consumo

```text
consumo = CPU + placa-mãe + GPU + 5 W da memória + 5 W do SSD
fonte recomendada = teto(consumo × 1,20)
```

### FPS

O sistema não transforma índices arbitrários em FPS. A análise procura medições cadastradas para o mesmo jogo, resolução e qualidade:

- com a mesma CPU e GPU, mostra a média das medições exatas;
- sem a mesma CPU, usa medições da mesma GPU com o processador de índice mais próximo e identifica o resultado como **referência próxima**;
- sem medição da mesma GPU, mostra **Sem dados** em vez de inventar um valor.

O administrador cadastra essas medições na tela **Benchmarks**, informando jogo, componentes testados, resolução, qualidade, FPS médio, 1% low opcional e a fonte. Para comparações coerentes, registre se o teste usou ray tracing ou upscaling no campo de observações.

Na tela **Jogos**, cada card abre a configuração completa de menor custo que utiliza uma medição real de pelo menos 60 FPS em 1920×1080. CPU e GPU vêm do benchmark; placa-mãe, memória, SSD e fonte são completados com peças compatíveis do catálogo. Jogos sem medição adequada exibem **Sem dados**, sem estimar FPS artificialmente. O cadastro administrativo aceita gênero, descrição e uma URL pública opcional de capa; sem imagem, a interface usa um monograma automático.

## Segurança

- credenciais não ficam no código-fonte;
- senha vazia exige `DB_ALLOW_EMPTY_PASSWORD=true`;
- senhas cadastradas usam PBKDF2-HMAC-SHA-256 com salt aleatório;
- o hash da senha não permanece no objeto de sessão;
- mensagens de login inválido não revelam se a conta existe;
- consultas de builds na interface exigem o ID do usuário autenticado;
- comandos SQL usam `PreparedStatement`;
- a importação usa transação e bloqueia itens duplicados por categoria.

Contas muito antigas que tenham senha salva em texto puro não são aceitas pelo login. Crie uma nova conta pela tela de cadastro antes de remover esses registros antigos.

## Testes

Os testes unitários cobrem autenticação, perfis, proteção de senha, compatibilidade, builds incompletas, consumo, potência recomendada, seleção de benchmarks e preço total. Testes de integração dos DAOs com banco descartável continuam no roadmap.

## Roadmap

- recuperação e alteração de senha;
- migrations versionadas;
- testes de integração com banco descartável;
- compatibilidade física e de BIOS;
- importação automatizada de benchmarks com revisão da fonte;
- importação de preços com fonte e data visíveis;
- recomendações por orçamento e perfil de uso.

## Licença

Projeto educacional. Antes de redistribuir, adicione um arquivo de licença explícito ao repositório.
