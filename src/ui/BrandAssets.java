package ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public final class BrandAssets {

    private static final String LOGO_PATH = "/images/intratech-logo.png";
    private static final Image LOGO = carregarLogo();

    private BrandAssets() {
    }

    public static Image logoImage() {
        return LOGO;
    }

    public static ImageView logo(double tamanho) {
        ImageView view = new ImageView(LOGO);
        view.setFitWidth(tamanho);
        view.setFitHeight(tamanho);
        view.setPreserveRatio(true);
        view.setSmooth(true);
        return view;
    }

    private static Image carregarLogo() {
        try (InputStream stream = BrandAssets.class.getResourceAsStream(LOGO_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Logo da IntraTech não encontrada em " + LOGO_PATH);
            }
            return new Image(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível carregar a logo da IntraTech.", e);
        }
    }
}
