import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import model.Usuario;
import ui.UiSupport;

public final class ContaView extends VBox {
    public ContaView(Usuario usuario) { this(usuario, () -> {}); }

    public ContaView(Usuario usuario, Runnable aoRestaurar) {
        PasswordField atual = new PasswordField();
        PasswordField nova = new PasswordField();
        PasswordField confirmacao = new PasswordField();
        nova.setPromptText("Mínimo de 8 caracteres");
        Button salvar = new Button("Alterar minha senha");
        salvar.getStyleClass().add("primary-button");
        Label info = new Label("Confirme sua senha atual para escolher uma nova. Sua conta e suas builds serão preservadas.");
        info.setWrapText(true);
        info.getStyleClass().add("muted-text");
        salvar.setOnAction(event -> {
            String senhaAtual = atual.getText(), senhaNova = nova.getText(), repetir = confirmacao.getText();
            atual.clear(); nova.clear(); confirmacao.clear();
            UiSupport.executarComFeedback(salvar, "Alterando...", () -> {
                new AutenticacaoService().alterarSenha(usuario, senhaAtual, senhaNova, repetir);
                return true;
            }, ok -> UiSupport.sucesso("Senha alterada. Use a nova senha no próximo acesso."));
        });
        VBox form = new VBox(16, info, UiSupport.campo("SENHA ATUAL", atual),
                UiSupport.campo("NOVA SENHA", nova), UiSupport.campo("CONFIRMAR NOVA SENHA", confirmacao), salvar);
        if (usuario.isAdmin()) {
            Label aviso = new Label("Backup inclui contas, hashes de senha, builds e catálogo. Guarde o arquivo em local privado. Restaure durante uma janela sem outras gravações.");
            aviso.setWrapText(true);
            Button backup = new Button("Salvar backup do banco");
            Button restaurar = new Button("Restaurar backup");
            backup.getStyleClass().add("secondary-button");
            restaurar.getStyleClass().add("danger-button");
            backup.setOnAction(event -> backup(usuario, false, backup, aoRestaurar));
            restaurar.setOnAction(event -> backup(usuario, true, restaurar, aoRestaurar));
            form.getChildren().addAll(new javafx.scene.control.Separator(), new Label("BACKUP ADMINISTRATIVO"), aviso, backup, restaurar);
        }
        form.setMaxWidth(600);
        form.setPadding(new javafx.geometry.Insets(24));
        form.getStyleClass().add("panel-card");
        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("transparent-scroll");
        getChildren().add(UiSupport.pagina("Minha conta", usuario.getNome() + " · @" + usuario.getLogin(), scroll));
        VBox.setVgrow(getChildren().getFirst(), javafx.scene.layout.Priority.ALWAYS);
    }

    private void backup(Usuario usuario, boolean restaurar, Button botao, Runnable aoRestaurar) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Backup IntraTech", "*.json"));
        chooser.setTitle(restaurar ? "Escolher backup para restaurar" : "Salvar backup privado");
        if (!restaurar) chooser.setInitialFileName("intratech-backup-" + java.time.LocalDate.now() + ".json");
        java.io.File arquivo = restaurar ? chooser.showOpenDialog(getScene().getWindow()) : chooser.showSaveDialog(getScene().getWindow());
        if (arquivo == null) return;
        if (restaurar && !UiSupport.confirmar("Substituir todos os dados atuais de contas, builds e catálogo pelo backup? Uma restauração concluída não pode ser desfeita sem outro backup. Você será desconectado.")) return;
        PasswordField senha = new PasswordField();
        javafx.scene.control.Dialog<String> dialog = new javafx.scene.control.Dialog<>();
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Confirmar conta administradora");
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.getDialogPane().setContent(UiSupport.campo("SUA SENHA ATUAL", senha));
        dialog.setResultConverter(b -> b == javafx.scene.control.ButtonType.OK ? senha.getText() : null);
        ui.ThemeManager.estilizar(dialog);
        dialog.showAndWait().ifPresent(atual -> {
            senha.clear();
            UiSupport.executarComFeedback(botao, restaurar ? "Restaurando..." : "Salvando backup...", () -> {
                try {
                    DatabaseBackupService service = new DatabaseBackupService();
                    if (restaurar) service.restaurar(usuario, atual, arquivo.toPath());
                    else service.exportar(usuario, atual, arquivo.toPath());
                    return true;
                } catch (java.sql.SQLException | java.io.IOException e) {
                    throw new IllegalStateException(restaurar ? "Não foi possível restaurar o backup. Confira o arquivo e a estrutura do banco antes de tentar novamente." : "Não foi possível salvar o backup. Confira conexão, estrutura e destino.", e);
                }
            }, ok -> {
                UiSupport.sucesso(restaurar ? "Backup restaurado. Entre novamente com uma conta do backup." : "Backup salvo. O arquivo contém informações privadas.");
                if (restaurar) aoRestaurar.run();
            });
        });
    }
}
