import ui.ThemeManager;
import ui.UiSupport;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.BenchmarkFPS;
import model.Build;
import model.Jogo;
import model.RecomendacaoJogo;
import model.Usuario;

import java.util.List;
import java.net.URI;

public class JogosView extends VBox {

    private final RecomendacaoJogoService service = new RecomendacaoJogoService();
    private final JogoDAO jogoDAO = new JogoDAO();
    private final AutorizacaoService autorizacao = new AutorizacaoService();
    private final StackPane conteudo = new StackPane();
    private final Usuario usuarioAtual;

    public JogosView(Usuario usuario) {
        this.usuarioAtual = usuario;
        conteudo.getChildren().setAll(carregando());
        getChildren().add(UiSupport.pagina(
                "Jogos",
                "Escolha um jogo e veja uma configuração baseada em medições reais para 1080p a 60 FPS.",
                conteudo
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
        carregar();
    }

    private void carregar() {
        UiSupport.emSegundoPlano(service::listar, this::mostrarJogos, erro -> {
            Label mensagem = new Label("Não foi possível carregar as recomendações.");
            mensagem.getStyleClass().add("startup-error");
            conteudo.getChildren().setAll(mensagem);
            UiSupport.erro(erro);
        });
    }

    private void mostrarJogos(List<RecomendacaoJogo> recomendacoes) {
        if (recomendacoes.isEmpty()) {
            Label vazio = new Label("Nenhum jogo cadastrado. Um administrador pode adicioná-los em Benchmarks.");
            vazio.setWrapText(true);
            vazio.getStyleClass().add("empty-state");
            conteudo.getChildren().setAll(vazio);
            return;
        }

        TilePane grade = new TilePane(18, 18);
        grade.setPrefColumns(3);
        grade.setTileAlignment(Pos.TOP_LEFT);
        grade.setPadding(new Insets(24, 0, 24, 0));
        recomendacoes.stream().map(this::card).forEach(grade.getChildren()::add);

        ScrollPane scroll = new ScrollPane(grade);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("games-scroll");
        conteudo.getChildren().setAll(scroll);
    }

    private Node card(RecomendacaoJogo recomendacao) {
        Jogo jogo = recomendacao.jogo();
        Label titulo = new Label(jogo.getNome());
        titulo.setWrapText(true);
        titulo.getStyleClass().add("game-card-title");

        Label genero = new Label(textoOu(jogo.getGenero(), "Jogo para PC"));
        genero.getStyleClass().add("game-card-genre");

        Label status = new Label(recomendacao.disponivel()
                ? recomendacao.benchmark().getFpsMedio() + " FPS • " + recomendacao.benchmark().getQualidade()
                : "Sem medição para 60 FPS");
        status.getStyleClass().add(recomendacao.disponivel()
                ? "game-status-ready"
                : "game-status-missing");

        VBox corpo = new VBox(9, capa(jogo), titulo, genero, status);
        corpo.setAlignment(Pos.TOP_LEFT);
        corpo.setFillWidth(true);

        Button card = new Button();
        card.setGraphic(corpo);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(260);
        card.setPrefHeight(244);
        card.setMinHeight(244);
        card.setMaxHeight(244);
        card.getStyleClass().add("game-card");
        card.setOnAction(event -> mostrarDetalhes(recomendacao));

        if (!usuarioAtual.isAdmin()) {
            return card;
        }

        Button trocarCapa = new Button("Trocar capa");
        trocarCapa.setMaxWidth(Double.MAX_VALUE);
        trocarCapa.getStyleClass().add("secondary-button");
        trocarCapa.setOnAction(event -> editarCapa(jogo));
        return new VBox(8, card, trocarCapa);
    }

    private void editarCapa(Jogo jogo) {
        autorizacao.exigirAdmin(usuarioAtual);

        TextField url = UiSupport.texto("https://exemplo.com/capa.webp");
        url.setText(textoOu(jogo.getImagemUrl(), ""));

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Capa de " + jogo.getNome());
        dialog.setHeaderText("Trocar imagem de capa");
        ButtonType salvar = new ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvar, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(new VBox(10,
                UiSupport.campo("URL PÚBLICA DA IMAGEM", url),
                dicaCapa()
        ));
        dialog.setResultConverter(botao -> botao == salvar ? url.getText().trim() : null);
        ThemeManager.estilizar(dialog);

        dialog.showAndWait().ifPresent(novaUrl -> {
            try {
                validarUrlImagem(novaUrl);
                autorizacao.exigirAdmin(usuarioAtual);
            } catch (RuntimeException e) {
                UiSupport.erro(e);
                return;
            }

            UiSupport.emSegundoPlano(() -> {
                jogoDAO.atualizarImagem(jogo.getId(), novaUrl);
                return null;
            }, ignorado -> {
                UiSupport.sucesso(novaUrl.isBlank() ? "Capa removida." : "Capa atualizada.");
                carregar();
            }, UiSupport::erro);
        });
    }

    private Label dicaCapa() {
        Label dica = new Label("Use uma URL iniciada por https:// ou http://. Deixe o campo vazio para remover a capa.");
        dica.setWrapText(true);
        dica.getStyleClass().add("muted-text");
        return dica;
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

    private Node capa(Jogo jogo) {
        StackPane cover = new StackPane();
        cover.setPrefSize(228, 120);
        cover.setMinSize(228, 120);
        cover.setMaxSize(228, 120);
        cover.getStyleClass().add("game-cover");

        Label fallback = new Label(inicial(jogo.getNome()));
        fallback.getStyleClass().add("game-cover-fallback");
        cover.getChildren().add(fallback);

        String url = jogo.getImagemUrl();
        if (url != null && (url.startsWith("https://") || url.startsWith("http://"))) {
            try {
                Image image = new Image(url, 228, 120, true, true, true);
                ImageView view = new ImageView(image);
                view.setFitWidth(228);
                view.setFitHeight(120);
                view.setPreserveRatio(false);
                view.getStyleClass().add("game-cover-image");
                Rectangle clip = new Rectangle(228, 120);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                view.setClip(clip);
                image.errorProperty().addListener((observable, previous, error) -> {
                    if (error) cover.getChildren().remove(view);
                });
                cover.getChildren().add(view);
            } catch (IllegalArgumentException ignored) {
                // O monograma permanece visível quando a URL não é válida.
            }
        }
        return cover;
    }

    private void mostrarDetalhes(RecomendacaoJogo recomendacao) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(recomendacao.jogo().getNome());
        dialog.setHeaderText("Recomendação para 1920×1080 a 60 FPS");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().setContent(recomendacao.disponivel()
                ? detalhesDisponiveis(recomendacao)
                : detalhesIndisponiveis(recomendacao.jogo()));
        ThemeManager.estilizar(dialog);
        dialog.showAndWait();
    }

    private Node detalhesDisponiveis(RecomendacaoJogo recomendacao) {
        Build build = recomendacao.build();
        BenchmarkFPS benchmark = recomendacao.benchmark();
        GridPane pecas = new GridPane();
        pecas.setHgap(18);
        pecas.setVgap(10);
        pecas.getStyleClass().add("recommendation-parts");
        linha(pecas, 0, "PROCESSADOR", build.getProcessador().getNome());
        linha(pecas, 1, "PLACA DE VÍDEO", build.getPlacaVideo().getNome());
        linha(pecas, 2, "PLACA-MÃE", build.getPlacaMae().getNome());
        linha(pecas, 3, "MEMÓRIA", build.getMemoria().getNome());
        linha(pecas, 4, "SSD", build.getSsd().getNome());
        linha(pecas, 5, "FONTE", build.getFonte().getNome());

        Label desempenho = new Label(
                benchmark.getFpsMedio() + " FPS médio • "
                        + benchmark.getQualidade() + " • 1920×1080"
        );
        desempenho.getStyleClass().add("recommendation-performance");
        Label preco = new Label(UiSupport.moeda(build.getPrecoTotal()));
        preco.getStyleClass().add("recommendation-price");
        Label fonte = new Label("Fonte do benchmark: " + benchmark.getFonte());
        fonte.setWrapText(true);
        fonte.getStyleClass().add("muted-text");
        Label aviso = new Label(
                "CPU e GPU vêm da medição cadastrada. As demais peças são as opções compatíveis "
                        + "de menor custo disponíveis no catálogo."
        );
        aviso.setWrapText(true);
        aviso.getStyleClass().add("analysis-note");
        return new VBox(16, desempenho, pecas, preco, fonte, aviso);
    }

    private Node detalhesIndisponiveis(Jogo jogo) {
        Label mensagem = new Label(
                "Ainda não existe uma medição real de " + jogo.getNome()
                        + " em 1920×1080 com pelo menos 60 FPS, ou faltam peças compatíveis no catálogo."
        );
        mensagem.setWrapText(true);
        mensagem.getStyleClass().add("analysis-note");
        return mensagem;
    }

    private void linha(GridPane grade, int linha, String rotulo, String valor) {
        Label label = new Label(rotulo);
        label.getStyleClass().add("recommendation-part-label");
        Label content = new Label(valor);
        content.setWrapText(true);
        content.getStyleClass().add("recommendation-part-value");
        grade.add(label, 0, linha);
        grade.add(content, 1, linha);
    }

    private Node carregando() {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(42, 42);
        Label label = new Label("Calculando configurações recomendadas...");
        label.getStyleClass().add("loading-title");
        VBox box = new VBox(12, progress, label);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private String inicial(String nome) {
        return nome == null || nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
    }

    private String textoOu(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto;
    }
}
