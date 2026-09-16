package ui;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;

public final class WindowFrame extends BorderPane {

    private static final double RESIZE_MARGIN = 6;
    private final Stage stage;
    private final StackPane content = new StackPane();
    private final Button maximizeButton;
    private double dragOffsetX;
    private double dragOffsetY;
    private Cursor resizeCursor = Cursor.DEFAULT;
    private double resizeStartScreenX;
    private double resizeStartScreenY;
    private double resizeStartX;
    private double resizeStartY;
    private double resizeStartWidth;
    private double resizeStartHeight;

    public WindowFrame(Stage stage, String title) {
        this.stage = stage;
        getStyleClass().add("window-frame");

        javafx.scene.image.ImageView logo = BrandAssets.logo(20);
        logo.getStyleClass().add("window-title-logo");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button minimizeButton = windowButton("M5 12 H19", "Minimizar", "window-minimize-button");
        minimizeButton.setOnAction(event -> stage.setIconified(true));

        maximizeButton = windowButton("M6 6 H18 V18 H6 Z", "Maximizar", "window-maximize-button");
        maximizeButton.setOnAction(event -> alternarMaximizacao());

        Button closeButton = windowButton("M6 6 L18 18 M18 6 L6 18", "Fechar", "window-close-button");
        closeButton.setOnAction(event -> stage.close());

        HBox titleBar = new HBox(9, logo, titleLabel, spacer, minimizeButton, maximizeButton, closeButton);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("window-title-bar");
        configurarMovimento(titleBar);

        stage.maximizedProperty().addListener((observable, previous, maximized) -> {
            maximizeButton.setGraphic(windowControlIcon(maximized
                    ? "M8 5 H19 V16 H16 M5 8 H16 V19 H5 Z"
                    : "M6 6 H18 V18 H6 Z"));
            maximizeButton.setAccessibleText(maximized ? "Restaurar" : "Maximizar");
        });

        setTop(titleBar);
        setCenter(content);
        configurarRedimensionamento();
    }

    public void setContent(Node node) {
        content.getChildren().setAll(node);
    }

    private Button windowButton(String path, String accessibleText, String styleClass) {
        Button button = new Button();
        button.setAccessibleText(accessibleText);
        button.setGraphic(windowControlIcon(path));
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setFocusTraversable(false);
        button.getStyleClass().addAll("window-button", styleClass);
        return button;
    }

    private SVGPath windowControlIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#3e4858"));
        icon.setStrokeWidth(1.4);
        icon.setStrokeLineCap(StrokeLineCap.ROUND);
        icon.setStrokeLineJoin(StrokeLineJoin.ROUND);
        icon.setMouseTransparent(true);
        icon.getStyleClass().add("window-control-icon");
        return icon;
    }

    private void configurarMovimento(HBox titleBar) {
        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !stage.isMaximized()) {
                dragOffsetX = event.getScreenX() - stage.getX();
                dragOffsetY = event.getScreenY() - stage.getY();
            }
        });

        titleBar.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY || stage.isMaximized()) {
                return;
            }
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });

        titleBar.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2
                    && !clicouEmBotao(event.getTarget())) {
                alternarMaximizacao();
            }
        });
    }

    private boolean clicouEmBotao(Object target) {
        Node node = target instanceof Node ? (Node) target : null;
        while (node != null) {
            if (node instanceof Button) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private void alternarMaximizacao() {
        stage.setMaximized(!stage.isMaximized());
    }

    private void configurarRedimensionamento() {
        addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (!stage.isMaximized() && resizeCursor == Cursor.DEFAULT) {
                setCursor(cursorNaPosicao(event.getX(), event.getY()));
            }
        });

        addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
            if (resizeCursor == Cursor.DEFAULT) {
                setCursor(Cursor.DEFAULT);
            }
        });

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != MouseButton.PRIMARY || stage.isMaximized()) {
                return;
            }
            Cursor cursor = cursorNaPosicao(event.getX(), event.getY());
            if (!ehCursorDeRedimensionamento(cursor)) {
                return;
            }
            resizeCursor = cursor;
            resizeStartScreenX = event.getScreenX();
            resizeStartScreenY = event.getScreenY();
            resizeStartX = stage.getX();
            resizeStartY = stage.getY();
            resizeStartWidth = stage.getWidth();
            resizeStartHeight = stage.getHeight();
            event.consume();
        });

        addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            if (resizeCursor == Cursor.DEFAULT) {
                return;
            }
            redimensionar(event.getScreenX(), event.getScreenY());
            event.consume();
        });

        addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (resizeCursor != Cursor.DEFAULT) {
                resizeCursor = Cursor.DEFAULT;
                setCursor(cursorNaPosicao(event.getX(), event.getY()));
                event.consume();
            }
        });
    }

    private Cursor cursorNaPosicao(double x, double y) {
        if (stage.isMaximized()) {
            return Cursor.DEFAULT;
        }
        boolean left = x <= RESIZE_MARGIN;
        boolean right = x >= getWidth() - RESIZE_MARGIN;
        boolean top = y <= RESIZE_MARGIN;
        boolean bottom = y >= getHeight() - RESIZE_MARGIN;

        if (left && top) return Cursor.NW_RESIZE;
        if (right && top) return Cursor.NE_RESIZE;
        if (left && bottom) return Cursor.SW_RESIZE;
        if (right && bottom) return Cursor.SE_RESIZE;
        if (left) return Cursor.W_RESIZE;
        if (right) return Cursor.E_RESIZE;
        if (top) return Cursor.N_RESIZE;
        if (bottom) return Cursor.S_RESIZE;
        return Cursor.DEFAULT;
    }

    private boolean ehCursorDeRedimensionamento(Cursor cursor) {
        return cursor == Cursor.N_RESIZE || cursor == Cursor.NE_RESIZE
                || cursor == Cursor.E_RESIZE || cursor == Cursor.SE_RESIZE
                || cursor == Cursor.S_RESIZE || cursor == Cursor.SW_RESIZE
                || cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE;
    }

    private void redimensionar(double screenX, double screenY) {
        double deltaX = screenX - resizeStartScreenX;
        double deltaY = screenY - resizeStartScreenY;
        boolean west = resizeCursor == Cursor.W_RESIZE
                || resizeCursor == Cursor.NW_RESIZE || resizeCursor == Cursor.SW_RESIZE;
        boolean east = resizeCursor == Cursor.E_RESIZE
                || resizeCursor == Cursor.NE_RESIZE || resizeCursor == Cursor.SE_RESIZE;
        boolean north = resizeCursor == Cursor.N_RESIZE
                || resizeCursor == Cursor.NW_RESIZE || resizeCursor == Cursor.NE_RESIZE;
        boolean south = resizeCursor == Cursor.S_RESIZE
                || resizeCursor == Cursor.SW_RESIZE || resizeCursor == Cursor.SE_RESIZE;

        if (east) {
            stage.setWidth(Math.max(stage.getMinWidth(), resizeStartWidth + deltaX));
        }
        if (south) {
            stage.setHeight(Math.max(stage.getMinHeight(), resizeStartHeight + deltaY));
        }
        if (west) {
            double width = Math.max(stage.getMinWidth(), resizeStartWidth - deltaX);
            stage.setX(resizeStartX + resizeStartWidth - width);
            stage.setWidth(width);
        }
        if (north) {
            double height = Math.max(stage.getMinHeight(), resizeStartHeight - deltaY);
            stage.setY(resizeStartY + resizeStartHeight - height);
            stage.setHeight(height);
        }
    }
}
