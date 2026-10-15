package ui;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.Map;

public final class NavigationAssets {

    private static final double ARTBOARD_SIZE = 24.0;
    private static final Map<String, String> ICONS = Map.of(
            "menu", "M4 6 H20 M4 12 H20 M4 18 H20",
            "dashboard", "M4 5 H20 M4 12 H20 M4 19 H20 M8 3 V7 M8 10 V14 M8 17 V21",
            "games", "M8 8 H16 Q20 8 21 13 L22 17 Q22.5 20 20 21 Q18.5 21.5 17 19 L15.5 17 H8.5 L7 19 Q5.5 21.5 4 21 Q1.5 20 2 17 L3 13 Q4 8 8 8 Z M7 12 V16 M5 14 H9 M16.5 12.5 H16.6 M19 15 H19.1",
            "components", "M9 2 V5 M15 2 V5 M9 19 V22 M15 19 V22 M2 9 H5 M2 15 H5 M19 9 H22 M19 15 H22 M7 5 H17 Q19 5 19 7 V17 Q19 19 17 19 H7 Q5 19 5 17 V7 Q5 5 7 5 Z M9 9 H15 V15 H9 Z",
            "builds", "M12 3 L21 8 L12 13 L3 8 Z M3 12 L12 17 L21 12 M3 16 L12 21 L21 16",
            "analysis", "M4 4 V20 H20 M7 16 L11 12 L14 14 L20 7",
            "benchmarks", "M5 20 V13 M10 20 V9 M15 20 V5 M20 20 V11",
            "import", "M12 3 V15 M7.5 10.5 L12 15 L16.5 10.5 M4 19 H20"
    );

    private NavigationAssets() {
    }

    public static StackPane icon(String name, double size) {
        String content = ICONS.get(name);
        if (content == null) {
            throw new IllegalArgumentException("Icone de navegacao desconhecido: " + name);
        }

        SVGPath drawing = new SVGPath();
        drawing.setContent(content);
        drawing.setFill(Color.TRANSPARENT);
        drawing.setStroke(Color.web("#8b929d"));
        drawing.setStrokeWidth(1.7);
        drawing.setStrokeLineCap(StrokeLineCap.ROUND);
        drawing.setStrokeLineJoin(StrokeLineJoin.ROUND);
        drawing.setMouseTransparent(true);
        drawing.getStyleClass().add("nav-icon-path");

        StackPane icon = new StackPane(drawing);
        icon.setMouseTransparent(true);
        icon.getStyleClass().add("nav-icon");
        resize(icon, size);
        return icon;
    }

    public static void resize(StackPane icon, double size) {
        icon.setMinSize(size, size);
        icon.setPrefSize(size, size);
        icon.setMaxSize(size, size);
        if (!icon.getChildren().isEmpty()) {
            double scale = size / ARTBOARD_SIZE;
            icon.getChildren().get(0).setScaleX(scale);
            icon.getChildren().get(0).setScaleY(scale);
        }
    }
}
