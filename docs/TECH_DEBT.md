# Débitos técnicos e decisões pendentes

Registro baseado na auditoria estática de 19/10/2026. A tabela registra a situação após a implementação de 21/10/2026. Os problemas originais são preservados como histórico, com o resultado na última coluna. P1: operação/segurança/manutenção necessária; P2: qualidade e consistência; P3: evolução opcional. Nenhum incidente P0 foi demonstrado.

| ID | Prioridade | Evidência e impacto | Próxima ação e aceite |
| --- | --- | --- | --- |
| DT-01 | P1 | `Main` usava operações globais sem login | **Resolvido na entrada do terminal:** login e ADMIN obrigatórios; cadastro compartilhado. Fronteira JDBC continua em DT-04 |
| DT-02 | P1 | Transferência omitia metadados novos de `jogo` | **Resolvido:** campos incluídos na definição compartilhada e regressão JDBC |
| DT-03 | P1 | Atualizador não reproduzia RLS/revogações | **Resolvido no código:** transação PostgreSQL e proteção das 11 tabelas; comandos verificados em PGlite. Aplicação no banco real é procedimento administrativo |
| DT-04 | P1 | Cliente recebe credencial JDBC e impõe autorização localmente | Definir público da distribuição; para clientes não confiáveis, aprovar arquitetura e implementar fronteira segura |
| DT-05 | P1 | Faltavam integração JDBC e recuperação | **Parcial:** H2 cobre DAOs, rollback, cópia e backup/restauração; servidores nativos e desastre permanecem pendentes |
| DT-06 | P2 | Histórico de migrations ausente e tipos de data divergentes | **Parcial:** transação global adicionada ao PostgreSQL; versionamento e diferença TIMESTAMP/TIMESTAMPTZ permanecem |
| DT-07 | P2 | Primeiro ADMIN por contagem e inserção separadas | Evitar disputa no provisionamento; reproduzir cadastro concorrente e validar correção |
| DT-08 | P2 | Scripts exigiam extensão Java e citavam JDK 17 | **Resolvido no código:** descoberta JAVA_HOME/PATH/extensão, JDK 21+, preferência Maven. Conferência no Windows pendente |
| DT-09 | P2 | Versões repetidas em POM e scripts; instalador auxiliar parcial | Reduzir divergências com escopo aprovado; testar dependências e pacote limpo |
| DT-10 | P2 | `Main` extenso, telas chamam DAOs, validações repetidas | **Parcial em 28/10:** navegação de builds separa dados/controles e `BuildsOperacoes` distingue persistência de recarregamento; outras páginas e terminal continuam pendentes |
| DT-11 | P2 | Modelo usa `double` para preços, SQL usa decimal | Política/tipos monetários seguem pendentes. Correção independente de 28/10: formatação da interface usa instância por thread, com regressão concorrente |
| DT-12 | P2 | Ausência de lint/CI e cobertura quantitativa | Adotar somente se necessário ao fluxo da equipe; começar com testes existentes |
| DT-13 | P2 | Cópia privada de fonte pode conter configuração e target | **Parcial:** novo exportador de fonte exclui ambientes privados e saídas sem apagar originais; revisar outros arquivos antes de publicação |
| DT-14 | P2 | Recuperação de senha/admin e retenção não definidas | **Parcial:** troca com senha atual implementada. Recuperação de senha esquecida e políticas de dados seguem pendentes |
| DT-15 | P3 | Sem licença explícita; artes têm fontes, não concessão de licença | Mantenedor decide licença do código e condições de redistribuição de recursos |

## Pendências de produto e operação

- **PENDENTE DE VALIDAÇÃO:** público definitivo e distribuição a terceiros.
- Terminal implementado como ferramenta administrativa autenticada desde a alteração registrada em 21/10/2026; operadores comuns usam a interface gráfica. Não é uma nova mudança de política nesta revisão documental.
- **PENDENTE DE VALIDAÇÃO:** ferramentas de IA que devem carregar instruções automaticamente. `AGENTS.md` permanece como fonte comum; este ZIP não inclui os pacotes de agentes especializados entregues separadamente, nem configuração do Copilot.
- **PENDENTE DE VALIDAÇÃO:** responsável por banco/backups, retenção e metas de recuperação.
- **PENDENTE DE VALIDAÇÃO:** metas mensuráveis de desempenho, disponibilidade e acessibilidade.

A falta de `.env.example`, as referências de execução do VS Code e a centralização documental foram tratadas nesta entrega. O documento histórico [ATUALIZACOES-CATALOGO.md](../ATUALIZACOES-CATALOGO.md) foi preservado; sua alegação de omissão de `target` não descreve o ZIP recebido. Responsáveis individuais e prazos deste backlog não foram atribuídos; o mantenedor deve priorizá-los.

ADRs históricos não foram criados: arquitetura observada está em [ARCHITECTURE.md](ARCHITECTURE.md), mas os motivos originais não foram confirmados. Quando houver decisão nova relevante, registrar contexto, alternativas, decisão e consequências, sem atribuir justificativas retrospectivas.

## Situação na revisão documental de 28/10/2026

Esta edição esclarece fontes, datas e pendências; não encerra novos débitos de código. Os resultados históricos de teste são mantidos em [TESTING.md](TESTING.md). O inventário e o relatório da atualização estão no [índice](README.md) e no [relatório documental](DOCUMENTATION_REPORT_2026-10-28.md).

O limite registrado na etapa documental levou à implementação subsequente de 28/10/2026: a navegação normal agora carrega os dados de builds em worker e cria os controles na thread FX. O construtor de conveniência segue síncrono; outras páginas do shell ainda precisam de revisão. Também foi corrigida a distinção entre gravação confirmada e falha na atualização da lista, evitando induzir repetição da gravação. Ver [ARCHITECTURE.md](ARCHITECTURE.md) e os resultados atuais em [TESTING.md](TESTING.md).
