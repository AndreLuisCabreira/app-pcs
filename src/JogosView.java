import ui.ThemeManager;
import ui.UiSupport;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private final javafx.scene.control.ComboBox<String> resolucao = new javafx.scene.control.ComboBox<>();
    private final TextField fpsMinimo = UiSupport.texto("FPS mínimo");
    private final TextField orcamento = UiSupport.texto("Sem limite de orçamento");
    private final javafx.scene.control.Button aplicar = new javafx.scene.control.Button("Aplicar filtros");

    public JogosView(Usuario usuario) {
        this.usuarioAtual = usuario;
        resolucao.getItems().setAll("1920x1080", "2560x1440", "3840x2160");
        resolucao.setValue("1920x1080");
        fpsMinimo.setText("60");
        fpsMinimo.setPrefWidth(90);
        aplicar.getStyleClass().add("secondary-button");
        aplicar.setOnAction(event -> carregar());
        javafx.scene.layout.FlowPane filtros = new javafx.scene.layout.FlowPane(12, 10,
                UiSupport.campo("RESOLUÇÃO", resolucao), UiSupport.campo("FPS MÍNIMO", fpsMinimo),
                UiSupport.campo("ORÇAMENTO MÁXIMO (R$)", orcamento), aplicar);
        VBox corpo = new VBox(16, filtros, conteudo);
        VBox.setVgrow(conteudo, Priority.ALWAYS);
        conteudo.getChildren().setAll(carregando());
        getChildren().add(UiSupport.pagina(
                "Jogos",
                "Escolha resolução, FPS e orçamento para consultar medições cadastradas e peças compatíveis.",
                corpo
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
        carregar();
    }

    private void carregar() {
        final String alvo = resolucao.getValue();
        final int fps;
        final double limite;
        try {
            fps = UiSupport.inteiro(fpsMinimo, "FPS mínimo");
            limite = orcamento.getText().isBlank() ? Double.MAX_VALUE : UiSupport.decimal(orcamento, "Orçamento");
            if (fps <= 0 || limite <= 0) throw new IllegalArgumentException("FPS e orçamento devem ser maiores que zero.");
        } catch (RuntimeException e) { UiSupport.erro(e); return; }
        aplicar.setDisable(true);
        conteudo.getChildren().setAll(carregando());
        UiSupport.emSegundoPlano(() -> service.listar(alvo, fps, limite), dados -> {
            aplicar.setDisable(false);
            mostrarJogos(dados);
        }, erro -> {
            aplicar.setDisable(false);
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

        conteudo.getChildren().setAll(new ui.GameCatalog(recomendacoes,
                this::mostrarDetalhes, usuarioAtual.isAdmin() ? this::editarCapa : null));
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

    private void mostrarDetalhes(RecomendacaoJogo recomendacao) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(recomendacao.jogo().getNome());
        dialog.setHeaderText(recomendacao.disponivel()
                ? "Medição cadastrada em " + recomendacao.benchmark().getResolucao()
                : "Sem configuração para os filtros selecionados");
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
                        + benchmark.getQualidade() + " • " + benchmark.getResolucao()
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
                        + " para a resolução, FPS e orçamento selecionados, ou faltam peças compatíveis no catálogo."
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

    private String textoOu(String texto, String padrao) {
        return texto == null || texto.isBlank() ? padrao : texto;
    }
}
