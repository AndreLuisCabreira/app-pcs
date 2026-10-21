package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.Jogo;
import model.RecomendacaoJogo;

import java.net.URL;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Searchable cover grid, independent of persistence so it can be previewed offline. */
public final class GameCatalog extends VBox {
    private static final double WIDTH = 174;
    private static final double COVER_HEIGHT = 232;
    private final TilePane grid = new TilePane(14, 18);
    private final Label count = new Label();

    public GameCatalog(List<RecomendacaoJogo> games, Consumer<RecomendacaoJogo> open,
                       Consumer<Jogo> editCover) {
        super(8);
        setMinHeight(0);
        TextField search = UiSupport.texto("Buscar jogo pelo nome ou gênero...");
        search.setAccessibleText("Buscar jogos");
        HBox.setHgrow(search, Priority.ALWAYS);
        count.getStyleClass().add("game-catalog-count");
        HBox toolbar = new HBox(16, search, count);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("game-catalog-toolbar");
        grid.setPrefColumns(5);
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setTileAlignment(Pos.TOP_LEFT);
        grid.setPadding(new Insets(12, 2, 20, 2));
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("games-scroll");
        scroll.setMinHeight(0);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(toolbar, scroll);

        // Retain cards and their image requests while filtering.
        List<Node> cards = games.stream().map(game -> card(game, open, editCover)).toList();
        Label empty = new Label("Nenhum jogo encontrado. Tente outro nome ou gênero.");
        empty.getStyleClass().add("empty-state");
        Runnable filter = () -> {
            String query = normalize(search.getText());
            grid.getChildren().clear();
            for (int i = 0; i < games.size(); i++) {
                Jogo game = games.get(i).jogo();
                if (normalize(game.getNome() + " " + value(game.getGenero(), "")).contains(query)) {
                    grid.getChildren().add(cards.get(i));
                }
            }
            int visible = grid.getChildren().size();
            count.setText(visible + (visible == 1 ? " jogo" : " jogos"));
            if (visible == 0) grid.getChildren().add(empty);
            scroll.setVvalue(0);
        };
        search.textProperty().addListener((obs, old, text) -> filter.run());
        filter.run();
    }

    private Node card(RecomendacaoJogo recommendation, Consumer<RecomendacaoJogo> open,
                      Consumer<Jogo> editCover) {
        Jogo game = recommendation.jogo();
        StackPane cover = cover(game);
        Label title = new Label(game.getNome());
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        title.getStyleClass().add("game-card-title");
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        title.setMouseTransparent(true);
        cover.getChildren().add(title);

        Label performance = new Label(recommendation.disponivel()
                ? recommendation.benchmark().getFpsMedio() + " FPS • "
                    + recommendation.benchmark().getQualidade()
                : "FPS indisponível");
        performance.setWrapText(true);
        performance.getStyleClass().add(recommendation.disponivel()
                ? "game-status-ready" : "game-status-missing");
        Label resolution = new Label(recommendation.disponivel()
                ? recommendation.benchmark().getResolucao() + " • Configuração recomendada" : "Sem configuração para os filtros selecionados");
        resolution.setWrapText(true);
        resolution.getStyleClass().add("game-card-resolution");
        Label description = new Label(value(game.getDescricao(), value(game.getGenero(),
                "Confira os detalhes da configuração.")));
        description.setWrapText(true);
        description.setMinHeight(30);
        description.setMaxHeight(30);
        description.getStyleClass().add("game-card-description");
        VBox summary = new VBox(5, performance, resolution, description);
        summary.getStyleClass().add("game-card-summary");
        summary.setPrefHeight(108);
        summary.setMinHeight(108);
        summary.setMaxHeight(108);
        VBox body = new VBox(cover, summary);
        body.setMaxWidth(WIDTH);
        Button button = new Button();
        button.setGraphic(body);
        button.setMinWidth(WIDTH + 2);
        button.setPrefWidth(WIDTH + 2);
        button.setMaxWidth(WIDTH + 2);
        button.getStyleClass().add("game-card");
        button.setAccessibleText(game.getNome() + ". " + performance.getText() + ". " + resolution.getText());
        button.setTooltip(new Tooltip(game.getNome() + "\n" + description.getText()
                + "\nClique para ver a configuração."));
        button.setOnAction(event -> open.accept(recommendation));
        // Keyboard focus exposes the same information as mouse hover.
        title.visibleProperty().bind(button.hoverProperty().or(button.focusedProperty()));
        if (editCover == null) return button;
        Button edit = new Button("Trocar capa");
        edit.setMaxWidth(Double.MAX_VALUE);
        edit.setAccessibleText("Trocar capa de " + game.getNome());
        edit.getStyleClass().add("secondary-button");
        edit.setOnAction(event -> editCover.accept(game));
        VBox adminCard = new VBox(6, button, edit);
        adminCard.setMinWidth(WIDTH + 2);
        adminCard.setMaxWidth(WIDTH + 2);
        return adminCard;
    }

    private StackPane cover(Jogo game) {
        StackPane cover = new StackPane();
        cover.setMinSize(WIDTH, COVER_HEIGHT);
        cover.setPrefSize(WIDTH, COVER_HEIGHT);
        cover.setMaxSize(WIDTH, COVER_HEIGHT);
        cover.getStyleClass().add("game-cover");
        Rectangle clip = new Rectangle(WIDTH, COVER_HEIGHT);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        cover.setClip(clip);
        Label fallback = new Label(game.getNome());
        fallback.setWrapText(true);
        fallback.setMaxSize(WIDTH, COVER_HEIGHT);
        fallback.getStyleClass().add("game-cover-fallback");
        cover.getChildren().add(fallback);
        String url = game.getImagemUrl();
        if (url == null || url.isBlank()) {
            URL bundled = GameCatalog.class.getResource("/images/games/" + slug(game.getNome()) + ".jpg");
            url = bundled == null ? null : bundled.toExternalForm();
        } else if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            return cover;
        }
        if (url == null) return cover;
        try {
            Image image = new Image(url, 600, 800, true, true, true);
            ImageView view = new ImageView(image);
            view.setFitWidth(WIDTH);
            view.setFitHeight(COVER_HEIGHT);
            view.setPreserveRatio(false);
            Runnable crop = () -> {
                double w = image.getWidth(), h = image.getHeight();
                if (image.isError()) { cover.getChildren().remove(view); return; }
                if (w <= 0 || h <= 0) return;
                double aspect = WIDTH / COVER_HEIGHT;
                double cw = Math.min(w, h * aspect), ch = cw / aspect;
                view.setViewport(new Rectangle2D((w - cw) / 2, (h - ch) / 2, cw, ch));
            };
            image.progressProperty().addListener((obs, old, progress) -> crop.run());
            image.errorProperty().addListener((obs, old, error) -> crop.run());
            cover.getChildren().add(view);
            crop.run();
        } catch (IllegalArgumentException ignored) {
            // The game remains identifiable when its image is missing or invalid.
        }
        return cover;
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private static String slug(String name) {
        return normalize(name).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private static String value(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }
}
