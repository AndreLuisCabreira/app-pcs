import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.Usuario;
import ui.BrandAssets;
import ui.ThemeManager;
import ui.WindowFrame;

public class DesktopApp extends Application {

    private Scene scene;
    private WindowFrame windowFrame;

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);
        windowFrame = new WindowFrame(stage, "IntraTech — PC Builder");
        scene = new Scene(windowFrame, 1280, 800);
        scene.getStylesheets().add(
                DesktopApp.class.getResource("/styles.css").toExternalForm()
        );
        ThemeManager.instalar(scene);

        stage.setTitle("IntraTech — PC Builder");
        stage.getIcons().add(BrandAssets.logoImage());
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
        mostrarInicializacao();
        ui.UiSupport.emSegundoPlano(() -> {
            new DatabaseSchemaValidator().verificar();
            return true;
        }, ignorado -> mostrarAutenticacao(), this::mostrarErroInicializacao);
    }

    @Override
    public void stop() {
        ConnectionFactory.fechar();
    }

    private void mostrarInicializacao() {
        ProgressIndicator indicador = new ProgressIndicator();
        Label mensagem = new Label("Verificando conexão e estrutura do banco...");
        mensagem.getStyleClass().add("loading-title");
        VBox caixa = new VBox(14, indicador, mensagem);
        caixa.setAlignment(Pos.CENTER);
        caixa.getStyleClass().add("loading-view");
        windowFrame.setContent(new StackPane(caixa));
    }

    private void mostrarErroInicializacao(Throwable erroOriginal) {
        Label erro = new Label(erroOriginal.getMessage());
        erro.setWrapText(true);
        erro.getStyleClass().add("startup-error");
        windowFrame.setContent(new StackPane(erro));
    }

    private void mostrarAutenticacao() {
        windowFrame.setContent(new AutenticacaoView(this::mostrarAplicacao));
    }

    private void mostrarAplicacao(Usuario usuario) {
        windowFrame.setContent(new AppShell(usuario, this::mostrarAutenticacao));
    }
}
