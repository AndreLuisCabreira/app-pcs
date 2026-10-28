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
    private final BuildsOperacoes operacoes;
    private final Label avisoLista = new Label();
    private final TableView<Build> table = new TableView<>();
    private final TabPane abas = new TabPane();
    private final Tab listaTab = new Tab("Minhas builds");
    private final Tab montagemTab = new Tab("Montar PC");
    private MontagemWizardView wizard;

    public BuildsView(Usuario usuarioAtual) {
        this(usuarioAtual, new BuildDAO(), carregarDados(usuarioAtual));
    }

    /** JDBC can be supplied internally for disposable integration/UI verification. */
    BuildsView(Usuario usuarioAtual, BuildDAO buildDAO, MontagemPC.Catalogo catalogo) {
        this(usuarioAtual, buildDAO, new DadosIniciais(catalogo,
                buildDAO.listarPorUsuario(exigirUsuario(usuarioAtual))));
    }

    /** Builds controls from a prepared snapshot without querying JDBC. */
    BuildsView(Usuario usuarioAtual, BuildDAO buildDAO, DadosIniciais dados) {
        if (usuarioAtual == null || usuarioAtual.getId() <= 0) throw new SecurityException("Entre na sua conta.");
        this.usuarioAtual = usuarioAtual; this.buildDAO = buildDAO; this.catalogo = dados.catalogo();
        this.operacoes = new BuildsOperacoes(buildDAO, usuarioAtual.getId());
        configurarTabela();
        atualizarTabela(dados.builds());
        listaTab.setClosable(false); montagemTab.setClosable(false);
        abas.getStyleClass().add("component-tabs");
        listaTab.setContent(criarListagem());
        criarMontagem(null);
        abas.getTabs().addAll(listaTab, montagemTab);
        getChildren().add(UiSupport.pagina("Minhas builds",
                "Monte seu PC uma peça por vez ou consulte suas configurações salvas.", abas));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    record DadosIniciais(MontagemPC.Catalogo catalogo, List<Build> builds) {
        DadosIniciais {
            java.util.Objects.requireNonNull(catalogo);
            builds = List.copyOf(builds);
        }
    }

    static DadosIniciais carregarDados(Usuario usuario) {
        int usuarioId = exigirUsuario(usuario);
        return new DadosIniciais(carregarCatalogo(), new BuildDAO().listarPorUsuario(usuarioId));
    }

    private static int exigirUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() <= 0) throw new SecurityException("Entre na sua conta.");
        return usuario.getId();
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
        excluir.setOnAction(e -> excluirSelecionada());
        Button atualizar = new Button("Atualizar lista"); atualizar.getStyleClass().add("ghost-button");
        atualizar.setOnAction(e -> atualizarLista(atualizar));
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
        avisoLista.setId("builds-aviso-lista"); avisoLista.setWrapText(true);
        avisoLista.getStyleClass().add("muted-text"); avisoLista.setVisible(false); avisoLista.setManaged(false);
        VBox conteudo = new VBox(14, acoes, dica, avisoLista, table); conteudo.setPadding(new Insets(20, 0, 0, 0));
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
        UiSupport.emSegundoPlano(() -> operacoes.salvar(build), resultado -> {
            abas.setDisable(false);
            criarMontagem(null); abas.getSelectionModel().select(listaTab);
            aplicarResultado(resultado, "Seu PC foi salvo");
            if (resultado.listaAtualizada()) UiSupport.sucesso("Seu PC foi salvo com sucesso.");
        }, erro -> { abas.setDisable(false); UiSupport.erro(erro); });
    }

    private void excluirSelecionada() {
        Build build = table.getSelectionModel().getSelectedItem();
        if (build == null) { UiSupport.erro(new IllegalArgumentException("Selecione uma build para excluir.")); return; }
        if (!UiSupport.confirmar("Excluir permanentemente a build “" + build.getNome() + "”?")) return;
        abas.setDisable(true);
        UiSupport.emSegundoPlano(() -> operacoes.excluir(build), resultado -> {
            abas.setDisable(false);
            if (wizard.estado().parcial().getId() == build.getId()) criarMontagem(null);
            aplicarResultado(resultado, "A build foi excluída");
        }, erro -> { abas.setDisable(false); UiSupport.erro(erro); });
    }

    private void atualizarLista(Button botao) {
        abas.setDisable(true);
        botao.setText("Atualizando...");
        UiSupport.emSegundoPlano(this::buscarBuilds, builds -> {
            abas.setDisable(false); botao.setText("Atualizar lista");
            atualizarTabela(builds);
        }, erro -> {
            abas.setDisable(false); botao.setText("Atualizar lista");
            UiSupport.erro(erro);
        });
    }

    private void aplicarResultado(BuildsOperacoes.Resultado resultado, String operacao) {
        if (resultado.listaAtualizada()) {
            atualizarTabela(resultado.builds());
        } else {
            table.getItems().clear();
            table.setPlaceholder(new Label("Lista indisponível. Use Atualizar lista."));
            avisoLista.setText(operacao + ", mas não foi possível recarregar a lista. Use Atualizar lista; não é necessário repetir a operação.");
            avisoLista.setVisible(true); avisoLista.setManaged(true);
        }
    }

    private void atualizarTabela(List<Build> builds) {
        table.setItems(FXCollections.observableArrayList(builds));
        table.setPlaceholder(new Label("Você ainda não tem builds. Clique em Montar novo PC para começar."));
        avisoLista.setText(""); avisoLista.setVisible(false); avisoLista.setManaged(false);
    }

    private List<Build> buscarBuilds() { return buildDAO.listarPorUsuario(usuarioAtual.getId()); }
}
