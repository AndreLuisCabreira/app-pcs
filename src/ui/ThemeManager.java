package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.ToggleButton;

import java.util.Objects;
import java.util.prefs.Preferences;

public final class ThemeManager {

    private static final String PREFERENCE_KEY = "darkMode";
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ThemeManager.class);
    private static final BooleanProperty DARK_MODE = new SimpleBooleanProperty(
            PREFERENCES.getBoolean(PREFERENCE_KEY, false)
    );
    private static final String BASE_STYLESHEET = recurso("/styles.css");
    private static final String DARK_STYLESHEET = recurso("/dark-theme.css");

    static {
        DARK_MODE.addListener((observable, anterior, atual) ->
                PREFERENCES.putBoolean(PREFERENCE_KEY, atual)
        );
    }

    private ThemeManager() {
    }

    public static void instalar(Scene scene) {
        atualizarStylesheet(scene);
        DARK_MODE.addListener((observable, anterior, atual) -> atualizarStylesheet(scene));
    }

    public static ToggleButton criarAlternador() {
        ToggleButton alternador = new ToggleButton();
        alternador.getStyleClass().add("theme-toggle");
        alternador.selectedProperty().bindBidirectional(DARK_MODE);
        alternador.textProperty().bind(Bindings.when(DARK_MODE)
                .then("☀  Usar tema claro")
                .otherwise("☾  Usar tema escuro"));
        alternador.setMaxWidth(Double.MAX_VALUE);
        return alternador;
    }

    public static void estilizar(Dialog<?> dialogo) {
        if (!dialogo.getDialogPane().getStylesheets().contains(BASE_STYLESHEET)) {
            dialogo.getDialogPane().getStylesheets().add(BASE_STYLESHEET);
        }
        if (DARK_MODE.get() && !dialogo.getDialogPane().getStylesheets().contains(DARK_STYLESHEET)) {
            dialogo.getDialogPane().getStylesheets().add(DARK_STYLESHEET);
        }
    }

    private static void atualizarStylesheet(Scene scene) {
        if (DARK_MODE.get()) {
            if (!scene.getStylesheets().contains(DARK_STYLESHEET)) {
                scene.getStylesheets().add(DARK_STYLESHEET);
            }
        } else {
            scene.getStylesheets().remove(DARK_STYLESHEET);
        }
    }

    private static String recurso(String caminho) {
        return Objects.requireNonNull(
                ThemeManager.class.getResource(caminho),
                "Recurso visual não encontrado: " + caminho
        ).toExternalForm();
    }
}
