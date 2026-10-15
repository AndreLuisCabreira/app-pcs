package ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UiSupport {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("pt-BR")
    );

    private UiSupport() {
    }

    public static VBox pagina(String titulo, String subtitulo, Node conteudo) {
        Label title = new Label(titulo);
        title.getStyleClass().add("page-title");
        Label subtitle = new Label(subtitulo);
        subtitle.getStyleClass().add("page-subtitle");

        VBox box = new VBox(8, title, subtitle, conteudo);
        box.setPadding(new Insets(32));
        box.getStyleClass().add("page");
        VBox.setVgrow(conteudo, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }

    public static VBox campo(String rotulo, Node controle) {
        Label label = new Label(rotulo);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(6, label, controle);
        box.getStyleClass().add("field-group");
        return box;
    }

    public static TextField texto(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    public static int inteiro(TextField campo, String nome) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nome + " deve ser um número inteiro não negativo.");
        }
    }

    public static double decimal(TextField campo, String nome) {
        try {
            double valor = Double.parseDouble(campo.getText().trim().replace(',', '.'));
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nome + " deve ser um número não negativo.");
        }
    }

    public static String obrigatorio(TextField campo, String nome) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException(nome + " é obrigatório.");
        }
        return valor;
    }

    public static String moeda(double valor) {
        return MOEDA.format(valor);
    }

    public static void sucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        alert.setHeaderText("Operação concluída");
        alert.setTitle("IntraTech");
        ThemeManager.estilizar(alert);
        alert.showAndWait();
    }

    public static void erro(Throwable erro) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensagem(erro), ButtonType.OK);
        alert.setHeaderText("Não foi possível concluir a operação");
        alert.setTitle("IntraTech");
        ThemeManager.estilizar(alert);
        alert.showAndWait();
    }

    public static boolean confirmar(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.CANCEL, ButtonType.OK);
        alert.setHeaderText("Confirme a operação");
        alert.setTitle("IntraTech");
        ThemeManager.estilizar(alert);
        Optional<ButtonType> resposta = alert.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.OK;
    }

    public static <T> void configurarCombo(ComboBox<T> combo, Function<T, String> texto) {
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item == null ? "" : texto.apply(item);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }

    public static <T> TableColumn<T, String> coluna(
            String titulo,
            Function<T, String> valor,
            double largura
    ) {
        TableColumn<T, String> coluna = new TableColumn<>(titulo);
        coluna.setCellValueFactory(dado -> new SimpleStringProperty(valor.apply(dado.getValue())));
        coluna.setPrefWidth(largura);
        return coluna;
    }

    public static <T> void emSegundoPlano(
            Supplier<T> trabalho,
            Consumer<T> sucesso,
            Consumer<Throwable> falha
    ) {
        Thread.ofVirtual().name("intratech-worker").start(() -> {
            try {
                T resultado = trabalho.get();
                Platform.runLater(() -> sucesso.accept(resultado));
            } catch (Throwable erro) {
                Platform.runLater(() -> falha.accept(erro));
            }
        });
    }

    public static <T> void executarComFeedback(
            javafx.scene.control.Button botao,
            String textoCarregando,
            Supplier<T> trabalho,
            Consumer<T> sucesso
    ) {
        if (botao.isDisabled()) {
            return;
        }
        String textoOriginal = botao.getText();
        botao.setDisable(true);
        botao.setText(textoCarregando);
        emSegundoPlano(trabalho, resultado -> {
            restaurarBotao(botao, textoOriginal);
            try {
                sucesso.accept(resultado);
            } catch (Throwable erro) {
                UiSupport.erro(erro);
            }
        }, erro -> {
            restaurarBotao(botao, textoOriginal);
            UiSupport.erro(erro);
        });
    }

    private static void restaurarBotao(javafx.scene.control.Button botao, String textoOriginal) {
        botao.setText(textoOriginal);
        botao.setDisable(false);
    }

    private static String mensagem(Throwable erro) {
        String mensagem = erro.getMessage();
        if (mensagem == null || mensagem.isBlank()) {
            return "Ocorreu um erro inesperado.";
        }
        return mensagem;
    }
}
