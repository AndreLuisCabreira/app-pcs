# Orientações para agentes — IntraTech

## Antes de editar

Leia o [README](README.md), os documentos pertinentes em [docs](docs/ARCHITECTURE.md) e o estado atual dos arquivos. Confira `git status --short` e o diff; preserve mudanças preexistentes. A fonte principal da arquitetura é [ARCHITECTURE.md](docs/ARCHITECTURE.md), do ambiente é [DEVELOPMENT.md](docs/DEVELOPMENT.md) e das regras é [REQUIREMENTS.md](docs/REQUIREMENTS.md).

O [índice de documentação](docs/README.md) orienta a escolha das fontes. Os requisitos descrevem comportamento observado, não aprovação formal de produto. Não transforme pendências, hipóteses ou resultados históricos em funcionalidades/validações atuais. Quando uma informação depender do mantenedor, registre `PENDENTE DE VALIDAÇÃO:` e a decisão necessária.

## Convenções observadas

- Java 21 e JavaFX; Maven com `src/`, `test/` e `resources/` personalizados.
- Classes de domínio em `model`, serviços puros em `service`, utilitários visuais em `ui`; várias classes permanecem no pacote padrão. Não reorganize pacotes como efeito colateral.
- Nomes de domínio e mensagens em português; nomes históricos em inglês também existem. Siga o arquivo alterado e evite renomeações amplas.
- Reutilize serviços, DAOs e utilitários. Evite duplicar cálculos nas telas.
- Preserve SQL parametrizado para valores, fechamento de recursos e transações existentes.
- Em JavaFX, operações de rede/banco devem usar os mecanismos de trabalho em segundo plano existentes; alterações da interface precisam respeitar a thread JavaFX. Não trate o comportamento atual como prova de correção de concorrência.
- Mantenha preços e FPS associados às fontes disponíveis; ausência de dados deve ser identificada.

## Escopo, validação e segurança

Implemente o pedido autorizado, sem trocar tecnologias, instalar ferramentas ou refatorar áreas não relacionadas. Não exponha `.env`, `.env.local`, hashes de senha ou credenciais em mensagens, exemplos e logs. Consulte [SECURITY.md](docs/SECURITY.md) antes de alterar acesso ou persistência.

Execute verificações proporcionais conforme [TESTING.md](docs/TESTING.md). Informe comando, resultado e limitações; nunca apresente um resultado histórico como teste executado agora. Migrações, mudanças de dados, distribuição pública e operações destrutivas exigem autorização aplicável à tarefa. Uma autorização já concedida não precisa ser solicitada novamente.

Em tarefa exclusivamente documental, confira comandos por leitura do POM/scripts e valide caminhos, links e diff; não execute build, aplicação ou ferramenta administrativa como efeito colateral. Não declare cobertura sem medição nem testes aprovados sem execução atual. Preservar o código inclui testes, SQL, scripts e configurações.

## Documentação viva e conclusão

| Mudança | Fonte a revisar |
| --- | --- |
| Funcionalidade ou regra | `docs/REQUIREMENTS.md` e cenários em `docs/TESTING.md` |
| Schema, constraints ou migration | `docs/DATA_MODEL.md`, `SUPABASE.md` e recuperação, se afetada |
| Ambiente, dependência ou comando | `docs/DEVELOPMENT.md`, `.env.example` e guias afetados |
| Acesso ou credenciais | `docs/SECURITY.md` |
| Empacotamento | `DISTRIBUICAO.md` |
| Alteração relevante ao usuário | seção não lançada de `CHANGELOG.md` |

Mantenha uma fonte principal por informação; use links relativos. Não crie documentos vazios. Justificativas históricas desconhecidas ficam pendentes. Para uma nova decisão arquitetural relevante e confirmada, registre em `docs/adr/` contexto, decisão, alternativas e consequências; não invente decisões retroativas.

Uma entrega está concluída quando atende ao pedido, preserva trabalhos existentes, atualiza documentos afetados, verifica caminhos/links e informa validações reais e pendências. O responsável pela alteração atualiza a documentação; o mantenedor revisa. Não duplique estas regras em `CLAUDE.md` sem necessidade comprovada de carregamento pela ferramenta utilizada.
