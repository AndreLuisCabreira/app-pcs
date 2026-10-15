import ui.UiSupport;
import ui.BrandAssets;
import ui.NavigationAssets;
import ui.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.Usuario;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.function.Supplier;

public class AppShell extends BorderPane {

    private static final double SIDEBAR_EXPANDIDA = 244;
    private static final double SIDEBAR_RECOLHIDA = 84;
    private static final String PREFERENCIA_SIDEBAR = "sidebarCollapsed";
    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(AppShell.class);

    private final Usuario usuario;
    private final Runnable aoSair;
    private final StackPane content = new StackPane();
    private final Map<Button, Supplier<Node>> navigation = new LinkedHashMap<>();
    private final Map<Button, NavigationItem> navigationItems = new LinkedHashMap<>();
    private Button activeButton;
    private VBox sidebar;
    private boolean sidebarCollapsed = PREFERENCIAS.getBoolean(PREFERENCIA_SIDEBAR, false);
    private long requisicaoNavegacao;

    public AppShell(Usuario usuario, Runnable aoSair) {
        this.usuario = usuario;
        this.aoSair = aoSair;
        getStyleClass().add("app-shell");
        setLeft(criarSidebar());
        setCenter(content);
        abrir(activeButton, navigation.get(activeButton));
    }

    private VBox criarSidebar() {
        javafx.scene.image.ImageView mark = BrandAssets.logo(48);
        mark.getStyleClass().add("brand-logo");
        Label name = new Label("IntraTech");
        name.getStyleClass().add("brand-name");
        Label product = new Label("PC BUILDER");
        product.getStyleClass().add("brand-product");

        VBox brandText = new VBox(1, name, product);
        HBox brand = new HBox(12, mark, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("brand");

        Button collapseButton = new Button();
        collapseButton.setGraphic(NavigationAssets.icon("menu", 24));
        collapseButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        collapseButton.setMinSize(36, 36);
        collapseButton.setPrefSize(36, 36);
        collapseButton.setMaxSize(36, 36);
        collapseButton.getStyleClass().add("sidebar-collapse");
        collapseButton.setTooltip(new Tooltip("Recolher menu"));
        HBox collapseRow = new HBox(collapseButton);
        collapseRow.setAlignment(Pos.CENTER_RIGHT);
        collapseRow.getStyleClass().add("sidebar-collapse-row");

        VBox menu = new VBox(6);
        Button dashboard = navButton("dashboard", "Dashboard", () -> new DashboardView(usuario));
        navButton("games", "Jogos", () -> new JogosView(usuario));
        if (usuario.isAdmin()) {
            navButton("components", "Componentes", () -> new ComponentesView(usuario));
            navButton("import", "Importar catálogo", () -> new ImportacaoCatalogoView(usuario));
            navButton("benchmarks", "Benchmarks", () -> new BenchmarksView(usuario));
            navButton("builds", "Minhas builds", () -> new BuildsView(usuario));
            navButton("analysis", "Análises", () -> new AnalisesView(usuario));
        } else {
            navButton("builds", "Minhas builds", () -> new BuildsView(usuario));
            navButton("analysis", "Análises", () -> new AnalisesView(usuario));
        }
        menu.getChildren().addAll(navigation.keySet());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label appearanceTitle = new Label("APARÊNCIA");
        appearanceTitle.getStyleClass().add("status-title");
        ToggleButton themeToggle = ThemeManager.criarAlternador();
        Label themeIcon = new Label();
        themeIcon.textProperty().bind(Bindings.when(themeToggle.selectedProperty())
                .then("☀")
                .otherwise("☾"));
        themeIcon.textFillProperty().bind(themeToggle.textFillProperty());
        themeToggle.setGraphic(themeIcon);
        themeToggle.setTooltip(new Tooltip("Alternar entre tema claro e escuro"));

        Label statusTitle = new Label("BANCO DE DADOS");
        statusTitle.getStyleClass().add("status-title");
        Label status = new Label("●  Verificando conexão...");
        status.getStyleClass().add("status-warning");
        UiSupport.emSegundoPlano(this::testarConexao, bancoDisponivel -> {
            status.setText(bancoDisponivel
                    ? "●  " + ConnectionFactory.nomeBanco() + " conectado"
                    : "●  Banco indisponível");
            status.getStyleClass().removeAll("status-warning", "status-online", "status-offline");
            status.getStyleClass().add(bancoDisponivel ? "status-online" : "status-offline");
        }, erro -> {
            status.setText("●  Banco indisponível");
            status.getStyleClass().removeAll("status-warning", "status-online");
            status.getStyleClass().add("status-offline");
        });

        Label userName = new Label(usuario.getNome());
        userName.getStyleClass().add("sidebar-user-name");
        Label userLogin = new Label("@" + usuario.getLogin());
        userLogin.getStyleClass().add("sidebar-user-login");
        Label avatar = new Label(iniciais(usuario.getNome()));
        avatar.getStyleClass().add("sidebar-user-avatar");
        VBox identityText = new VBox(2, userName, userLogin);
        HBox identity = new HBox(10, avatar, identityText);
        identity.setAlignment(Pos.CENTER_LEFT);
        Label userRole = new Label(usuario.isAdmin() ? "ADMINISTRADOR" : "USUÁRIO");
        userRole.getStyleClass().add(usuario.isAdmin() ? "role-admin" : "role-user");
        Button logout = new Button("Sair da conta");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.getStyleClass().add("logout-button");
        logout.setOnAction(event -> {
            if (UiSupport.confirmar("Deseja sair da conta atual?")) {
                aoSair.run();
            }
        });
        VBox account = new VBox(7, identity, userRole, logout);
        account.setPadding(new Insets(10));
        account.getStyleClass().add("sidebar-account");

        sidebar = new VBox(
                10,
                brand,
                collapseRow,
                menu,
                spacer,
                appearanceTitle,
                themeToggle,
                statusTitle,
                status,
                account
        );
        sidebar.getStyleClass().add("sidebar");

        List<Node> detalhes = List.of(
                brandText,
                appearanceTitle,
                statusTitle,
                status,
                identityText,
                userRole,
                logout
        );
        collapseButton.setOnAction(event -> {
            sidebarCollapsed = !sidebarCollapsed;
            PREFERENCIAS.putBoolean(PREFERENCIA_SIDEBAR, sidebarCollapsed);
            aplicarEstadoSidebar(
                    brand,
                    identity,
                    account,
                    collapseRow,
                    collapseButton,
                    themeToggle,
                    detalhes
            );
        });
        aplicarEstadoSidebar(brand, identity, account, collapseRow, collapseButton, themeToggle, detalhes);

        activeButton = dashboard;
        dashboard.getStyleClass().add("nav-active");
        return sidebar;
    }

    private Button navButton(String iconName, String title, Supplier<Node> factory) {
        StackPane icon = NavigationAssets.icon(iconName, 24);
        Button button = new Button(title, icon);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(12);
        button.getStyleClass().add("nav-button");
        button.setTooltip(new Tooltip(title));
        button.setOnAction(event -> abrir(button, factory));
        navigation.put(button, factory);
        navigationItems.put(button, new NavigationItem(title, icon));
        return button;
    }

    private void aplicarEstadoSidebar(
            HBox brand,
            HBox identity,
            VBox account,
            HBox collapseRow,
            Button collapseButton,
            ToggleButton themeToggle,
            List<Node> detalhes
    ) {
        double largura = sidebarCollapsed ? SIDEBAR_RECOLHIDA : SIDEBAR_EXPANDIDA;
        sidebar.setMinWidth(largura);
        sidebar.setPrefWidth(largura);
        sidebar.setMaxWidth(largura);
        sidebar.setPadding(sidebarCollapsed
                ? new Insets(18, 10, 14, 10)
                : new Insets(18, 20, 14, 20));

        if (sidebarCollapsed) {
            if (!sidebar.getStyleClass().contains("sidebar-collapsed")) {
                sidebar.getStyleClass().add("sidebar-collapsed");
            }
        } else {
            sidebar.getStyleClass().remove("sidebar-collapsed");
        }

        detalhes.forEach(node -> {
            node.setVisible(!sidebarCollapsed);
            node.setManaged(!sidebarCollapsed);
        });
        brand.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        identity.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        account.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.TOP_LEFT);
        collapseRow.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_RIGHT);
        account.setPadding(sidebarCollapsed ? new Insets(8) : new Insets(10));
        collapseButton.getTooltip().setText(sidebarCollapsed ? "Expandir menu" : "Recolher menu");
        themeToggle.setContentDisplay(sidebarCollapsed
                ? ContentDisplay.GRAPHIC_ONLY
                : ContentDisplay.TEXT_ONLY);
        themeToggle.setAlignment(sidebarCollapsed ? Pos.CENTER : Pos.CENTER_LEFT);

        navigationItems.forEach((button, item) -> {
            button.setText(sidebarCollapsed ? "" : item.title());
            button.setContentDisplay(sidebarCollapsed
                    ? ContentDisplay.GRAPHIC_ONLY
                    : ContentDisplay.LEFT);
            button.setGraphicTextGap(sidebarCollapsed ? 0 : 12);
            NavigationAssets.resize(item.icon(), sidebarCollapsed ? 28 : 24);
        });
    }

    private void abrir(Button button, Supplier<Node> factory) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-active");
        }
        activeButton = button;
        if (!button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
        long requisicaoAtual = ++requisicaoNavegacao;
        content.getChildren().setAll(criarCarregamento());
        navigation.keySet().forEach(item -> item.setDisable(true));
        UiSupport.emSegundoPlano(factory, pagina -> {
            if (requisicaoAtual == requisicaoNavegacao) {
                content.getChildren().setAll(pagina);
            }
            navigation.keySet().forEach(item -> item.setDisable(false));
        }, erro -> {
            navigation.keySet().forEach(item -> item.setDisable(false));
            Label mensagem = new Label("Não foi possível carregar esta tela. Tente novamente.");
            mensagem.getStyleClass().add("startup-error");
            content.getChildren().setAll(mensagem);
            UiSupport.erro(erro);
        });
    }

    private VBox criarCarregamento() {
        ProgressIndicator indicador = new ProgressIndicator();
        indicador.setMaxSize(44, 44);
        Label mensagem = new Label("Carregando dados do Supabase...");
        mensagem.getStyleClass().add("loading-title");
        VBox carregamento = new VBox(14, indicador, mensagem);
        carregamento.setAlignment(Pos.CENTER);
        carregamento.getStyleClass().add("loading-view");
        return carregamento;
    }

    private String iniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase();
    }

    private boolean testarConexao() {
        try (Connection connection = ConnectionFactory.getConexao()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private record NavigationItem(String title, StackPane icon) {
    }
}
