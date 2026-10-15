package ui;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Content host. Movement, sizing, snapping and caption controls belong to the OS. */
public final class WindowFrame extends BorderPane {
    private final StackPane content = new StackPane();

    public WindowFrame(Stage stage, String title) {
        stage.setTitle(title);
        getStyleClass().add("window-frame");
        setCenter(content);
    }

    public void setContent(Node node) {
        content.getChildren().setAll(node);
    }
}
