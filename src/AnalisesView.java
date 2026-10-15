import ui.UiSupport;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.BenchmarkFPS;
import model.Build;
import model.Jogo;
import model.ResultadoFPS;
import model.Usuario;
import service.CompatibilidadeService;
import service.ConsumoService;
import service.FPSService;

import java.util.List;

public class AnalisesView extends VBox {

    private final Usuario usuarioAtual;
    private final ComboBox<Build> buildCombo = new ComboBox<>();
    private final ComboBox<Jogo> jogoCombo = new ComboBox<>();
    private final ComboBox<String> resolucaoCombo = new ComboBox<>();
    private final ComboBox<String> qualidadeCombo = new ComboBox<>();
    private final VBox result = new VBox(18);
    private final CompatibilidadeService compatibilidade = new CompatibilidadeService();
    private final ConsumoService consumo = new ConsumoService();
    private final FPSService fps = new FPSService();

    public AnalisesView(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        UiSupport.configurarCombo(buildCombo, b -> b.getId() + " — " + b.getNome());
        UiSupport.configurarCombo(jogoCombo, Jogo::getNome);
        resolucaoCombo.setItems(FXCollections.observableArrayList(BenchmarksView.RESOLUCOES));
        qualidadeCombo.setItems(FXCollections.observableArrayList(BenchmarksView.QUALIDADES));
        resolucaoCombo.setMaxWidth(Double.MAX_VALUE);
        qualidadeCombo.setMaxWidth(Double.MAX_VALUE);
        resolucaoCombo.getSelectionModel().selectFirst();
        qualidadeCombo.getSelectionModel().select("Alto");
        carregarDados();

        Button analyze = new Button("Analisar configuração");
        analyze.getStyleClass().add("primary-button");
        analyze.setOnAction(event -> analisar(analyze));
        Button refresh = new Button("Recarregar dados");
        refresh.getStyleClass().add("secondary-button");
        refresh.setOnAction(event -> UiSupport.executarComFeedback(
                refresh,
                "Recarregando...",
                this::buscarDados,
                this::aplicarDados
        ));

        HBox actions = new HBox(10, analyze, refresh);
        VBox selector = new VBox(
                14,
                label("Selecione uma configuração"),
                UiSupport.campo("BUILD", buildCombo),
                UiSupport.campo("JOGO DE REFERÊNCIA", jogoCombo),
                UiSupport.campo("RESOLUÇÃO", resolucaoCombo),
                UiSupport.campo("QUALIDADE GRÁFICA", qualidadeCombo),
                actions
        );
        selector.setPadding(new Insets(22));
        selector.setPrefWidth(390);
        selector.getStyleClass().add("panel-card");

        result.setPadding(new Insets(22));
        result.getStyleClass().add("panel-card");
        mostrarVazio();

        HBox content = new HBox(20, selector, result);
        content.setPadding(new Insets(24, 0, 0, 0));
        HBox.setHgrow(result, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Análise da configuração",
                "Confira compatibilidade, energia, custo e desempenho medido em benchmarks.",
                content
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    private void carregarDados() {
        aplicarDados(buscarDados());
    }

    private DadosSeletores buscarDados() {
        return new DadosSeletores(
                new BuildDAO().listarPorUsuario(usuarioAtual.getId()),
                new JogoDAO().listar()
        );
    }

    private void aplicarDados(DadosSeletores dados) {
        buildCombo.setItems(FXCollections.observableArrayList(dados.builds()));
        jogoCombo.setItems(FXCollections.observableArrayList(dados.jogos()));
        if (!buildCombo.getItems().isEmpty()) {
            buildCombo.getSelectionModel().selectFirst();
        }
        if (!jogoCombo.getItems().isEmpty()) {
            jogoCombo.getSelectionModel().selectFirst();
        }
    }

    private void analisar(Button botaoAnalisar) {
        Build build = buildCombo.getValue();
        Jogo jogo = jogoCombo.getValue();
        if (build == null || jogo == null) {
            UiSupport.erro(new IllegalArgumentException("Selecione uma build e um jogo."));
            return;
        }

        String resolucao = resolucaoCombo.getValue();
        String qualidade = qualidadeCombo.getValue();
        UiSupport.executarComFeedback(
                botaoAnalisar,
                "Analisando...",
                () -> new BenchmarkFPSDAO().listar(),
                benchmarks -> mostrarAnalise(build, jogo, resolucao, qualidade, benchmarks)
        );
    }

    private void mostrarAnalise(
            Build build,
            Jogo jogo,
            String resolucao,
            String qualidade,
            List<BenchmarkFPS> benchmarks
    ) {
        boolean compativel = compatibilidade.verificarBuild(build);
        boolean fonteSuporta = consumo.fonteSuporta(build);
        int consumoTotal = consumo.calcularConsumo(build);
        int recomendado = consumo.consumoRecomendado(build);
        ResultadoFPS resultadoFPS = fps.analisar(
                build,
                jogo,
                resolucao,
                qualidade,
                benchmarks
        );

        FlowPane metrics = new FlowPane(12, 12);
        metrics.getChildren().addAll(
                metric("COMPATIBILIDADE", compativel ? "Aprovada" : "Reprovada", compativel),
                metric("CONSUMO", consumoTotal + " W", true),
                metric("FONTE RECOMENDADA", recomendado + " W", fonteSuporta),
                metric(
                        "FPS MÉDIO",
                        resultadoFPS.disponivel() ? String.valueOf(resultadoFPS.fpsMedio()) : "Sem dados",
                        resultadoFPS.disponivel() && resultadoFPS.fpsMedio() >= 60
                )
        );
        if (resultadoFPS.fpsUmPorCento() != null) {
            metrics.getChildren().add(metric(
                    "1% LOW",
                    String.valueOf(resultadoFPS.fpsUmPorCento()),
                    resultadoFPS.fpsUmPorCento() >= 45
            ));
        }
        metrics.getChildren().add(metric("CUSTO TOTAL", UiSupport.moeda(build.getPrecoTotal()), true));

        VBox parts = new VBox(
                9,
                label("Componentes analisados"),
                part("Processador", build.getProcessador().getNome()),
                part("Placa-mãe", build.getPlacaMae().getNome()),
                part("Placa de vídeo", build.getPlacaVideo().getNome()),
                part("Memória", build.getMemoria().getNome()),
                part("SSD", build.getSsd().getNome()),
                part("Fonte", build.getFonte().getNome() + " · " + build.getFonte().getPotencia() + " W")
        );

        Label caveat = new Label(resultadoFPS.tipoReferencia() + " · " + resultadoFPS.explicacao()
                + (resultadoFPS.amostras() > 0 ? " Amostras usadas: " + resultadoFPS.amostras() + "." : "")
                + (!resultadoFPS.fontes().isBlank() ? " Fontes: " + resultadoFPS.fontes() + "." : ""));
        caveat.setWrapText(true);
        caveat.getStyleClass().add("analysis-note");

        result.getChildren().setAll(label(build.getNome() + " · " + jogo.getNome()), metrics, parts, caveat);
    }

    private VBox metric(String title, String value, boolean positive) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-eyebrow");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("analysis-value");
        VBox box = new VBox(5, titleLabel, valueLabel);
        box.setPadding(new Insets(15));
        box.setPrefWidth(175);
        box.getStyleClass().add(positive ? "analysis-card-positive" : "analysis-card-negative");
        return box;
    }

    private HBox part(String name, String value) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("part-label");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("part-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, nameLabel, spacer, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("part-row");
        return row;
    }

    private void mostrarVazio() {
        Label empty = new Label("Selecione uma build e execute a análise para ver os resultados.");
        empty.setWrapText(true);
        empty.getStyleClass().add("empty-state");
        result.getChildren().setAll(label("Resultado"), empty);
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        return label;
    }

    private record DadosSeletores(List<Build> builds, List<Jogo> jogos) {
    }
}
