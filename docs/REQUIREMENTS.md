# Funcionalidades, regras e critérios de aceite

Este documento consolida comportamento observado a partir de 19/10/2026, com complementos de 21/10 e reconferência documental dos arquivos pertinentes em 28/10/2026. Os identificadores RF são uma rastreabilidade do comportamento existente, não requisitos formalmente aprovados de produto. Os critérios são verificáveis, mas não declaram execução de testes nesta revisão. Público inferido: pessoas que montam/comparam PCs e administradores do catálogo. **PENDENTE DE VALIDAÇÃO:** objetivos comerciais e público definitivo.

## Funcionalidades e rastreabilidade

| ID | Necessidade e comportamento observado | Fontes | Critério verificável |
| --- | --- | --- | --- |
| RF-01 | Cadastrar e autenticar conta na interface gráfica | `AutenticacaoService`, `AutenticacaoView` | Normalizar login; rejeitar duplicado/senha inválida; retornar sessão sem hash |
| RF-02 | Restringir manutenção de catálogo a administrador na interface gráfica | `AppShell`, `AutorizacaoService`, telas administrativas | Usuário comum não vê entradas administrativas e validações de perfil rejeitam acesso |
| RF-03 | Gerenciar builds e favoritas próprias na interface gráfica | `BuildsView`, `BuildDAO`, `DashboardView` | Listagem filtra proprietário; atualização/exclusão usam ID de build e de usuário |
| RF-04 | Montar build com componentes compatíveis, uma etapa por vez | `BuildsView`, `MontagemWizardView`, `MontagemPC`, `CompatibilidadeService`, `ConsumoService` | Avançar após escolher; voltar/trocar preserva escolhas válidas; revisão final rejeita peças ausentes, socket/DDR incompatíveis e fonte insuficiente |
| RF-05 | Consultar consumo, preço e FPS | `AnalisesView`, `Build`, serviços | Exibir cálculos conforme regras abaixo; ausência de benchmark não gera FPS fictício |
| RF-06 | Consultar jogos e recomendações | `JogosView`, `RecomendacaoJogoService`, `ui/GameCatalog` | Filtrar por nome/gênero; abrir detalhes; exibir indisponibilidade quando não houver configuração elegível |
| RF-07 | Cadastrar jogos e medições | `BenchmarksView`, `JogoDAO`, `BenchmarkFPSDAO` | Administrador informa cenário, FPS e fonte; 1% low é opcional e validado |
| RF-08 | Importar componentes após revisão | `ImportacaoCatalogoView`, `PcPartDatasetClient`, `CatalogoImportacaoDAO` | Selecionar item, completar dados, informar preço local e rejeitar duplicidade por tipo/origem/ID |
| RF-09 | Trocar senha com reautenticação | `ContaView`, `AutenticacaoService`, `UsuarioDAO` | Exigir senha atual/confirmada e rejeitar sobrescrita quando o hash anterior mudou |
| RF-10 | Fazer backup e restaurar dados por ADMIN | `ContaView`, `DatabaseBackupService` | Revalidar perfil/senha; validar arquivo e conta recuperável; reverter falha de integridade em transação |
| RF-11 | Comparar e exportar builds próprias | `BuildDialogs`, `service/BuildExportService` | Comparar duas/três builds da conta; exportar TXT/HTML individual ou HTML comparativo, com texto escapado |
| RF-12 | Filtrar catálogo e recomendações | `ComponentesView`, `service/CatalogoFiltro`, `JogosView`, `RecomendacaoJogoService` | Combinar termos/faixa de preço no catálogo e respeitar orçamento/resolução/FPS nas configurações elegíveis |

## Contas e acesso

No cadastro gráfico, nome deve ter pelo menos dois caracteres; login é aparado, convertido a minúsculas e exige pelo menos três caracteres, com letras, números, ponto, hífen ou sublinhado. Senha exige oito caracteres e confirmação igual. A primeira conta recebe `ADMIN` quando a contagem de usuários é zero; contas posteriores recebem `USUARIO`, mesmo se não restar administrador. Login inválido tem mensagem genérica. A troca exige sessão, senha atual correta, nova senha com oito caracteres e confirmação; rejeita reutilização da senha atual. O UPDATE condiciona ID e hash anterior para não sobrescrever uma troca concorrente. Não há recuperação de senha esquecida.

O terminal usa o mesmo serviço de cadastro/login e só abre o menu global para ADMIN. As operações antigas de montagem ainda mantêm validações próprias; usuário comum usa o fluxo gráfico. As fronteiras estão em [SECURITY.md](SECURITY.md).

## Builds, consumo e preço

Uma build persistida referencia CPU, placa-mãe, GPU, memória, SSD e fonte. O salvamento gráfico exige seleção desses componentes e nome. `CompatibilidadeService.verificarBuild` verifica socket CPU/placa-mãe e tipo de memória, ignorando maiúsculas/minúsculas. A fonte é verificada separadamente por `ConsumoService`.

```text
consumo = consumo CPU + consumo placa-mãe + consumo GPU
          + 5 W se há memória + 5 W se há SSD
fonte mínima recomendada = teto(consumo × 1,20)
preço total = soma dos preços dos seis componentes presentes
```

O modelo trata peças ausentes como zero no preço e o serviço de consumo soma somente peças presentes. Isso não autoriza persistir builds incompletas. Preços usam `double` no modelo; não há cotação em tempo real. Compatibilidade não cobre BIOS, dimensões, conectores ou lanes PCIe. Não há garantia de que um DAO chamado isoladamente imponha todas as validações da tela.

## Montagem guiada na interface gráfica

Em **Minhas builds**, a lista de configurações salvas e a montagem ficam em abas separadas. O assistente segue CPU, placa-mãe, RAM, GPU, SSD, fonte e revisão. A lista central mostra uma categoria por vez, com busca textual e preço; a lateral mantém as seleções, o preço parcial e a potência recomendada. Não permite pular uma escolha anterior incompleta. Voltar e navegar pelas etapas disponíveis mantém o rascunho.

Uma troca preserva as peças que continuam válidas: CPU de outro socket remove placa-mãe e RAM dependente; outra placa-mãe remove apenas RAM de geração incompatível; consumo maior remove fonte insuficiente. O aviso identifica o que precisa ser escolhido novamente. Busca sem resultados não apaga a seleção.

Revisão permite trocar cada componente, informar nome de até 120 caracteres e marcar favorita. Editar carrega uma cópia da build e mantém ID/proprietário; uma build antiga inválida abre na primeira escolha que precisa de correção. Somente salvar persiste as alterações. Alternar abas mantém o rascunho enquanto a tela existir; abrir outra montagem pede confirmação se houve mudanças. Fechar/sair da tela não oferece persistência de rascunho. O banco existente continua compatível, sem migration. Na navegação normal, o carregamento inicial ocorre em segundo plano e a construção dos controles na thread FX. Gravação confirmada com falha apenas no recarregamento não é apresentada como gravação malsucedida: a tela limpa o rascunho salvo e oferece Atualizar lista. Se a gravação falhar, o rascunho é mantido. Atualização manual, gravação e exclusão não podem concorrer pelas abas desta tela.

## FPS de uma build

A análise filtra jogo, resolução e qualidade. Havendo mesma CPU/GPU, calcula média arredondada das medições. Na ausência da mesma CPU, procura a mesma GPU e o processador com índice de desempenho mais próximo, usando as medições dessa CPU como referência próxima. Sem GPU compatível no cenário, retorna sem dados. O 1% low é a média das medições que o informam; fontes distintas são reunidas. Ray tracing/upscaling devem ser registrados em observações para comparação; o algoritmo não interpreta essas observações como filtros estruturados.

## Recomendações por jogo

A tela permite selecionar `1920x1080`, `2560x1440` ou `3840x2160`, informar FPS mínimo positivo e orçamento máximo positivo; orçamento vazio significa sem limite. O padrão continua 1920x1080/60 FPS. Somente benchmarks que atendam à resolução e meta de FPS são candidatos, e configurações acima do orçamento são excluídas. CPU/GPU vêm da medição. O serviço prefere o SSD mais barato entre os de pelo menos 500 GB e, na ausência, o mais barato disponível. Para cada placa-mãe de socket compatível, prefere RAM da geração correta com pelo menos 16 GB; se não houver, aceita capacidade menor. Escolhe fonte suficiente e compara preços das configurações elegíveis.

A seleção final ordena por menor preço; em empate, maior nível de qualidade; persistindo empate, menor FPS entre os candidatos. Portanto, “mais barata” se refere às escolhas e preferências do algoritmo, não a uma otimização irrestrita de todas as combinações. Sem candidato completo, mostra indisponibilidade. A recomendação não mede o computador do usuário.

## Catálogo e imagens

A manutenção permite cadastro/edição de componentes. Na importação, CPU não fornece socket/threads, GPU não fornece consumo e SSD não fornece leitura/escrita; o operador completa os campos. O README anterior declara referência de preços do dataset de julho de 2025; a atualidade dessa fonte não foi verificada. Valores externos são USD; o usuário revisa preço em reais.

Importação usa transação e unicidade por categoria/origem/ID externo. O cache externo pertence à instância do cliente; não se deve prometer retenção após reinício ou recriação da tela. Peças já importadas permanecem no banco, ainda necessário para acessá-las.

Capas cadastradas por URL têm prioridade; sem URL, o catálogo tenta recursos locais dos três jogos iniciais; sem imagem utilizável, mantém o nome do jogo. Cards oferecem foco por teclado, nome no foco/hover e busca normalizada sem acentos. A validação admite HTTP/HTTPS, sem conversão WebP implementada.

## Limitações e decisões pendentes

Não foram identificadas metas aprovadas de latência, disponibilidade, volume, retenção ou recuperação. Há recursos de acessibilidade pontuais, sem certificação de conformidade. A política implementada do terminal já exige ADMIN; ela não está pendente como comportamento do código. **PENDENTE DE VALIDAÇÃO:** público da distribuição, responsável por backups, metas operacionais e eventual adoção de outras ferramentas de IA. Evoluções propostas ficam em [TECH_DEBT.md](TECH_DEBT.md) e no roadmap do [README](../README.md); não são funcionalidades já implementadas. Não foram criadas histórias de usuário adicionais sem validação de produto.

## Comparação, exportação e filtros

Em **Minhas builds**, Ctrl/Shift permite selecionar duas ou três builds para comparar peças, preço, consumo, fonte recomendada e diferença para a mais barata. O diálogo e o exportador rejeitam proprietário diferente. A comparação não calcula FPS; a tela Análises continua responsável por isso.

A exportação individual oferece TXT e HTML, e a comparação oferece HTML. O HTML é independente de recursos externos, escapa conteúdo textual e pode ser impresso em PDF pelo navegador; não há geração nativa de PDF ou imagem. Não inclui login, senha ou hash da conta.

O catálogo administrativo combina termos de nome/especificação/fabricante e preço mínimo/máximo inclusivos, ignorando acentos/caixa. Termos separados por espaço devem corresponder todos. Após salvar, a lista conserva seus filtros. Valores numéricos não finitos são rejeitados nos formulários que usam `UiSupport.decimal`.

## Backup e restauração

Administrador acessa **Minha conta → Backup administrativo** e confirma a senha atual. A exportação mantém as 11 tabelas e metadados dos jogos em JSON versão 1. A restauração confirma substituição integral, valida formato e conta administradora recuperável, exige suporte transacional (InnoDB em MySQL/MariaDB), executa as alterações em transação e encerra a sessão após sucesso. Falhas de integridade provocam rollback. Arquivos acima de 64 MiB são rejeitados. Operar sem gravações concorrentes e guardar o arquivo privado; mais detalhes em [BACKUP_AND_RECOVERY.md](BACKUP_AND_RECOVERY.md).
