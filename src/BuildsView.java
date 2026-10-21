import ui.UiSupport;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import model.*;
import service.MontagemPC;
import java.util.List;

public class BuildsView extends VBox {
    private final BuildDAO buildDAO;
    private final Usuario usuarioAtual;
    private final MontagemPC.Catalogo catalogo;
    private final TableView<Build> table = new TableView<>();
    private final TabPane abas = new TabPane();
    private final Tab listaTab = new Tab("Minhas builds");
    private final Tab montagemTab = new Tab("Montar PC");
    private MontagemWizardView wizard;

    public BuildsView(Usuario usuarioAtual) {
        this(usuarioAtual, new BuildDAO(), carregarCatalogo());
    }

    /** JDBC can be supplied internally for disposable integration/UI verification. */
    BuildsView(Usuario usuarioAtual, BuildDAO buildDAO, MontagemPC.Catalogo catalogo) {
        if (usuarioAtual == null || usuarioAtual.getId() <= 0) throw new SecurityException("Entre na sua conta.");
        this.usuarioAtual = usuarioAtual; this.buildDAO = buildDAO; this.catalogo = catalogo;
        configurarTabela();
        listaTab.setClosable(false); montagemTab.setClosable(false);
        abas.getStyleClass().add("component-tabs");
        listaTab.setContent(criarListagem());
        criarMontagem(null);
        abas.getTabs().addAll(listaTab, montagemTab);
        getChildren().add(UiSupport.pagina("Minhas builds",
                "Monte seu PC uma peça por vez ou consulte suas configurações salvas.", abas));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    private static MontagemPC.Catalogo carregarCatalogo() {
        return new MontagemPC.Catalogo(new ProcessadorDAO().listar(), new PlacaMaeDAO().listar(),
                new MemoriaDAO().listar(), new PlacaVideoDAO().listar(), new SSDDAO().listar(), new FonteDAO().listar());
    }

    private VBox criarListagem() {
        Button novo = new Button("+ Montar novo PC"); novo.getStyleClass().add("primary-button");
        novo.setOnAction(e -> { if (podeDescartar()) { criarMontagem(null); abas.getSelectionModel().select(montagemTab); } });
        Button editar = new Button("Editar selecionada"); editar.getStyleClass().add("secondary-button");
        editar.setOnAction(e -> carregarParaEdicao(table.getSelectionModel().getSelectedItem()));
        Button excluir = new Button("Excluir selecionada"); excluir.getStyleClass().add("danger-button");
        excluir.setOnAction(e -> excluirSelecionada(excluir));
        Button atualizar = new Button("Atualizar lista"); atualizar.getStyleClass().add("ghost-button");
        atualizar.setOnAction(e -> UiSupport.executarComFeedback(atualizar, "Atualizando...", this::buscarBuilds,
                builds -> table.setItems(FXCollections.observableArrayList(builds))));
        Button comparar = new Button("Comparar 2–3 builds"); comparar.getStyleClass().add("secondary-button");
        comparar.setOnAction(e -> {
            try { BuildDialogs.comparar(List.copyOf(table.getSelectionModel().getSelectedItems()), usuarioAtual, getScene().getWindow()); }
            catch (RuntimeException erro) { UiSupport.erro(erro); }
        });
        Button exportar = new Button("Exportar selecionada"); exportar.getStyleClass().add("secondary-button");
        exportar.setOnAction(e -> {
            try { BuildDialogs.exportar(table.getSelectionModel().getSelectedItem(), usuarioAtual, getScene().getWindow()); }
            catch (RuntimeException erro) { UiSupport.erro(erro); }
        });
        FlowPane acoes = new FlowPane(10, 10, novo, editar, excluir, atualizar, comparar, exportar);
        Label dica = new Label("Use Ctrl ou Shift para comparar duas ou três builds. Clique duas vezes para editar.");
        dica.setWrapText(true); dica.getStyleClass().add("muted-text");
        VBox conteudo = new VBox(14, acoes, dica, table); conteudo.setPadding(new Insets(20, 0, 0, 0));
        VBox.setVgrow(table, Priority.ALWAYS); return conteudo;
    }

    private void configurarTabela() {
        table.getColumns().addAll(List.of(
                UiSupport.coluna("ID", b -> String.valueOf(b.getId()), 55),
                UiSupport.coluna("Build", Build::getNome, 180),
                UiSupport.coluna("CPU", b -> b.getProcessador().getNome(), 180),
                UiSupport.coluna("GPU", b -> b.getPlacaVideo().getNome(), 180),
                UiSupport.coluna("Total", b -> UiSupport.moeda(b.getPrecoTotal()), 125),
                UiSupport.coluna("Favorita", b -> b.isFavorita() ? "Sim" : "Não", 80)));
        table.setId("minhas-builds-tabela");
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.setPlaceholder(new Label("Você ainda não tem builds. Clique em Montar novo PC para começar."));
        table.getStyleClass().add("data-table");
        table.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) carregarParaEdicao(table.getSelectionModel().getSelectedItem());
        });
        table.setItems(FXCollections.observableArrayList(buscarBuilds()));
    }

    private void criarMontagem(Build original) {
        MontagemPC estado = new MontagemPC(usuarioAtual.getId());
        if (original != null) estado.editar(original);
        wizard = new MontagemWizardView(estado, catalogo, this::salvar);
        montagemTab.setText(original == null ? "Montar PC" : "Editar PC");
        montagemTab.setContent(wizard);
    }

    private boolean podeDescartar() {
        return wizard == null || !wizard.estado().modificado()
                || UiSupport.confirmar("Você tem uma montagem em andamento. Descartar essas alterações e abrir outra?");
    }

    private void carregarParaEdicao(Build build) {
        if (build == null) { UiSupport.erro(new IllegalArgumentException("Selecione uma build para editar.")); return; }
        if (build.getUsuarioId() != usuarioAtual.getId()) { UiSupport.erro(new SecurityException("Esta build pertence a outro usuário.")); return; }
        if (!podeDescartar()) return;
        criarMontagem(build); abas.getSelectionModel().select(montagemTab);
    }

    private void salvar(Build build) {
        if (build.getUsuarioId() != usuarioAtual.getId()) throw new SecurityException("Esta build pertence a outro usuário.");
        // Freeze both tabs while saving so another draft cannot replace the one in flight.
        abas.setDisable(true);
        UiSupport.emSegundoPlano(() -> {
            if (build.getId() > 0) buildDAO.atualizarDoUsuario(build, usuarioAtual.getId());
            else buildDAO.inserir(build);
            return buscarBuilds();
        }, builds -> {
            abas.setDisable(false); table.setItems(FXCollections.observableArrayList(builds));
            criarMontagem(null); abas.getSelectionModel().select(listaTab);
            UiSupport.sucesso("Seu PC foi salvo com sucesso.");
        }, erro -> { abas.setDisable(false); UiSupport.erro(erro); });
    }

    private void excluirSelecionada(Button botao) {
        Build build = table.getSelectionModel().getSelectedItem();
        if (build == null) { UiSupport.erro(new IllegalArgumentException("Selecione uma build para excluir.")); return; }
        if (!UiSupport.confirmar("Excluir permanentemente a build “" + build.getNome() + "”?")) return;
        UiSupport.executarComFeedback(botao, "Excluindo...", () -> {
            buildDAO.excluirDoUsuario(build.getId(), usuarioAtual.getId()); return buscarBuilds();
        }, builds -> {
            table.setItems(FXCollections.observableArrayList(builds));
            if (wizard.estado().parcial().getId() == build.getId()) criarMontagem(null);
        });
    }

    private List<Build> buscarBuilds() { return buildDAO.listarPorUsuario(usuarioAtual.getId()); }
}
