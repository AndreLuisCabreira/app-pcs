# Changelog

## Não lançado

### Montagem guiada — 2026-10-21

- Minhas builds separa configurações salvas e assistente de montagem em abas;
- uma categoria por etapa: CPU, placa-mãe, RAM, GPU, SSD, fonte e revisão;
- busca por etapa, retorno, navegação por etapas disponíveis e resumo com preço/potência;
- trocas mantêm escolhas compatíveis e avisam quando é necessário substituir peças dependentes;
- revisão com nome/favorita e troca individual; edição mantém a build existente e seu proprietário;
- dez testes novos; 64 testes aprovados e fluxo JavaFX exercitado em modo headless, nos temas claro/escuro e largura mínima;
- sem alteração de schema ou migration; a compilação/execução nativa no Windows continua pendente.

### Melhorias implementadas — 2026-10-21

- terminal exige login e perfil ADMIN; cadastro reaproveita validações e hashes do serviço gráfico;
- transferência MariaDB/PostgreSQL preserva gênero, descrição e URL de capa dos jogos;
- atualizador PostgreSQL envolve DDL e RLS/revogações em transação, mantendo bloqueio das roles públicas;
- Minha conta permite trocar senha com reautenticação e proteção contra sobrescrita concorrente;
- administrador exporta backup JSON e restaura dados com confirmação de senha, validação e rollback em falhas;
- comparação de duas/três builds próprias, exportação TXT/HTML e HTML de comparação imprimível em PDF;
- filtros de orçamento, resolução e FPS nas recomendações e de termos/faixa de preço no catálogo;
- scripts procuram JDK 21+ em JAVA_HOME/PATH, preferem Maven e disponibilizam exportação sanitizada de fonte;
- 24 testes novos, totalizando 54 aprovados; SQL RLS exercitado em PostgreSQL WASM e telas novas renderizadas em JavaFX headless;
- detalhes, limites e recursos pendentes em `MELHORIAS.md` e `docs/TESTING.md`. Nenhuma migration foi executada no banco do usuário.

### Documentação — 2026-10-19

- criada documentação de desenvolvimento, arquitetura, modelo de dados, requisitos/regras, segurança, testes, backup/recuperação e débitos técnicos em `docs/`;
- adicionados `AGENTS.md` com regras de manutenção e `.env.example` sem credenciais;
- README reorganizado como entrada para os guias, preservando o conhecimento útil em documentos por assunto;
- corrigidas referências de execução do VS Code e explicitados os pré-requisitos dos scripts PowerShell;
- documentadas diferenças de autorização do terminal, limites das migrations/RLS e campos de jogos omitidos na transferência; essas pendências não foram corrigidas no código;
- preservados os documentos históricos e identificados os resultados de testes anteriores como históricos; build, JUnit, banco e pacote Windows não foram executados nesta entrega.

### Catálogo de jogos e janela nativa

- registrados no histórico os cards verticais, busca por nome/gênero, nome no foco/hover, capas locais e troca administrativa de capa por URL;
- registrada a barra de título nativa, com controles e encaixe a cargo do sistema;
- preservado `ATUALIZACOES-CATALOGO.md` como relato da alteração anterior; a afirmação de ausência de `target/` se refere àquele relato e não descreve o ZIP recebido nesta revisão documental.

### Recomendações por jogo

- adicionada a aba Jogos para todos os usuários;
- criados cards com capa remota opcional e fallback visual automático;
- recomendações usam benchmarks reais de pelo menos 60 FPS em 1920x1080;
- configurações são completadas com placa-mãe, memória, SSD e fonte compatíveis;
- cadastro de jogos agora aceita gênero, descrição e URL da capa;
- incluída migration compatível com Supabase/PostgreSQL e MariaDB/MySQL.

### Ciclo de migrations

- removida a execução de alterações estruturais durante a abertura das interfaces JavaFX e de terminal;
- adicionada validação somente leitura da estrutura antes do login;
- criado `scripts/migrate-database.ps1` para aplicar migrations explicitamente;
- criado executor compartilhado para as ferramentas administrativas de banco;
- documentado o fluxo de atualização para desenvolvimento, Supabase e distribuição.

## 1.8.1 — 2026-09-02

### Desempenho com Supabase

- conexões JDBC agora são reutilizadas por um pool HikariCP pequeno;
- login, cadastro, migração inicial e troca de telas não bloqueiam mais a thread do JavaFX;
- adicionados indicadores visuais durante carregamentos remotos;
- conexões PostgreSQL usam TCP keepalive e são encerradas ao fechar o aplicativo;
- incluído diagnóstico reproduzível de latência do banco.

## 1.8.0 — 2026-09-02

### Distribuição para Windows

- adicionada build portátil com `jpackage`, Java 21 e JavaFX incluídos;
- criado download verificável do Eclipse Temurin JDK 21 para empacotamento;
- configuração empacotada procura o `.env` ao lado do executável;
- o `.env` real é excluído da distribuição e validado antes da entrega;
- adicionado guia para executar o aplicativo em outro computador.

## 1.7.0 — 2026-09-01

### Supabase

- adicionada conexão automática com PostgreSQL/Supabase a partir de `DB_URL`;
- incluído o driver pgJDBC 42.7.13 sem remover a compatibilidade com MariaDB;
- criado o esquema idempotente `database/supabase.sql` para o SQL Editor;
- adicionadas instruções de Session pooler, SSL e configuração segura no `SUPABASE.md`;
- tabelas do Supabase usam RLS e não ficam expostas às roles públicas da Data API;
- chaves `sb_secret_...` não são armazenadas nem incorporadas ao aplicativo desktop.

## 1.6.0 — 2026-09-01

### PC Part Dataset

- removida a integração e a configuração de chave da Best Buy;
- categorias agora são carregadas diretamente dos JSONs públicos de `docyx/pc-part-dataset`;
- adicionados cache em memória, atualização manual e filtro local da lista;
- CPU, GPU, fonte, memória e SSD são normalizados conforme os campos documentados pelo dataset;
- campos que não existem na fonte são identificados e exigidos antes da importação;
- identificadores determinísticos impedem importar o mesmo item repetidamente.

## 1.5.0 — 2026-09-01

### Catálogo externo

- adicionada integração administrativa com a Best Buy Products API;
- busca de CPU, GPU, fontes, memórias e SSDs sem bloquear a interface;
- revisão obrigatória dos dados técnicos e do preço local antes da importação;
- importações são transacionais e SKUs duplicados são bloqueados por categoria;
- adicionada leitura segura de `BESTBUY_API_KEY` pelo `.env` ou ambiente;
- adicionados testes do parser JSON e da normalização de unidades.

## 1.4.0 — 2026-09-01

### Interface

- removidos os números dos itens da navegação lateral;
- removido o indicador de investimento do dashboard;
- removida a faixa superior redundante e consolidada a identidade da conta na barra lateral.

### Benchmarks

- substituída a fórmula heurística de FPS por medições reais cadastradas;
- adicionadas resolução, qualidade gráfica, FPS médio, 1% low, fonte e observações;
- a análise usa medições exatas ou a mesma GPU com a CPU de referência mais próxima;
- resultados sem benchmark compatível agora aparecem como sem dados, sem valor inventado;
- adicionada tela administrativa para cadastrar jogos e medições.

### Acesso

- adicionados perfis de administrador e usuário;
- somente administradores podem manter componentes e benchmarks.

## 1.3.0 — 2026-09-01

### Autenticação

- adicionada tela inicial com login e criação de conta;
- cadastro normaliza o login, valida confirmação e exige senha com pelo menos 8 caracteres;
- sessão mantém apenas os dados públicos do usuário, sem o hash da senha;
- adicionada opção de sair e retornar à autenticação.

### Privacidade

- dashboard, builds, análises e favoritas são filtrados pelo usuário autenticado;
- atualização e exclusão de builds validam a propriedade também na consulta SQL;
- removida da montagem a possibilidade de escolher outro proprietário.

### Testes

- adicionados testes de credenciais válidas e inválidas, cadastro seguro, login duplicado e normalização.

## 1.2.0 — 2026-09-01

### Interface

- adicionada aplicação desktop em JavaFX com navegação lateral e tema responsivo;
- criado dashboard conectado ao banco com indicadores e builds cadastradas;
- adicionadas telas para usuários, seis categorias de componentes, builds e análises;
- formulários e seletores aplicam as regras de compatibilidade já existentes;
- configuração do VS Code passa a iniciar `DesktopLauncher` pelo F5.

### Build e documentação

- adicionados JavaFX Controls e plugin JavaFX ao Maven;
- script local de compilação inclui módulos JavaFX e recursos visuais;
- README atualizado com o novo fluxo de execução.

## 1.1.0 — 2026-09-01

### Segurança

- removidas as credenciais MySQL fixas do código;
- adicionada configuração por `DB_URL`, `DB_USER` e `DB_PASSWORD`;
- senhas de usuários agora são protegidas com PBKDF2-HMAC-SHA-256 e salt aleatório.

### Correções

- atualização de builds agora rejeita memória incompatível;
- criação e atualização rejeitam fonte abaixo da potência recomendada;
- falhas de banco não são mais tratadas como operações bem-sucedidas;
- estimativa de FPS agora consegue produzir valores acima de 85 e respeita o intervalo 15–300;
- listagem de builds deixou de executar seis consultas adicionais por registro.

### Recursos

- cálculo e exibição do preço total da build;
- esquema MySQL reproduzível;
- build Maven e testes unitários.

### Documentação

- README alinhado ao estado real do MVP e às suas limitações.
