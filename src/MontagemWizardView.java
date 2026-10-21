import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.*;
import service.CatalogoFiltro;
import service.MontagemPC;
import service.MontagemPC.Etapa;
import ui.UiSupport;
import java.util.List;
import java.util.Comparator;
import java.util.function.Consumer;

/** One visible component category at a time, backed by a draft independent of the controls. */
public final class MontagemWizardView extends BorderPane {
    private final MontagemPC estado;
    private final MontagemPC.Catalogo catalogo;
    private final Consumer<Build> aoSalvar;
    private final VBox principal = new VBox(14);
    private final VBox resumo = new VBox(10);
    private final FlowPane etapas = new FlowPane(6, 6);
    private final Label titulo = new Label();
    private final Label subtitulo = new Label();
    private final Label aviso = new Label();
    private final Label selecionado = new Label();
    private final ProgressBar progresso = new ProgressBar();
    private final Button voltar = new Button("← Voltar");
    private final Button continuar = new Button("Continuar →");
    private final Button salvar = new Button();

    public MontagemWizardView(MontagemPC estado, MontagemPC.Catalogo catalogo, Consumer<Build> aoSalvar) {
        this.estado = estado; this.catalogo = catalogo; this.aoSalvar = aoSalvar;
        getStyleClass().add("montagem-wizard");
        setPadding(new Insets(18, 16, 14, 16));
        titulo.getStyleClass().add("montagem-title");
        subtitulo.getStyleClass().add("muted-text"); subtitulo.setWrapText(true);
        aviso.getStyleClass().add("montagem-notice"); aviso.setWrapText(true);
        progresso.setMaxWidth(Double.MAX_VALUE); progresso.getStyleClass().add("montagem-progress");
        VBox cabecalho = new VBox(10, etapas, titulo, subtitulo, progresso, aviso);
        cabecalho.setPadding(new Insets(0, 0, 14, 0));
        setTop(cabecalho);

        principal.setMinWidth(0); principal.setPadding(new Insets(20));
        principal.getStyleClass().add("panel-card");
        HBox.setHgrow(principal, Priority.ALWAYS);
        resumo.setPadding(new Insets(18)); resumo.getStyleClass().add("montagem-summary");
        ScrollPane resumoScroll = new ScrollPane(resumo);
        resumoScroll.setFitToWidth(true); resumoScroll.setPrefWidth(255); resumoScroll.setMinWidth(220);
        resumoScroll.setMaxWidth(275); resumoScroll.getStyleClass().add("transparent-scroll");
        HBox centro = new HBox(16, principal, resumoScroll); centro.setMinHeight(0);
        setCenter(centro);

        voltar.getStyleClass().add("secondary-button");
        continuar.getStyleClass().add("primary-button"); salvar.getStyleClass().add("primary-button");
        voltar.setOnAction(e -> { estado.voltar(); aviso.setText(""); mostrarEtapa(); });
        continuar.setOnAction(e -> {
            try { estado.avancar(); aviso.setText(""); mostrarEtapa(); }
            catch (RuntimeException erro) { aviso.setText(erro.getMessage()); }
        });
        salvar.setOnAction(e -> {
            try { aoSalvar.accept(estado.finalizar()); }
            catch (RuntimeException erro) { aviso.setText(erro.getMessage()); }
        });
        Region espaco = new Region(); HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox rodape = new HBox(12, voltar, espaco, continuar, salvar);
        rodape.setAlignment(Pos.CENTER_LEFT); rodape.setPadding(new Insets(14, 0, 0, 0));
        setBottom(rodape);
        mostrarEtapa();
    }

    public MontagemPC estado() { return estado; }

    private void mostrarEtapa() {
        Etapa atual = estado.etapa();
        titulo.setText("Etapa " + (atual.ordinal() + 1) + " de 7 · "
                + (atual == Etapa.REVISAO ? "Revise seu PC" : "Escolha " + artigo(atual) + " " + atual.titulo().toLowerCase()));
        subtitulo.setText(descricao(atual));
        principal.getChildren().clear();
        if (atual == Etapa.REVISAO) mostrarRevisao(); else mostrarOpcoes();
        atualizarResumo(); atualizarNavegacao();
    }

    private void mostrarOpcoes() {
        TextField busca = UiSupport.texto("Buscar nesta etapa por nome ou especificação");
        busca.setId("montagem-busca");
        selecionado.getStyleClass().setAll("montagem-selected"); selecionado.setWrapText(true);
        ListView<Componente> lista = new ListView<>(); lista.setId("montagem-opcoes");
        lista.getStyleClass().add("montagem-list"); lista.setMinHeight(100);
        lista.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(Componente item, boolean vazio) {
                super.updateItem(item, vazio); setText(null);
                if (vazio || item == null) { setGraphic(null); return; }
                Label nome = new Label(item.getNome()); nome.setWrapText(true);
                nome.getStyleClass().add("montagem-item-name");
                Label detalhes = new Label(detalhes(item)); detalhes.setWrapText(true);
                detalhes.getStyleClass().add("muted-text");
                VBox info = new VBox(5, nome, detalhes); info.setMinWidth(0); HBox.setHgrow(info, Priority.ALWAYS);
                Label preco = new Label(UiSupport.moeda(item.getPreco())); preco.getStyleClass().add("montagem-price");
                preco.setMinWidth(Region.USE_PREF_SIZE);
                HBox linha = new HBox(12, info, preco); linha.setAlignment(Pos.CENTER_LEFT);
                // Constrain card width so long names wrap instead of introducing horizontal scrolling.
                linha.prefWidthProperty().bind(lista.widthProperty().subtract(44));
                setGraphic(linha); setAccessibleText(item.getNome() + ", " + detalhes.getText() + ", " + preco.getText());
            }
        });
        Label vazio = new Label("Nenhuma peça disponível para esta escolha. Volte para trocar uma peça anterior ou confira o catálogo.");
        vazio.setWrapText(true); vazio.getStyleClass().add("muted-text"); lista.setPlaceholder(vazio);
        List<Componente> opcoes = estado.opcoes(catalogo).stream().map(c -> (Componente)c)
                .sorted(Comparator.comparingDouble(Componente::getPreco).thenComparing(Componente::getNome)).toList();
        Runnable filtrar = () -> {
            lista.setItems(FXCollections.observableArrayList(opcoes.stream()
                    .filter(c -> CatalogoFiltro.corresponde(c, busca.getText(), 0, Double.MAX_VALUE)).toList()));
            Componente atual = estado.selecionado(estado.etapa());
            if (atual != null) lista.getItems().stream().filter(c -> c.getId() == atual.getId()).findFirst()
                    .ifPresent(lista.getSelectionModel()::select);
        };
        filtrar.run();
        busca.textProperty().addListener((o, a, b) -> filtrar.run());
        lista.getSelectionModel().selectedItemProperty().addListener((o, antes, item) -> {
            // Filtering may hide a selected card; it must never erase the draft selection.
            if (item == null) return;
            try {
                List<String> removidas = estado.selecionar(item);
                aviso.setText(removidas.isEmpty() ? "" : "Para manter a compatibilidade, escolha novamente: " + String.join(", ", removidas) + ".");
                atualizarResumo(); atualizarNavegacao();
            } catch (RuntimeException erro) { aviso.setText(erro.getMessage()); }
        });
        principal.getChildren().addAll(busca, selecionado, lista);
        VBox.setVgrow(lista, Priority.ALWAYS);
    }

    private void mostrarRevisao() {
        Build parcial = estado.parcial();
        TextField nome = UiSupport.texto("Ex.: Meu PC para jogar"); nome.setId("montagem-nome");
        nome.setText(parcial.getNome());
        nome.textProperty().addListener((o, a, b) -> estado.definirNome(b));
        CheckBox favorita = new CheckBox("Marcar como favorita"); favorita.setSelected(parcial.isFavorita());
        favorita.selectedProperty().addListener((o, a, b) -> estado.definirFavorita(b));
        VBox linhas = new VBox(10, UiSupport.campo("NOME DA BUILD", nome), favorita);
        for (Etapa e : Etapa.values()) {
            if (e == Etapa.REVISAO) continue;
            Componente c = estado.selecionado(e);
            Label nomePeca = new Label(e.titulo() + "\n" + (c == null ? "Não selecionado" : c.getNome())); nomePeca.setWrapText(true);
            nomePeca.getStyleClass().add("montagem-review-name");
            VBox info = new VBox(4, nomePeca, new Label(c == null ? "" : UiSupport.moeda(c.getPreco())));
            info.setMinWidth(0); HBox.setHgrow(info, Priority.ALWAYS);
            Button trocar = new Button("Trocar"); trocar.getStyleClass().add("secondary-button");
            trocar.setAccessibleText("Trocar " + e.titulo()); trocar.setOnAction(event -> irPara(e));
            HBox linha = new HBox(10, info, trocar); linha.setAlignment(Pos.CENTER_LEFT);
            linha.getStyleClass().add("montagem-review-row"); linhas.getChildren().add(linha);
        }
        Label nota = new Label("Confira as peças antes de salvar. A compatibilidade considera socket, geração da memória e potência da fonte.");
        nota.setWrapText(true); nota.getStyleClass().add("muted-text"); linhas.getChildren().add(nota);
        ScrollPane scroll = new ScrollPane(linhas); scroll.setFitToWidth(true); scroll.getStyleClass().add("transparent-scroll");
        principal.getChildren().add(scroll); VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private void atualizarResumo() {
        resumo.getChildren().clear();
        Label label = new Label("SEU PC ATÉ AQUI"); label.getStyleClass().add("field-label");
        Label total = new Label(UiSupport.moeda(estado.parcial().getPrecoTotal())); total.getStyleClass().add("montagem-total");
        total.setWrapText(true); total.setMaxWidth(Double.MAX_VALUE);
        Label parcial = new Label("Total das peças selecionadas"); parcial.getStyleClass().add("muted-text");
        parcial.setWrapText(true);
        resumo.getChildren().addAll(label, total, parcial, new Separator());
        for (Etapa e : Etapa.values()) {
            if (e == Etapa.REVISAO) continue;
            Componente c = estado.selecionado(e);
            Button item = new Button(e.titulo() + "\n" + (c == null ? "Ainda não escolhido" : c.getNome()));
            item.setWrapText(true); item.setMaxWidth(Double.MAX_VALUE); item.setAlignment(Pos.CENTER_LEFT);
            item.getStyleClass().add("montagem-summary-item"); item.setDisable(!estado.podeIrPara(e));
            item.setAccessibleText("Rever " + e.titulo() + (c == null ? "" : ": " + c.getNome()));
            item.setOnAction(event -> irPara(e)); resumo.getChildren().add(item);
        }
        Label energia = new Label("Fonte recomendada: " + estado.fonteRecomendada() + " W\nAtualizada conforme as peças escolhidas.");
        energia.setWrapText(true); energia.getStyleClass().add("muted-text"); resumo.getChildren().add(energia);
        Componente c = estado.selecionado(estado.etapa());
        selecionado.setText(c == null ? "Selecione uma peça abaixo para continuar." : "Selecionado: " + c.getNome());
    }

    private void atualizarNavegacao() {
        etapas.getChildren().clear();
        for (Etapa e : Etapa.values()) {
            Button botao = new Button((e.ordinal() + 1) + "  " + e.titulo());
            botao.getStyleClass().add("montagem-step");
            if (e == estado.etapa()) botao.getStyleClass().add("montagem-step-active");
            else if (estado.selecionado(e) != null) botao.getStyleClass().add("montagem-step-done");
            botao.setDisable(!estado.podeIrPara(e)); botao.setOnAction(event -> irPara(e));
            botao.setAccessibleText("Etapa " + (e.ordinal() + 1) + ": " + e.titulo()); etapas.getChildren().add(botao);
        }
        progresso.setProgress((estado.etapa().ordinal() + 1) / 7.0);
        voltar.setDisable(estado.etapa() == Etapa.PROCESSADOR);
        continuar.setVisible(estado.etapa() != Etapa.REVISAO); continuar.setManaged(continuar.isVisible());
        continuar.setDisable(!estado.podeAvancar()); continuar.setId("montagem-continuar");
        salvar.setVisible(estado.etapa() == Etapa.REVISAO); salvar.setManaged(salvar.isVisible());
        salvar.setText(estado.editando() ? "Salvar alterações" : "Salvar meu PC"); salvar.setId("montagem-salvar");
        aviso.setVisible(!aviso.getText().isBlank()); aviso.setManaged(aviso.isVisible());
    }

    private void irPara(Etapa e) {
        try { estado.irPara(e); aviso.setText(""); mostrarEtapa(); }
        catch (RuntimeException erro) { aviso.setText(erro.getMessage()); }
    }
    private String descricao(Etapa e) {
        return switch (e) {
            case PROCESSADOR -> "Comece pelo processador. Ele define quais placas-mãe você poderá usar.";
            case PLACA_MAE -> "Mostrando placas-mãe com o socket do processador escolhido.";
            case MEMORIA -> "Mostrando apenas a geração de memória aceita pela sua placa-mãe.";
            case PLACA_VIDEO -> "Escolha sua placa de vídeo. A potência necessária será recalculada.";
            case SSD -> "Escolha o armazenamento para seu sistema e seus jogos.";
            case FONTE -> "Mostrando fontes de pelo menos " + estado.fonteRecomendada() + " W, com margem de 20% no consumo estimado.";
            case REVISAO -> "Dê um nome ao seu PC e confira tudo. Use Trocar para rever qualquer escolha.";
        };
    }
    private static String artigo(Etapa e) { return e == Etapa.PROCESSADOR || e == Etapa.SSD ? "o" : "a"; }
    private static String detalhes(Componente c) {
        if (c instanceof Processador p) return p.getFabricante() + " · " + p.getSocket() + " · " + p.getNucleos() + " núcleos / " + p.getThreads() + " threads · " + p.getConsumo() + " W";
        if (c instanceof PlacaMae p) return p.getFabricante() + " · " + p.getSocket() + " · " + p.getTipoMemoria();
        if (c instanceof Memoria m) return m.getCapacidade() + " GB · " + m.getTipo() + " · " + m.getFrequencia() + " MHz";
        if (c instanceof PlacaVideo p) return p.getFabricante() + " · " + p.getMemoria() + " GB VRAM · " + p.getConsumo() + " W";
        if (c instanceof SSD s) return s.getCapacidade() + " GB · " + s.getTipo() + " · leitura " + s.getLeitura() + " MB/s";
        if (c instanceof Fonte f) return f.getPotencia() + " W · " + f.getCertificacao();
        return "";
    }
}
