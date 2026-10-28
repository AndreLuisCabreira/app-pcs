import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.Build;
import model.Usuario;
import service.BuildExportService;
import ui.ThemeManager;
import ui.UiSupport;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class BuildDialogs {
    private static final BuildExportService EXPORT = new BuildExportService();
    private BuildDialogs() { }

    public static void exportar(Build build, Usuario usuario, Window janela) {
        EXPORT.exigirProprietario(build, usuario);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar build");
        FileChooser.ExtensionFilter html = new FileChooser.ExtensionFilter("Página HTML (imprimível em PDF)", "*.html");
        FileChooser.ExtensionFilter txt = new FileChooser.ExtensionFilter("Texto", "*.txt");
        chooser.getExtensionFilters().addAll(html, txt);
        chooser.setInitialFileName("build-" + build.getId() + ".html");
        File arquivo = chooser.showSaveDialog(janela);
        if (arquivo == null) return;
        boolean texto = chooser.getSelectedExtensionFilter() == txt;
        String conteudo = texto ? EXPORT.texto(build, usuario) : EXPORT.html(List.of(build), usuario);
        salvar(arquivo, conteudo);
    }

    public static void comparar(List<Build> builds, Usuario usuario, Window janela) {
        if (builds.size() < 2 || builds.size() > 3) throw new IllegalArgumentException("Selecione duas ou três builds com Ctrl ou Shift.");
        builds.forEach(b -> EXPORT.exigirProprietario(b, usuario));
        if (builds.stream().map(Build::getId).distinct().count() != builds.size()) throw new IllegalArgumentException("Selecione builds diferentes.");
        TableView<Linha> tabela = new TableView<>();
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.getColumns().add(coluna("Item", 0));
        for (int i=0; i<builds.size(); i++) tabela.getColumns().add(coluna(builds.get(i).getNome(), i+1));
        List<Linha> linhas = new ArrayList<>();
        for (String chave : EXPORT.resumo(builds.getFirst()).keySet()) {
            List<String> valores = new ArrayList<>(); valores.add(chave);
            builds.forEach(b -> valores.add(EXPORT.resumo(b).get(chave)));
            linhas.add(new Linha(valores));
        }
        double menor = builds.stream().mapToDouble(Build::getPrecoTotal).min().orElse(0);
        List<String> diferencas = new ArrayList<>(); diferencas.add("Diferença para a mais barata");
        builds.forEach(b -> diferencas.add(BuildExportService.moeda(b.getPrecoTotal()-menor)));
        linhas.add(new Linha(diferencas));
        tabela.getItems().setAll(linhas);
        tabela.setPrefHeight(430);
        tabela.getStyleClass().add("data-table");
        Button exportar = new Button("Exportar comparação HTML / PDF");
        exportar.getStyleClass().add("secondary-button");
        exportar.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("comparacao-builds.html");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML imprimível", "*.html"));
            File arquivo = chooser.showSaveDialog(janela);
            if (arquivo != null) salvar(arquivo, EXPORT.html(builds, usuario));
        });
        Label nota = new Label("Compare custo, peças e consumo. FPS continua disponível em Análises, conforme os benchmarks cadastrados.");
        nota.setWrapText(true);
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(janela); dialog.setTitle("Comparar builds");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(950);
        dialog.getDialogPane().setContent(new VBox(12, tabela, nota, exportar));
        ThemeManager.estilizar(dialog); dialog.showAndWait();
    }

    private static TableColumn<Linha,String> coluna(String nome, int indice) {
        TableColumn<Linha,String> c = new TableColumn<>(nome);
        c.setPrefWidth(indice==0 ? 220 : 235);
        c.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().valores().get(indice)));
        return c;
    }
    private static void salvar(File arquivo, String conteudo) {
        UiSupport.emSegundoPlano(() -> {
                    try { Files.writeString(arquivo.toPath(), conteudo, StandardCharsets.UTF_8); return true; }
                    catch (java.io.IOException e) { throw new java.io.UncheckedIOException("Não foi possível salvar o arquivo.", e); }
                },
                ok -> UiSupport.sucesso("Arquivo exportado."), UiSupport::erro);
    }
    private record Linha(List<String> valores) { }
}
