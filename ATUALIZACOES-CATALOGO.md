# Catálogo de jogos e janela do Windows

## Alterações

- Capas verticais em uma grade que reorganiza as colunas conforme a largura disponível.
- Nome sobre a parte superior da capa ao passar o mouse ou focar o card pelo teclado.
- FPS, qualidade gráfica, resolução e uma descrição curta abaixo da imagem.
- Busca por nome ou gênero, ignorando acentos e diferenças entre maiúsculas e minúsculas.
- Clique no card abre os detalhes da configuração recomendada.
- Administradores continuam podendo trocar a capa por URL.
- Capas locais para Counter-Strike 2, Cyberpunk 2077 e Fortnite. A URL cadastrada tem prioridade. Jogos sem imagem usam o nome como alternativa.
- Barra de título nativa: controles, arraste, redimensionamento e encaixe ficam a cargo do Windows. As dimensões e a aparência dos botões acompanham as configurações de tema e escala do sistema.

Os FPS continuam vindo dos benchmarks cadastrados e da configuração recomendada em 1080p; não representam uma medição do computador em uso. Nenhum valor de desempenho foi inventado ou inserido no banco. Nenhuma migração de banco é necessária para esta alteração.

## Executar

Use JDK 21 e as dependências descritas no README original. Na raiz do projeto:

```powershell
mvn clean test
mvn javafx:run
```

Também é possível usar os scripts originais de compilação e empacotamento para Windows. O ZIP contém o projeto fonte; recompile para atualizar o aplicativo instalado. Os arquivos antigos de target foram omitidos para evitar executar classes anteriores à alteração.

## Validação realizada

- Todos os fontes compilados com JDK 21 e JavaFX 21.0.8.
- 30 testes existentes executados, todos passaram.
- Catálogo renderizado e conferido nos temas claro e escuro em JavaFX sem tela.
- Busca por nome/gênero, resultado vazio, foco por teclado e alternativa sem imagem conferidos.
- O encaixe nativo e o comportamento dos botões precisam de conferência no Windows; o ambiente de validação é Linux.

## Conferência no Windows

1. Arraste a barra para o topo: a janela deve maximizar e continuar mostrando a barra de tarefas.
2. Arraste para uma lateral e use Win + setas para conferir o encaixe.
3. Confira duplo clique na barra, minimizar, maximizar/restaurar e fechar.
4. Na aba Jogos, passe o mouse pelas capas, busque um título e abra uma configuração.
