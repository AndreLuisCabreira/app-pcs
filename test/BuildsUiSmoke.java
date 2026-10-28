import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.util.Duration;
import model.*;
import service.MontagemPC;
import ui.UiSupport;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Optional JavaFX smoke in the default package, alongside the application and H2 fixture. */
public final class BuildsUiSmoke {
    private static final CountDownLatch done = new CountDownLatch(1);
    private static volatile Throwable failure;
    private static int phase;
    private static int savedId;
    private static boolean sawWriteError;
    private static Timeline timer;

    public static void main(String[] args) throws Exception {
        BancoTeste db = new BancoTeste();
        Usuario user = db.usuario("smoke-builds");
        Build template = db.build(user.getId());
        MontagemPC.Catalogo catalog = new MontagemPC.Catalogo(
                List.of(template.getProcessador()), List.of(template.getPlacaMae()),
                List.of(template.getMemoria()), List.of(template.getPlacaVideo()),
                List.of(template.getSsd()), List.of(template.getFonte()));
        FaultDAO dao = new FaultDAO(db);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                UiSupport.carregarPagina(() -> {
                    check(!Platform.isFxApplicationThread(), "JDBC inicial na thread FX");
                    return new BuildsView.DadosIniciais(catalog, dao.listarPorUsuario(user.getId()));
                }, data -> {
                    check(Platform.isFxApplicationThread(), "Controles fora da thread FX");
                    return new BuildsView(user, dao, data);
                }, node -> {
                    try { begin((BuildsView) node, user, dao, db); }
                    catch (Throwable error) { fail(error); }
                }, BuildsUiSmoke::fail);
            });
            check(done.await(40, TimeUnit.SECONDS), "Timeout do smoke JavaFX");
            if (failure != null) throw new AssertionError(failure);
            System.out.println("PASS: JDBC inicial em worker e controles na thread FX; montagem, save confirmado com falha de lista, atualização, falha de gravação sem perder rascunho, edição com mesmo ID e exclusão com recuperação da lista.");
        } finally {
            Platform.exit();
        }
    }

    private static void begin(BuildsView view, Usuario user, FaultDAO dao, BancoTeste db) {
        check(Platform.isFxApplicationThread(), "Resultado fora da thread FX");
        Stage stage = new Stage(); Scene scene = new Scene(view, 1040, 800);
        var css = BuildsUiSmoke.class.getResource("/styles.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene); stage.show(); layout(scene);
        button(view, "+ Montar novo PC").fire(); layout(scene);
        for (int i = 0; i < 6; i++) {
            ((ListView<?>) view.lookup("#montagem-opcoes")).getSelectionModel().selectFirst();
            ((Button) view.lookup("#montagem-continuar")).fire(); layout(scene);
        }
        ((TextField) view.lookup("#montagem-nome")).setText("PC salvo uma vez");
        dao.failList.set(true);
        ((Button) view.lookup("#montagem-salvar")).fire();
        TabPane tabs = (TabPane) view.lookup(".tab-pane");
        check(tabs.isDisable(), "Abas ativas durante gravação");
        timer = new Timeline(new KeyFrame(Duration.millis(60), event -> Platform.runLater(() -> {
            try {
                for (Window window : List.copyOf(Window.getWindows())) {
                    if (window == stage || !window.isShowing()) continue;
                    if (window.getScene().getRoot() instanceof DialogPane dialog) {
                        if (dialog.getContentText() != null && dialog.getContentText().contains("Falha simulada na gravação")) sawWriteError = true;
                        ((Button) dialog.lookupButton(ButtonType.OK)).fire();
                        return;
                    }
                }
                if (tabs.isDisable()) return;
                @SuppressWarnings("unchecked")
                TableView<Build> table = (TableView<Build>) view.lookup("#minhas-builds-tabela");
                Label notice = (Label) view.lookup("#builds-aviso-lista");
                BuildDAO verify = new BuildDAO(db::abrir);
                switch (phase) {
                    case 0 -> {
                        check(notice.isVisible(), "Falta aviso de operação concluída/lista indisponível");
                        check(notice.getText().contains("foi salvo"), "Gravação foi tratada como falha");
                        check(db.contar("build") == 1 && table.getItems().isEmpty(), "Gravação ou estado da lista incorretos");
                        MontagemWizardView draft = (MontagemWizardView) tabs.getTabs().get(1).getContent();
                        check(!draft.estado().modificado(), "Rascunho salvo ainda permite repetição acidental");
                        savedId = verify.listarPorUsuario(user.getId()).getFirst().getId();
                        phase = 1; button(view, "Atualizar lista").fire();
                    }
                    case 1 -> {
                        if (button(view, "Atualizar lista").isDisabled()) return;
                        check(!notice.isVisible() && table.getItems().size() == 1, "Recuperação da lista falhou");
                        table.getSelectionModel().selectFirst(); button(view, "Editar selecionada").fire(); layout(scene);
                        ((TextField) view.lookup("#montagem-nome")).setText("PC editado");
                        dao.failWrite.set(true); phase = 2;
                        ((Button) view.lookup("#montagem-salvar")).fire();
                    }
                    case 2 -> {
                        if (!sawWriteError) return;
                        MontagemWizardView draft = (MontagemWizardView) tabs.getTabs().get(1).getContent();
                        check(draft.estado().parcial().getNome().equals("PC editado"), "Falha perdeu o nome no rascunho");
                        check(verify.buscarPorIdDoUsuario(savedId, user.getId()).getNome().equals("PC salvo uma vez"), "Falha alterou os dados");
                        phase = 3; ((Button) view.lookup("#montagem-salvar")).fire();
                    }
                    case 3 -> {
                        check(db.contar("build") == 1, "Edição duplicou a build");
                        check(table.getItems().getFirst().getId() == savedId, "Edição mudou o ID");
                        check(table.getItems().getFirst().getNome().equals("PC editado"), "Tabela não mostra edição");
                        check(verify.buscarPorIdDoUsuario(savedId, user.getId()).getNome().equals("PC editado"), "Edição não persistida");
                        table.getSelectionModel().selectFirst(); button(view, "Editar selecionada").fire(); layout(scene);
                        tabs.getSelectionModel().select(0); layout(scene);
                        dao.failList.set(true); phase = 4;
                        button(view, "Excluir selecionada").fire();
                    }
                    case 4 -> {
                        check(notice.isVisible() && notice.getText().contains("foi excluída"), "Exclusão confirmada tratada como falha");
                        check(db.contar("build") == 0, "Exclusão não persistida");
                        MontagemWizardView draft = (MontagemWizardView) tabs.getTabs().get(1).getContent();
                        check(draft.estado().parcial().getId() == 0, "Rascunho da build excluída foi mantido");
                        phase = 5; button(view, "Atualizar lista").fire();
                    }
                    case 5 -> {
                        if (button(view, "Atualizar lista").isDisabled()) return;
                        check(table.getItems().isEmpty() && !notice.isVisible(), "Lista vazia não recuperada");
                        timer.stop(); stage.close(); done.countDown();
                    }
                    default -> throw new AssertionError("Fase inesperada");
                }
            } catch (Throwable error) { fail(error); stage.close(); }
        })));
        timer.setCycleCount(Animation.INDEFINITE); timer.play();
    }

    private static void layout(Scene scene) { scene.getRoot().applyCss(); scene.getRoot().layout(); }
    private static Button button(Parent parent, String text) {
        return parent.lookupAll(".button").stream().filter(node -> node instanceof Button b && b.getText().equals(text))
                .map(node -> (Button) node).findFirst().orElseThrow();
    }
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    private static void fail(Throwable error) { failure = error; if (timer != null) timer.stop(); done.countDown(); }

    private static final class FaultDAO extends BuildDAO {
        final AtomicBoolean failList = new AtomicBoolean();
        final AtomicBoolean failWrite = new AtomicBoolean();
        FaultDAO(BancoTeste db) { super(db::abrir); }
        @Override public List<Build> listarPorUsuario(int id) {
            check(!Platform.isFxApplicationThread(), "JDBC da tela na thread FX");
            if (failList.getAndSet(false)) throw new DataAccessException("Falha simulada na lista", null);
            return super.listarPorUsuario(id);
        }
        @Override public void atualizarDoUsuario(Build build, int userId) {
            check(!Platform.isFxApplicationThread(), "UPDATE na thread FX");
            if (failWrite.getAndSet(false)) throw new DataAccessException("Falha simulada na gravação", null);
            super.atualizarDoUsuario(build, userId);
        }
        @Override public void inserir(Build build) {
            check(!Platform.isFxApplicationThread(), "INSERT na thread FX"); super.inserir(build);
        }
        @Override public void excluirDoUsuario(int id, int userId) {
            check(!Platform.isFxApplicationThread(), "DELETE na thread FX"); super.excluirDoUsuario(id, userId);
        }
    }
}
