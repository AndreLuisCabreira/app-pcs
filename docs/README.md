# Índice e manutenção da documentação

Esta é a entrada de `/docs`. O [README principal](../README.md) apresenta o aplicativo; os guias abaixo aprofundam cada assunto. Escopo desta edição: autorização de 28/10/2026 para modificar a documentação usando `/docs` como referência e seguir o prompt de implementação documental. Não se presume um novo plano de alteração de código.

## Fontes principais

| Assunto | Documento | Evidência principal |
| --- | --- | --- |
| Ambiente, configuração e comandos | [DEVELOPMENT.md](DEVELOPMENT.md) | [POM](../pom.xml), [carregador de configuração](../src/ConfiguracaoAplicacao.java) e `scripts/` |
| Componentes e fluxos | [ARCHITECTURE.md](ARCHITECTURE.md) | Inicialização, telas, serviços, DAOs e utilitários existentes |
| Comportamentos e critérios verificáveis | [REQUIREMENTS.md](REQUIREMENTS.md) | Regras nos serviços/telas; descrição observada, sem aprovação formal de produto |
| Dados, restrições e atualização | [DATA_MODEL.md](DATA_MODEL.md) | Schemas e atualizador; não representa inspeção do banco remoto |
| Controles de acesso e limites | [SECURITY.md](SECURITY.md) | Autenticação, autorização, SQL, configuração e empacotamento |
| Testes, comandos e resultados datados | [TESTING.md](TESTING.md) | Classes em `test/`, POM, scripts e registros históricos identificados |
| Backup e recuperação | [BACKUP_AND_RECOVERY.md](BACKUP_AND_RECOVERY.md) | Serviço de backup, transferência e limites operacionais |
| Débitos e decisões pendentes | [TECH_DEBT.md](TECH_DEBT.md) | Problemas documentados, sem autorização automática para implementá-los |

Guias especializados fora de `/docs`: [Supabase/JDBC](../SUPABASE.md), [distribuição Windows](../DISTRIBUICAO.md), [orientações de agentes](../AGENTS.md) e [histórico](../CHANGELOG.md). [MELHORIAS.md](../MELHORIAS.md) e [ATUALIZACOES-CATALOGO.md](../ATUALIZACOES-CATALOGO.md) preservam relatos datados. O [protótipo HTML](../figma-html/README.md) é independente da aplicação persistente.

## Matriz documental desta edição

Esta matriz registra o escopo adotado após o esclarecimento do usuário; não se apresenta como um relatório de auditoria anterior recuperado.

| Ação | Arquivos | Finalidade |
| --- | --- | --- |
| CRIAR | `docs/README.md` | Navegação, fontes e procedimento de manutenção |
| CRIAR | `docs/DOCUMENTATION_REPORT_2026-10-28.md` | Relatório da entrega e verificações reais |
| ATUALIZAR | `README.md`, `AGENTS.md` | Entrada e instruções alinhadas às fontes em `/docs` |
| COMPLETAR | `docs/ARCHITECTURE.md`, `docs/REQUIREMENTS.md` | Fluxo da montagem, limites das camadas e rastreabilidade de recursos já existentes |
| ATUALIZAR | `docs/DEVELOPMENT.md`, `docs/TESTING.md` | Comandos conferidos, testes presentes e datas/resultados históricos |
| ATUALIZAR | `docs/SECURITY.md`, `docs/TECH_DEBT.md` | Escopo da evidência, pendências explícitas e limites existentes |
| ATUALIZAR | `DISTRIBUICAO.md`, `CHANGELOG.md` | Distinguir verificações históricas da edição documental atual |
| MANTER | `docs/DATA_MODEL.md`, `docs/BACKUP_AND_RECOVERY.md`, `SUPABASE.md` | Conteúdo útil existente, sem mudanças de dados/contratos nesta tarefa |
| PRESERVAR COMO HISTÓRICO | `MELHORIAS.md`, `ATUALIZACOES-CATALOGO.md` e entradas anteriores do changelog | Preservar conhecimento acumulado e resultados com suas datas |
| NÃO CRIAR NESTA EDIÇÃO | API/OpenAPI, ADRs históricos, histórias de usuário novas, `CLAUDE.md` e instruções duplicadas do Copilot | Sem API própria/decisão comprovada/necessidade de carregamento confirmada |

## Regras para manter informações confiáveis

- Use o guia especializado como fonte principal e links relativos nos demais documentos; não copie o manual completo para o README ou `AGENTS.md`.
- Identifique fatos observados no código, registros históricos, propostas e decisões humanas. `PENDENTE DE VALIDAÇÃO:` marca informação que depende de confirmação do mantenedor, não uma funcionalidade prometida.
- Ao citar testes, informe data, comando/ferramenta, ambiente e limites conhecidos. Presença de `@Test` não é aprovação; relatório antigo não é nova execução; headless/H2 não comprovam Windows/banco nativo.
- Confira comandos no POM/scripts. Documentar um comando de migration/restauração não autoriza executá-lo.
- Não copie segredos, dados pessoais nem configuração privada para exemplos. Preserve os arquivos locais do usuário.
- Registre decisões arquiteturais relevantes somente quando confirmadas, com contexto, problema, alternativas conhecidas, decisão, justificativa, consequências, status e data quando conhecida. Não atribua motivos históricos sem evidência.
- Em alterações futuras, siga a matriz de documentação afetada do [AGENTS.md](../AGENTS.md) e confira links, caminhos, diff e preservação de arquivos antes de concluir.

O [relatório documental de 28/10/2026](DOCUMENTATION_REPORT_2026-10-28.md) preserva o escopo e o resultado daquela etapa, anterior ao pedido de alterar o código. A implementação subsequente de concorrência/persistência de builds está registrada no [changelog](../CHANGELOG.md), na [arquitetura](ARCHITECTURE.md) e na última seção de [testes](TESTING.md); não reescreve os resultados históricos do relatório documental.
