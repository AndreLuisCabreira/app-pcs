import ui.UiSupport;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.BenchmarkFPS;
import model.Jogo;
import model.PlacaVideo;
import model.Processador;
import model.Usuario;

import java.util.List;
import java.net.URI;

public class BenchmarksView extends VBox {

    public static final String[] RESOLUCOES = {"1920x1080", "2560x1440", "3840x2160"};
    public static final String[] QUALIDADES = {"Baixo", "Médio", "Alto", "Ultra"};

    private final Usuario usuarioAtual;
    private final AutorizacaoService autorizacao = new AutorizacaoService();
    private final BenchmarkFPSDAO benchmarkDAO = new BenchmarkFPSDAO();
    private final JogoDAO jogoDAO = new JogoDAO();
    private final ComboBox<Jogo> jogo = new ComboBox<>();
    private final ComboBox<Processador> processador = new ComboBox<>();
    private final ComboBox<PlacaVideo> placaVideo = new ComboBox<>();
    private final ComboBox<String> resolucao = new ComboBox<>();
    private final ComboBox<String> qualidade = new ComboBox<>();
    private final ListView<BenchmarkFPS> lista = new ListView<>();

    public BenchmarksView(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        autorizacao.exigirAdmin(usuarioAtual);

        UiSupport.configurarCombo(jogo, Jogo::getNome);
        UiSupport.configurarCombo(processador, Processador::getNome);
        UiSupport.configurarCombo(placaVideo, PlacaVideo::getNome);
        resolucao.setItems(FXCollections.observableArrayList(RESOLUCOES));
        qualidade.setItems(FXCollections.observableArrayList(QUALIDADES));
        resolucao.setMaxWidth(Double.MAX_VALUE);
        qualidade.setMaxWidth(Double.MAX_VALUE);
        resolucao.getSelectionModel().selectFirst();
        qualidade.getSelectionModel().select("Alto");

        TextField novoJogo = UiSupport.texto("Ex.: Red Dead Redemption 2");
        TextField generoJogo = UiSupport.texto("Opcional. Ex.: RPG de ação");
        TextField descricaoJogo = UiSupport.texto("Opcional. Breve descrição do jogo");
        TextField imagemJogo = UiSupport.texto("Opcional. URL pública da capa WebP");
        Button adicionarJogo = new Button("Adicionar jogo");
        adicionarJogo.getStyleClass().add("secondary-button");
        adicionarJogo.setOnAction(event -> {
            String nomeJogo;
            try {
                nomeJogo = UiSupport.obrigatorio(novoJogo, "Nome do jogo");
                validarUrlImagem(imagemJogo.getText());
                autorizacao.exigirAdmin(usuarioAtual);
            } catch (RuntimeException e) {
                UiSupport.erro(e);
                return;
            }
            UiSupport.executarComFeedback(adicionarJogo, "Adicionando...", () -> {
                jogoDAO.inserirDetalhes(
                        nomeJogo,
                        generoJogo.getText(),
                        descricaoJogo.getText(),
                        imagemJogo.getText()
                );
                return buscarCombos();
            }, dados -> {
                aplicarCombos(dados);
                novoJogo.clear();
                generoJogo.clear();
                descricaoJogo.clear();
                imagemJogo.clear();
                UiSupport.sucesso("Jogo adicionado.");
            });
        });
        HBox novoJogoLinha = new HBox(10, novoJogo, adicionarJogo);
        HBox.setHgrow(novoJogo, Priority.ALWAYS);

        TextField fpsMedio = UiSupport.texto("Ex.: 92");
        TextField fpsUmLow = UiSupport.texto("Opcional. Ex.: 68");
        TextField fonte = UiSupport.texto("Link, canal ou publicação");
        TextField observacoes = UiSupport.texto("Opcional: versão, RAM, ray tracing...");

        Button salvar = new Button("Salvar medição");
        salvar.setMaxWidth(Double.MAX_VALUE);
        salvar.getStyleClass().add("primary-button");
        salvar.setOnAction(event -> {
            BenchmarkFPS item;
            try {
                autorizacao.exigirAdmin(usuarioAtual);
                item = novoBenchmark(fpsMedio, fpsUmLow, fonte, observacoes);
            } catch (RuntimeException e) {
                UiSupport.erro(e);
                return;
            }
            UiSupport.executarComFeedback(salvar, "Salvando...", () -> {
                benchmarkDAO.inserir(item);
                return benchmarkDAO.listar();
            }, itens -> {
                lista.setItems(FXCollections.observableArrayList(itens));
                fpsMedio.clear();
                fpsUmLow.clear();
                fonte.clear();
                observacoes.clear();
                UiSupport.sucesso("Benchmark salvo. Ele já pode ser usado nas análises.");
            });
        });

        Label dica = new Label(
                "Cadastre o FPS médio observado em um benchmark real. Use sempre a mesma resolução e qualidade "
                        + "do teste, de preferência sem ray tracing ou upscaling, e informe a fonte."
        );
        dica.setWrapText(true);
        dica.getStyleClass().add("analysis-note");

        VBox formulario = new VBox(
                14,
                titulo("Novo jogo"), novoJogoLinha,
                UiSupport.campo("GÊNERO", generoJogo),
                UiSupport.campo("DESCRIÇÃO", descricaoJogo),
                UiSupport.campo("URL DA CAPA (WEBP)", imagemJogo),
                titulo("Nova medição"),
                UiSupport.campo("JOGO", jogo),
                UiSupport.campo("PROCESSADOR TESTADO", processador),
                UiSupport.campo("PLACA DE VÍDEO TESTADA", placaVideo),
                UiSupport.campo("RESOLUÇÃO", resolucao),
                UiSupport.campo("QUALIDADE GRÁFICA", qualidade),
                UiSupport.campo("FPS MÉDIO", fpsMedio),
                UiSupport.campo("FPS 1% LOW", fpsUmLow),
                UiSupport.campo("FONTE DO BENCHMARK", fonte),
                UiSupport.campo("OBSERVAÇÕES", observacoes),
                salvar,
                dica
        );
        formulario.setPadding(new Insets(22));
        formulario.getStyleClass().add("panel-card");

        ScrollPane formularioScroll = new ScrollPane(formulario);
        formularioScroll.setFitToWidth(true);
        formularioScroll.setPrefWidth(420);
        formularioScroll.getStyleClass().add("transparent-scroll");

        lista.setPlaceholder(new Label("Nenhuma medição cadastrada."));
        lista.getStyleClass().add("catalog-list");
        VBox catalogo = new VBox(12, titulo("Medições cadastradas"), lista);
        catalogo.setPadding(new Insets(20));
        catalogo.getStyleClass().add("panel-card");
        VBox.setVgrow(lista, Priority.ALWAYS);

        HBox conteudo = new HBox(18, formularioScroll, catalogo);
        conteudo.setPadding(new Insets(24, 0, 0, 0));
        HBox.setHgrow(catalogo, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Benchmarks de jogos",
                "Use medições reais para calcular uma referência de FPS transparente.",
                conteudo
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);

        carregarCombos();
        carregarLista();
    }

    private BenchmarkFPS novoBenchmark(
            TextField fpsMedio,
            TextField fpsUmLow,
            TextField fonte,
            TextField observacoes
    ) {
        if (jogo.getValue() == null || processador.getValue() == null || placaVideo.getValue() == null) {
            throw new IllegalArgumentException("Selecione jogo, processador e placa de vídeo.");
        }
        int media = UiSupport.inteiro(fpsMedio, "FPS médio");
        if (media <= 0) {
            throw new IllegalArgumentException("FPS médio deve ser maior que zero.");
        }
        Integer umLow = null;
        if (!fpsUmLow.getText().isBlank()) {
            umLow = UiSupport.inteiro(fpsUmLow, "FPS 1% low");
            if (umLow <= 0 || umLow > media) {
                throw new IllegalArgumentException("FPS 1% low deve ser maior que zero e não pode superar o FPS médio.");
            }
        }

        BenchmarkFPS item = new BenchmarkFPS();
        item.setJogoId(jogo.getValue().getId());
        item.setProcessadorId(processador.getValue().getId());
        item.setPlacaVideoId(placaVideo.getValue().getId());
        item.setResolucao(resolucao.getValue());
        item.setQualidade(qualidade.getValue());
        item.setFpsMedio(media);
        item.setFpsUmPorCento(umLow);
        item.setFonte(UiSupport.obrigatorio(fonte, "Fonte do benchmark"));
        item.setObservacoes(observacoes.getText().trim());
        return item;
    }

    private void carregarCombos() {
        aplicarCombos(buscarCombos());
    }

    private DadosCombos buscarCombos() {
        return new DadosCombos(
                jogoDAO.listar(),
                new ProcessadorDAO().listar(),
                new PlacaVideoDAO().listar()
        );
    }

    private void aplicarCombos(DadosCombos dados) {
        jogo.setItems(FXCollections.observableArrayList(dados.jogos()));
        processador.setItems(FXCollections.observableArrayList(dados.processadores()));
        placaVideo.setItems(FXCollections.observableArrayList(dados.placasVideo()));
        selecionarPrimeiro(jogo);
        selecionarPrimeiro(processador);
        selecionarPrimeiro(placaVideo);
    }

    private void carregarLista() {
        lista.setItems(FXCollections.observableArrayList(benchmarkDAO.listar()));
    }

    private <T> void selecionarPrimeiro(ComboBox<T> combo) {
        if (!combo.getItems().isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
    }

    private void validarUrlImagem(String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        try {
            URI uri = URI.create(valor.trim());
            if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("A URL da capa deve começar com https:// ou http://.");
        }
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("card-title");
        return label;
    }

    private record DadosCombos(
            List<Jogo> jogos,
            List<Processador> processadores,
            List<PlacaVideo> placasVideo
    ) {
    }
}
