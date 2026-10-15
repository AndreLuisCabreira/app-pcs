import ui.UiSupport;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Build;
import model.Usuario;

import java.util.List;

public class DashboardView extends VBox {

    private final List<Build> builds;
    private final FlowPane buildCards = new FlowPane(18, 18);
    private final Label emptyState = new Label();

    public DashboardView(Usuario usuario) {
        builds = new BuildDAO().listarPorUsuario(usuario.getId());
        int componentes = new ProcessadorDAO().listar().size()
                + new PlacaMaeDAO().listar().size()
                + new PlacaVideoDAO().listar().size()
                + new MemoriaDAO().listar().size()
                + new SSDDAO().listar().size()
                + new FonteDAO().listar().size();

        FlowPane metrics = new FlowPane(16, 16);
        metrics.getChildren().addAll(
                metric("MINHAS BUILDS", String.valueOf(builds.size()), "Montagens da sua conta"),
                metric("FAVORITAS", String.valueOf(builds.stream().filter(Build::isFavorita).count()), "Favoritas da sua conta"),
                metric("COMPONENTES", String.valueOf(componentes), "Itens no catálogo")
        );

        Label section = new Label("Suas builds");
        section.getStyleClass().add("section-title");
        Label sectionHint = new Label("Uma visão rápida das peças principais de cada montagem.");
        sectionHint.getStyleClass().add("muted-text");
        VBox sectionText = new VBox(3, section, sectionHint);

        ToggleButton favoritesFilter = new ToggleButton("☆  Mostrar favoritas");
        favoritesFilter.getStyleClass().add("favorite-filter");
        favoritesFilter.setOnAction(event -> {
            favoritesFilter.setText(favoritesFilter.isSelected()
                    ? "★  Somente favoritas"
                    : "☆  Mostrar favoritas");
            atualizarCards(favoritesFilter.isSelected());
        });

        Region sectionSpacer = new Region();
        HBox.setHgrow(sectionSpacer, Priority.ALWAYS);
        HBox sectionHeader = new HBox(16, sectionText, sectionSpacer, favoritesFilter);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);

        buildCards.setAlignment(Pos.TOP_LEFT);
        buildCards.setPrefWrapLength(920);
        buildCards.getStyleClass().add("build-grid");

        emptyState.getStyleClass().add("empty-state");
        atualizarCards(false);

        ScrollPane cardsScroll = new ScrollPane(buildCards);
        cardsScroll.setFitToWidth(true);
        cardsScroll.setPannable(true);
        cardsScroll.getStyleClass().add("dashboard-scroll");
        buildCards.prefWrapLengthProperty().bind(
                Bindings.max(330, cardsScroll.widthProperty().subtract(28))
        );
        VBox.setVgrow(cardsScroll, Priority.ALWAYS);

        VBox content = new VBox(24, metrics, sectionHeader, cardsScroll);
        content.setPadding(new Insets(24, 0, 0, 0));
        VBox.setVgrow(cardsScroll, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Olá, " + usuario.getNome(),
                "Acompanhe apenas as suas builds, favoritas e análises.",
                content
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    private void atualizarCards(boolean apenasFavoritas) {
        List<Build> visiveis = builds.stream()
                .filter(build -> !apenasFavoritas || build.isFavorita())
                .toList();

        buildCards.getChildren().clear();
        if (visiveis.isEmpty()) {
            emptyState.setText(apenasFavoritas
                    ? "Você ainda não marcou nenhuma build como favorita."
                    : "Nenhuma build cadastrada ainda.");
            buildCards.getChildren().add(emptyState);
            return;
        }

        visiveis.forEach(build -> buildCards.getChildren().add(buildCard(build)));
    }

    private VBox buildCard(Build build) {
        Label buildIcon = new Label("▦");
        buildIcon.getStyleClass().add("build-main-icon");

        Label name = new Label(build.getNome());
        name.setWrapText(true);
        name.getStyleClass().add("build-card-title");
        Label summary = new Label("Configuração personalizada");
        summary.getStyleClass().add("build-card-subtitle");
        VBox titleBox = new VBox(2, name, summary);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label favorite = new Label(build.isFavorita() ? "★" : "☆");
        favorite.getStyleClass().add(build.isFavorita() ? "build-star-active" : "build-star");

        HBox header = new HBox(12, buildIcon, titleBox, favorite);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox parts = new VBox(9,
                part("CPU", build.getProcessador().getNome()),
                part("GPU", build.getPlacaVideo().getNome()),
                part("RAM", build.getMemoria().getNome())
        );

        Label totalCaption = new Label("VALOR DA BUILD");
        totalCaption.getStyleClass().add("build-total-caption");
        Label total = new Label(UiSupport.moeda(build.getPrecoTotal()));
        total.getStyleClass().add("build-total");
        VBox price = new VBox(2, totalCaption, total);

        VBox card = new VBox(18, header, parts, price);
        card.setPadding(new Insets(20));
        card.setPrefWidth(330);
        card.setMaxWidth(360);
        card.getStyleClass().add("build-card");
        if (build.isFavorita()) {
            card.getStyleClass().add("build-card-favorite");
        }
        return card;
    }

    private HBox part(String icon, String value) {
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("build-part-icon");
        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.getStyleClass().add("build-part-value");
        HBox.setHgrow(valueLabel, Priority.ALWAYS);

        HBox row = new HBox(11, iconLabel, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("build-part-row");
        return row;
    }

    private VBox metric(String eyebrow, String value, String caption) {
        Label eyebrowLabel = new Label(eyebrow);
        eyebrowLabel.getStyleClass().add("metric-eyebrow");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("metric-caption");

        VBox card = new VBox(5, eyebrowLabel, valueLabel, captionLabel);
        card.setPadding(new Insets(20));
        card.setPrefWidth(210);
        card.getStyleClass().add("metric-card");
        return card;
    }
}
