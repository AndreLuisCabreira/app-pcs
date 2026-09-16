import ui.UiSupport;
import ui.BrandAssets;
import ui.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Usuario;

import java.util.function.Consumer;

public class AutenticacaoView extends StackPane {

    private final AutenticacaoService autenticacaoService = new AutenticacaoService();
    private final Consumer<Usuario> aoAutenticar;

    public AutenticacaoView(Consumer<Usuario> aoAutenticar) {
        this.aoAutenticar = aoAutenticar;
        getStyleClass().add("auth-page");

        HBox card = new HBox(criarApresentacao(), criarFormularios());
        card.setMaxWidth(980);
        card.setMaxHeight(650);
        card.getStyleClass().add("auth-shell");

        getChildren().add(card);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(36));
    }

    private VBox criarApresentacao() {
        javafx.scene.image.ImageView mark = BrandAssets.logo(82);
        mark.getStyleClass().add("auth-logo");
        Label brand = new Label("IntraTech");
        brand.getStyleClass().add("auth-brand");
        Label title = new Label("Monte. Compare.\nJogue melhor.");
        title.getStyleClass().add("auth-hero-title");
        Label description = new Label(
                "Sua área pessoal para criar builds compatíveis e guardar as configurações favoritas."
        );
        description.setWrapText(true);
        description.getStyleClass().add("auth-hero-text");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label privacy = new Label("Cada conta possui builds e favoritos separados.");
        privacy.setWrapText(true);
        privacy.getStyleClass().add("auth-privacy");

        VBox box = new VBox(18, mark, brand, title, description, spacer, privacy);
        box.setPadding(new Insets(46));
        box.setPrefWidth(440);
        box.getStyleClass().add("auth-presentation");
        return box;
    }

    private VBox criarFormularios() {
        ToggleButton themeToggle = ThemeManager.criarAlternador();
        themeToggle.getStyleClass().add("auth-theme-toggle");
        themeToggle.setMaxWidth(190);
        HBox themeBar = new HBox(themeToggle);
        themeBar.setAlignment(Pos.CENTER_RIGHT);
        themeBar.getStyleClass().add("auth-theme-bar");

        Label eyebrow = new Label("ÁREA DO USUÁRIO");
        eyebrow.getStyleClass().add("auth-eyebrow");
        Label title = new Label("Acesse o PC Builder");
        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Entre com sua conta ou faça um cadastro gratuito.");
        subtitle.getStyleClass().add("auth-subtitle");

        Tab login = new Tab("Entrar", criarLogin());
        Tab cadastro = new Tab("Criar conta", criarCadastro());
        login.setClosable(false);
        cadastro.setClosable(false);

        TabPane tabs = new TabPane(login, cadastro);
        tabs.getStyleClass().add("auth-tabs");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        VBox box = new VBox(8, themeBar, eyebrow, title, subtitle, tabs);
        box.setPadding(new Insets(30, 50, 38, 50));
        HBox.setHgrow(box, Priority.ALWAYS);
        box.getStyleClass().add("auth-forms");
        return box;
    }

    private VBox criarLogin() {
        TextField login = UiSupport.texto("seu.login");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Sua senha");
        Label erro = erroLabel();

        Button entrar = new Button("Entrar na minha conta");
        entrar.getStyleClass().add("primary-button");
        entrar.setMaxWidth(Double.MAX_VALUE);
        entrar.setDefaultButton(true);

        Runnable acao = () -> {
            if (entrar.isDisabled()) {
                return;
            }
            erro.setText("");
            entrar.setDisable(true);
            entrar.setText("Entrando...");
            String loginInformado = login.getText();
            String senhaInformada = senha.getText();
            UiSupport.emSegundoPlano(
                    () -> autenticacaoService.autenticar(loginInformado, senhaInformada),
                    usuario -> {
                        entrar.setDisable(false);
                        entrar.setText("Entrar na minha conta");
                        aoAutenticar.accept(usuario);
                    },
                    e -> {
                        entrar.setDisable(false);
                        entrar.setText("Entrar na minha conta");
                        erro.setText(mensagem(e));
                    }
            );
        };
        entrar.setOnAction(event -> acao.run());
        senha.setOnAction(event -> acao.run());

        VBox form = new VBox(
                16,
                UiSupport.campo("LOGIN", login),
                UiSupport.campo("SENHA", senha),
                erro,
                entrar
        );
        form.setPadding(new Insets(24, 0, 0, 0));
        return form;
    }

    private VBox criarCadastro() {
        TextField nome = UiSupport.texto("Nome completo");
        TextField login = UiSupport.texto("Escolha um login");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Mínimo de 8 caracteres");
        PasswordField confirmacao = new PasswordField();
        confirmacao.setPromptText("Repita a senha");
        Label erro = erroLabel();
        Label perfilInfo = new Label(
                "A primeira conta de um banco vazio será administradora. As demais contas serão usuários comuns."
        );
        perfilInfo.setWrapText(true);
        perfilInfo.getStyleClass().add("auth-hint");

        Button cadastrar = new Button("Criar conta e continuar");
        cadastrar.getStyleClass().add("primary-button");
        cadastrar.setMaxWidth(Double.MAX_VALUE);

        Runnable acao = () -> {
            if (cadastrar.isDisabled()) {
                return;
            }
            erro.setText("");
            cadastrar.setDisable(true);
            cadastrar.setText("Criando conta...");
            String nomeInformado = nome.getText();
            String loginInformado = login.getText();
            String senhaInformada = senha.getText();
            String confirmacaoInformada = confirmacao.getText();
            UiSupport.emSegundoPlano(
                    () -> autenticacaoService.cadastrar(
                            nomeInformado, loginInformado, senhaInformada, confirmacaoInformada
                    ),
                    usuario -> {
                        cadastrar.setDisable(false);
                        cadastrar.setText("Criar conta e continuar");
                        aoAutenticar.accept(usuario);
                    },
                    e -> {
                        cadastrar.setDisable(false);
                        cadastrar.setText("Criar conta e continuar");
                        erro.setText(mensagem(e));
                    }
            );
        };
        cadastrar.setOnAction(event -> acao.run());
        confirmacao.setOnAction(event -> acao.run());

        VBox form = new VBox(
                13,
                UiSupport.campo("NOME", nome),
                UiSupport.campo("LOGIN", login),
                UiSupport.campo("SENHA", senha),
                UiSupport.campo("CONFIRMAR SENHA", confirmacao),
                perfilInfo,
                erro,
                cadastrar
        );
        form.setPadding(new Insets(20, 0, 0, 0));
        return form;
    }

    private Label erroLabel() {
        Label erro = new Label();
        erro.setWrapText(true);
        erro.setMinHeight(20);
        erro.getStyleClass().add("auth-error");
        return erro;
    }

    private String mensagem(Throwable erro) {
        return erro.getMessage() == null ? "Não foi possível concluir a operação." : erro.getMessage();
    }
}
