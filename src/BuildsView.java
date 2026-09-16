import ui.UiSupport;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Build;
import model.Fonte;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.SSD;
import model.Usuario;
import service.CompatibilidadeService;
import service.ConsumoService;

import java.util.List;

public class BuildsView extends VBox {

    private final BuildDAO buildDAO = new BuildDAO();
    private final Usuario usuarioAtual;
    private final CompatibilidadeService compatibilidadeService = new CompatibilidadeService();
    private final ConsumoService consumoService = new ConsumoService();

    private final TextField nome = UiSupport.texto("Ex.: Setup principal");
    private final ComboBox<Processador> processador = new ComboBox<>();
    private final ComboBox<PlacaMae> placaMae = new ComboBox<>();
    private final ComboBox<PlacaVideo> placaVideo = new ComboBox<>();
    private final ComboBox<Memoria> memoria = new ComboBox<>();
    private final ComboBox<SSD> ssd = new ComboBox<>();
    private final ComboBox<Fonte> fonte = new ComboBox<>();
    private final CheckBox favorita = new CheckBox("Marcar como favorita");
    private final TableView<Build> table = new TableView<>();
    private final Button save = new Button("Salvar build");
    private Integer editingId;

    private List<PlacaMae> todasPlacasMae = List.of();
    private List<Memoria> todasMemorias = List.of();

    public BuildsView(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        configurarCombos();
        carregarCatalogo();
        configurarFiltros();
        configurarTabela();

        Label formTitle = new Label("Nova build");
        formTitle.getStyleClass().add("card-title");
        Label help = new Label(
                "Esta build será salva para " + usuarioAtual.getNome()
                        + ". Placa-mãe e memória são filtradas automaticamente."
        );
        help.setWrapText(true);
        help.getStyleClass().add("muted-text");

        save.getStyleClass().add("primary-button");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> salvar());

        Button cancel = new Button("Cancelar edição");
        cancel.getStyleClass().add("secondary-button");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setOnAction(event -> limparFormulario());

        HBox actions = new HBox(10, save, cancel);
        HBox.setHgrow(save, Priority.ALWAYS);
        HBox.setHgrow(cancel, Priority.ALWAYS);

        VBox form = new VBox(
                14,
                formTitle,
                help,
                UiSupport.campo("NOME DA BUILD", nome),
                UiSupport.campo("PROCESSADOR", processador),
                UiSupport.campo("PLACA-MÃE COMPATÍVEL", placaMae),
                UiSupport.campo("PLACA DE VÍDEO", placaVideo),
                UiSupport.campo("MEMÓRIA COMPATÍVEL", memoria),
                UiSupport.campo("SSD", ssd),
                UiSupport.campo("FONTE", fonte),
                favorita,
                actions
        );
        form.setPadding(new Insets(24));
        form.setPrefWidth(390);
        form.getStyleClass().add("panel-card");

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(410);
        scroll.getStyleClass().add("transparent-scroll");

        Button edit = new Button("Editar selecionada");
        edit.getStyleClass().add("secondary-button");
        edit.setOnAction(event -> carregarParaEdicao(table.getSelectionModel().getSelectedItem()));
        Button delete = new Button("Excluir selecionada");
        delete.getStyleClass().add("danger-button");
        delete.setOnAction(event -> excluirSelecionada(delete));
        Button refresh = new Button("Atualizar lista");
        refresh.getStyleClass().add("ghost-button");
        refresh.setOnAction(event -> atualizarTabelaAssincrono(refresh));

        HBox tableActions = new HBox(10, edit, delete, refresh);
        VBox listing = new VBox(14, tableActions, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox content = new HBox(20, scroll, listing);
        content.setPadding(new Insets(24, 0, 0, 0));
        HBox.setHgrow(listing, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Montagem de builds",
                "Combine componentes, valide compatibilidade e acompanhe o custo total.",
                content
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    private void configurarCombos() {
        UiSupport.configurarCombo(processador, p -> p.getNome() + " · " + p.getSocket());
        UiSupport.configurarCombo(placaMae, p -> p.getNome() + " · " + p.getSocket() + " / " + p.getTipoMemoria());
        UiSupport.configurarCombo(placaVideo, p -> p.getNome() + " · " + p.getMemoria() + " GB");
        UiSupport.configurarCombo(memoria, m -> m.getNome() + " · " + m.getTipo());
        UiSupport.configurarCombo(ssd, s -> s.getNome() + " · " + s.getCapacidade() + " GB");
        UiSupport.configurarCombo(fonte, f -> f.getNome() + " · " + f.getPotencia() + " W");
    }

    private void carregarCatalogo() {
        processador.setItems(FXCollections.observableArrayList(new ProcessadorDAO().listar()));
        todasPlacasMae = new PlacaMaeDAO().listar();
        placaMae.setItems(FXCollections.observableArrayList(todasPlacasMae));
        placaVideo.setItems(FXCollections.observableArrayList(new PlacaVideoDAO().listar()));
        todasMemorias = new MemoriaDAO().listar();
        memoria.setItems(FXCollections.observableArrayList(todasMemorias));
        ssd.setItems(FXCollections.observableArrayList(new SSDDAO().listar()));
        fonte.setItems(FXCollections.observableArrayList(new FonteDAO().listar()));
    }

    private void configurarFiltros() {
        processador.valueProperty().addListener((observable, anterior, atual) -> {
            if (atual == null) {
                placaMae.setItems(FXCollections.observableArrayList(todasPlacasMae));
                return;
            }
            placaMae.setItems(FXCollections.observableArrayList(
                    todasPlacasMae.stream()
                            .filter(item -> item.getSocket().equalsIgnoreCase(atual.getSocket()))
                            .toList()
            ));
            if (placaMae.getValue() != null
                    && !placaMae.getValue().getSocket().equalsIgnoreCase(atual.getSocket())) {
                placaMae.setValue(null);
            }
        });

        placaMae.valueProperty().addListener((observable, anterior, atual) -> {
            if (atual == null) {
                memoria.setItems(FXCollections.observableArrayList(todasMemorias));
                return;
            }
            memoria.setItems(FXCollections.observableArrayList(
                    todasMemorias.stream()
                            .filter(item -> item.getTipo().equalsIgnoreCase(atual.getTipoMemoria()))
                            .toList()
            ));
            if (memoria.getValue() != null
                    && !memoria.getValue().getTipo().equalsIgnoreCase(atual.getTipoMemoria())) {
                memoria.setValue(null);
            }
        });
    }

    private void configurarTabela() {
        table.getColumns().addAll(List.of(
                UiSupport.coluna("ID", b -> String.valueOf(b.getId()), 55),
                UiSupport.coluna("Build", Build::getNome, 180),
                UiSupport.coluna("CPU", b -> b.getProcessador().getNome(), 180),
                UiSupport.coluna("GPU", b -> b.getPlacaVideo().getNome(), 180),
                UiSupport.coluna("Total", b -> UiSupport.moeda(b.getPrecoTotal()), 125),
                UiSupport.coluna("Favorita", b -> b.isFavorita() ? "Sim" : "Não", 80)
        ));
        table.setPlaceholder(new Label("Nenhuma build cadastrada."));
        table.getStyleClass().add("data-table");
        table.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                carregarParaEdicao(table.getSelectionModel().getSelectedItem());
            }
        });
        atualizarTabela();
    }

    private void salvar() {
        Build build;
        boolean editando = editingId != null;
        try {
            build = criarBuildDoFormulario();
            if (!compatibilidadeService.verificarBuild(build)) {
                throw new IllegalArgumentException("Processador, placa-mãe e memória não são compatíveis.");
            }
            if (!consumoService.fonteSuporta(build)) {
                throw new IllegalArgumentException(
                        "A fonte selecionada é insuficiente. Recomendado: "
                                + consumoService.consumoRecomendado(build) + " W."
                );
            }
            if (editando) {
                build.setId(editingId);
            }
        } catch (RuntimeException e) {
            UiSupport.erro(e);
            return;
        }

        UiSupport.executarComFeedback(save, editando ? "Atualizando..." : "Salvando...", () -> {
            if (editando) {
                buildDAO.atualizarDoUsuario(build, usuarioAtual.getId());
            } else {
                buildDAO.inserir(build);
            }
            return buscarBuilds();
        }, builds -> {
            table.setItems(FXCollections.observableArrayList(builds));
            limparFormulario();
            UiSupport.sucesso("Build salva e validada com sucesso.");
        });
    }

    private Build criarBuildDoFormulario() {
        exigirSelecao(processador, "Processador");
        exigirSelecao(placaMae, "Placa-mãe");
        exigirSelecao(placaVideo, "Placa de vídeo");
        exigirSelecao(memoria, "Memória");
        exigirSelecao(ssd, "SSD");
        exigirSelecao(fonte, "Fonte");

        Build build = new Build(UiSupport.obrigatorio(nome, "Nome da build"), usuarioAtual.getId());
        build.setProcessador(processador.getValue());
        build.setPlacaMae(placaMae.getValue());
        build.setPlacaVideo(placaVideo.getValue());
        build.setMemoria(memoria.getValue());
        build.setSsd(ssd.getValue());
        build.setFonte(fonte.getValue());
        build.setFavorita(favorita.isSelected());
        return build;
    }

    private void carregarParaEdicao(Build build) {
        if (build == null) {
            UiSupport.erro(new IllegalArgumentException("Selecione uma build na tabela."));
            return;
        }
        editingId = build.getId();
        nome.setText(build.getNome());
        if (build.getUsuarioId() != usuarioAtual.getId()) {
            UiSupport.erro(new IllegalArgumentException("Esta build pertence a outro usuário."));
            return;
        }
        selecionarPorId(processador, build.getProcessador().getId(), Processador::getId);
        selecionarPorId(placaMae, build.getPlacaMae().getId(), PlacaMae::getId);
        selecionarPorId(placaVideo, build.getPlacaVideo().getId(), PlacaVideo::getId);
        selecionarPorId(memoria, build.getMemoria().getId(), Memoria::getId);
        selecionarPorId(ssd, build.getSsd().getId(), SSD::getId);
        selecionarPorId(fonte, build.getFonte().getId(), Fonte::getId);
        favorita.setSelected(build.isFavorita());
        save.setText("Atualizar build");
    }

    private <T> void selecionarPorId(ComboBox<T> combo, int id, java.util.function.ToIntFunction<T> idReader) {
        combo.getItems().stream()
                .filter(item -> idReader.applyAsInt(item) == id)
                .findFirst()
                .ifPresent(combo::setValue);
    }

    private void excluirSelecionada(Button botaoExcluir) {
        Build build = table.getSelectionModel().getSelectedItem();
        if (build == null) {
            UiSupport.erro(new IllegalArgumentException("Selecione uma build para excluir."));
            return;
        }
        if (!UiSupport.confirmar("Excluir permanentemente a build “" + build.getNome() + "”?")) {
            return;
        }
        UiSupport.executarComFeedback(botaoExcluir, "Excluindo...", () -> {
            buildDAO.excluirDoUsuario(build.getId(), usuarioAtual.getId());
            return buscarBuilds();
        }, builds -> {
            table.setItems(FXCollections.observableArrayList(builds));
            limparFormulario();
        });
    }

    private void atualizarTabela() {
        table.setItems(FXCollections.observableArrayList(buscarBuilds()));
    }

    private void atualizarTabelaAssincrono(Button botaoAtualizar) {
        UiSupport.executarComFeedback(
                botaoAtualizar,
                "Atualizando...",
                this::buscarBuilds,
                builds -> table.setItems(FXCollections.observableArrayList(builds))
        );
    }

    private List<Build> buscarBuilds() {
        return buildDAO.listarPorUsuario(usuarioAtual.getId());
    }

    private void limparFormulario() {
        editingId = null;
        nome.clear();
        processador.setValue(null);
        placaMae.setValue(null);
        placaVideo.setValue(null);
        memoria.setValue(null);
        ssd.setValue(null);
        fonte.setValue(null);
        favorita.setSelected(false);
        save.setText("Salvar build");
    }

    private void exigirSelecao(ComboBox<?> combo, String nomeCampo) {
        if (combo.getValue() == null) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório.");
        }
    }
}
