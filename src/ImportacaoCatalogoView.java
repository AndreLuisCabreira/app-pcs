import ui.UiSupport;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Componente;
import model.Fonte;
import model.Memoria;
import model.PlacaVideo;
import model.Processador;
import model.ProdutoCatalogo;
import model.SSD;
import model.TipoComponenteCatalogo;
import model.Usuario;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImportacaoCatalogoView extends VBox {

    private final Usuario usuarioAtual;
    private final AutorizacaoService autorizacao = new AutorizacaoService();
    private final PcPartDatasetClient client = new PcPartDatasetClient();
    private final CatalogoImportacaoDAO importacaoDAO = new CatalogoImportacaoDAO();
    private final ComboBox<TipoComponenteCatalogo> tipo = new ComboBox<>();
    private final TextField pesquisa = UiSupport.texto("Filtre por nome, fabricante ou especificação");
    private final TableView<ProdutoCatalogo> resultados = new TableView<>();
    private final VBox formulario = new VBox(14);
    private final Map<String, TextField> campos = new LinkedHashMap<>();
    private final Label status = new Label();
    private final Button atualizar = new Button("Atualizar lista");
    private final Button importar = new Button("Importar componente");
    private List<ProdutoCatalogo> listaCompleta = List.of();

    public ImportacaoCatalogoView(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        autorizacao.exigirAdmin(usuarioAtual);

        tipo.setItems(FXCollections.observableArrayList(TipoComponenteCatalogo.values()));
        tipo.getSelectionModel().selectFirst();
        tipo.setMaxWidth(Double.MAX_VALUE);
        tipo.valueProperty().addListener((observable, anterior, atual) -> carregarLista(false));

        atualizar.getStyleClass().add("primary-button");
        atualizar.setOnAction(event -> carregarLista(true));
        pesquisa.textProperty().addListener((observable, anterior, atual) -> filtrar());

        HBox pesquisaLinha = new HBox(10, pesquisa, atualizar);
        HBox.setHgrow(pesquisa, Priority.ALWAYS);
        VBox filtros = new VBox(
                12,
                UiSupport.campo("TIPO DE COMPONENTE", tipo),
                UiSupport.campo("FILTRAR LISTA", pesquisaLinha),
                status
        );
        filtros.setPadding(new Insets(20));
        filtros.getStyleClass().add("panel-card");

        configurarTabela();
        VBox tabelaBox = new VBox(12, titulo("Componentes disponíveis"), resultados);
        tabelaBox.setPadding(new Insets(20));
        tabelaBox.getStyleClass().add("panel-card");
        VBox.setVgrow(resultados, Priority.ALWAYS);

        importar.setMaxWidth(Double.MAX_VALUE);
        importar.getStyleClass().add("primary-button");
        importar.setOnAction(event -> importarSelecionado());
        mostrarInstrucao();

        ScrollPane formScroll = new ScrollPane(formulario);
        formScroll.setFitToWidth(true);
        formScroll.setPrefWidth(390);
        formScroll.getStyleClass().add("transparent-scroll");

        HBox corpo = new HBox(18, tabelaBox, formScroll);
        HBox.setHgrow(tabelaBox, Priority.ALWAYS);
        VBox.setVgrow(corpo, Priority.ALWAYS);
        VBox conteudo = new VBox(18, filtros, corpo);
        conteudo.setPadding(new Insets(24, 0, 0, 0));
        VBox.setVgrow(corpo, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Importar catálogo",
                "Lista direta do PC Part Dataset, sem chave ou configuração de API.",
                conteudo
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);

        status.setWrapText(true);
        status.getStyleClass().add("status-ready");
        carregarLista(false);
    }

    private void configurarTabela() {
        resultados.getColumns().addAll(List.of(
                UiSupport.coluna("ID", produto -> produto.getIdExterno().substring(0, 8), 75),
                UiSupport.coluna("Componente", ProdutoCatalogo::getNome, 360),
                UiSupport.coluna("Fabricante", ProdutoCatalogo::getFabricante, 130),
                UiSupport.coluna("Preço do dataset", this::precoDataset, 125)
        ));
        resultados.setPlaceholder(new Label("Carregando a lista pública..."));
        resultados.getStyleClass().add("data-table");
        resultados.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, atual) -> preencherFormulario(atual)
        );
    }

    private void carregarLista(boolean forcarAtualizacao) {
        TipoComponenteCatalogo tipoSelecionado = tipo.getValue();
        if (tipoSelecionado == null) {
            return;
        }
        atualizar.setDisable(true);
        tipo.setDisable(true);
        pesquisa.setDisable(true);
        listaCompleta = List.of();
        resultados.getItems().clear();
        resultados.setPlaceholder(new Label("Carregando a lista pública..."));
        mostrarInstrucao();
        definirStatus("Baixando " + tipoSelecionado + " do PC Part Dataset...", false);

        Task<List<ProdutoCatalogo>> tarefa = new Task<>() {
            @Override
            protected List<ProdutoCatalogo> call() {
                return client.listar(tipoSelecionado, forcarAtualizacao);
            }
        };
        tarefa.setOnSucceeded(event -> {
            atualizar.setDisable(false);
            tipo.setDisable(false);
            pesquisa.setDisable(false);
            listaCompleta = tarefa.getValue();
            filtrar();
        });
        tarefa.setOnFailed(event -> {
            atualizar.setDisable(false);
            tipo.setDisable(false);
            pesquisa.setDisable(false);
            resultados.setPlaceholder(new Label("Não foi possível carregar esta categoria."));
            definirStatus("Falha ao carregar a lista. Verifique sua internet e tente novamente.", true);
            UiSupport.erro(tarefa.getException());
        });

        Thread thread = new Thread(tarefa, "pc-part-dataset");
        thread.setDaemon(true);
        thread.start();
    }

    private void filtrar() {
        String termo = normalizar(pesquisa.getText());
        List<ProdutoCatalogo> filtrados = termo.isBlank()
                ? listaCompleta
                : listaCompleta.stream()
                .filter(produto -> corresponde(produto, termo))
                .toList();
        resultados.setItems(FXCollections.observableArrayList(filtrados));
        resultados.setPlaceholder(new Label("Nenhum componente corresponde ao filtro."));
        definirStatus(
                filtrados.size() + " de " + listaCompleta.size()
                        + " componente(s). Fonte: docyx/pc-part-dataset (dados de julho de 2025).",
                false
        );
        if (!filtrados.isEmpty()) {
            resultados.getSelectionModel().selectFirst();
        } else {
            mostrarInstrucao();
        }
    }

    private boolean corresponde(ProdutoCatalogo produto, String termo) {
        if (normalizar(produto.getNome()).contains(termo)
                || normalizar(produto.getFabricante()).contains(termo)) {
            return true;
        }
        return produto.getDetalhes().values().stream()
                .map(this::normalizar)
                .anyMatch(valor -> valor.contains(termo));
    }

    private void preencherFormulario(ProdutoCatalogo produto) {
        if (produto == null) {
            mostrarInstrucao();
            return;
        }
        campos.clear();
        formulario.getChildren().clear();
        prepararFormulario();

        Label referencia = new Label(
                "Preço no dataset: " + precoDataset(produto)
                        + "\nO valor é uma referência em dólar; informe abaixo o preço local em reais."
        );
        referencia.setWrapText(true);
        referencia.getStyleClass().add("analysis-note");
        formulario.getChildren().addAll(titulo("Revisar antes de importar"), referencia);

        adicionarCampo("nome", "NOME", produto.getNome());
        adicionarCampo("fabricante", "FABRICANTE", produto.getFabricante());
        adicionarCampo("preco", "PREÇO LOCAL (R$)", "");

        switch (tipo.getValue()) {
            case PROCESSADOR -> camposProcessador(produto);
            case PLACA_VIDEO -> camposPlacaVideo(produto);
            case FONTE -> camposFonte(produto);
            case MEMORIA -> camposMemoria(produto);
            case SSD -> camposSsd(produto);
        }

        Label aviso = new Label(
                "Campos vazios não existem no dataset e precisam ser preenchidos por você antes da importação."
        );
        aviso.setWrapText(true);
        aviso.getStyleClass().add("muted-text");
        formulario.getChildren().addAll(importar, aviso);
    }

    private void camposProcessador(ProdutoCatalogo produto) {
        adicionarCampo("socket", "SOCKET — NÃO DISPONÍVEL NO DATASET", "");
        adicionarCampo("nucleos", "NÚCLEOS", produto.buscarDetalhe("Núcleos"));
        adicionarCampo("threads", "THREADS — NÃO DISPONÍVEL NO DATASET", "");
        adicionarCampo("consumo", "CONSUMO / TDP (W)", produto.buscarDetalhe("TDP"));
        adicionarCampo("desempenho", "ÍNDICE DE DESEMPENHO", "100");
    }

    private void camposPlacaVideo(ProdutoCatalogo produto) {
        adicionarCampo("memoria", "VRAM (GB)", produto.buscarDetalhe("VRAM"));
        adicionarCampo("consumo", "CONSUMO (W) — NÃO DISPONÍVEL NO DATASET", "");
        adicionarCampo("desempenho", "ÍNDICE DE DESEMPENHO", "100");
    }

    private void camposFonte(ProdutoCatalogo produto) {
        adicionarCampo("potencia", "POTÊNCIA (W)", produto.buscarDetalhe("Potência"));
        String eficiencia = produto.buscarDetalhe("Eficiência");
        adicionarCampo(
                "certificacao",
                "CERTIFICAÇÃO",
                eficiencia.isBlank() ? "Não informada" : "80 Plus " + capitalizar(eficiencia)
        );
    }

    private void camposMemoria(ProdutoCatalogo produto) {
        adicionarCampo("capacidade", "CAPACIDADE (GB)", produto.buscarDetalhe("Capacidade"));
        adicionarCampo("frequencia", "FREQUÊNCIA (MHz)", produto.buscarDetalhe("Frequência"));
        adicionarCampo("tipo", "TIPO", produto.buscarDetalhe("Tipo"));
    }

    private void camposSsd(ProdutoCatalogo produto) {
        adicionarCampo("capacidade", "CAPACIDADE (GB)", produto.buscarDetalhe("Capacidade"));
        adicionarCampo("leitura", "LEITURA (MB/S) — NÃO DISPONÍVEL NO DATASET", "");
        adicionarCampo("escrita", "ESCRITA (MB/S) — NÃO DISPONÍVEL NO DATASET", "");
        String interfaceSsd = produto.buscarDetalhe("Interface");
        adicionarCampo("tipo", "TIPO / INTERFACE", interfaceSsd.isBlank() ? "SSD" : interfaceSsd);
    }

    private void adicionarCampo(String chave, String rotulo, String valor) {
        TextField campo = UiSupport.texto("");
        campo.setText(valor == null ? "" : valor.trim());
        campos.put(chave, campo);
        formulario.getChildren().add(UiSupport.campo(rotulo, campo));
    }

    private void importarSelecionado() {
        ProdutoCatalogo produto = resultados.getSelectionModel().getSelectedItem();
        if (produto == null) {
            UiSupport.erro(new IllegalArgumentException("Selecione um componente da lista."));
            return;
        }
        TipoComponenteCatalogo tipoSelecionado = tipo.getValue();
        Componente componente;
        try {
            autorizacao.exigirAdmin(usuarioAtual);
            componente = criarComponente(tipoSelecionado);
        } catch (RuntimeException e) {
            UiSupport.erro(e);
            return;
        }

        UiSupport.executarComFeedback(
                importar,
                "Importando...",
                () -> importacaoDAO.importar(tipoSelecionado, produto, componente),
                id -> UiSupport.sucesso("Componente importado com ID " + id + ".")
        );
    }

    private Componente criarComponente(TipoComponenteCatalogo tipoSelecionado) {
        String nome = obrigatorio("nome", "Nome");
        String fabricante = obrigatorio("fabricante", "Fabricante");
        double preco = decimalPositivo("preco", "Preço local");

        return switch (tipoSelecionado) {
            case PROCESSADOR -> {
                Processador item = new Processador();
                item.setNome(nome);
                item.setFabricante(fabricante);
                item.setPreco(preco);
                item.setSocket(obrigatorio("socket", "Socket").toUpperCase());
                item.setNucleos(inteiroPositivo("nucleos", "Núcleos"));
                item.setThreads(inteiroPositivo("threads", "Threads"));
                item.setConsumo(inteiroPositivo("consumo", "Consumo"));
                item.setDesempenho(decimalPositivo("desempenho", "Desempenho"));
                yield item;
            }
            case PLACA_VIDEO -> {
                PlacaVideo item = new PlacaVideo();
                item.setNome(nome);
                item.setFabricante(fabricante);
                item.setPreco(preco);
                item.setMemoria(inteiroPositivo("memoria", "VRAM"));
                item.setConsumo(inteiroPositivo("consumo", "Consumo"));
                item.setDesempenho(inteiroPositivo("desempenho", "Desempenho"));
                yield item;
            }
            case FONTE -> {
                Fonte item = new Fonte();
                item.setNome(nome);
                item.setPreco(preco);
                item.setPotencia(inteiroPositivo("potencia", "Potência"));
                item.setCertificacao(obrigatorio("certificacao", "Certificação"));
                yield item;
            }
            case MEMORIA -> {
                Memoria item = new Memoria();
                item.setNome(nome);
                item.setPreco(preco);
                item.setCapacidade(inteiroPositivo("capacidade", "Capacidade"));
                item.setFrequencia(inteiroPositivo("frequencia", "Frequência"));
                item.setTipo(obrigatorio("tipo", "Tipo").toUpperCase());
                yield item;
            }
            case SSD -> {
                SSD item = new SSD();
                item.setNome(nome);
                item.setPreco(preco);
                item.setCapacidade(inteiroPositivo("capacidade", "Capacidade"));
                item.setLeitura(inteiroPositivo("leitura", "Leitura"));
                item.setEscrita(inteiroPositivo("escrita", "Escrita"));
                item.setTipo(obrigatorio("tipo", "Tipo"));
                yield item;
            }
        };
    }

    private String obrigatorio(String chave, String nome) {
        return UiSupport.obrigatorio(campos.get(chave), nome);
    }

    private int inteiroPositivo(String chave, String nome) {
        int valor = UiSupport.inteiro(campos.get(chave), nome);
        if (valor <= 0) {
            throw new IllegalArgumentException(nome + " deve ser maior que zero.");
        }
        return valor;
    }

    private double decimalPositivo(String chave, String nome) {
        double valor = UiSupport.decimal(campos.get(chave), nome);
        if (valor <= 0) {
            throw new IllegalArgumentException(nome + " deve ser maior que zero.");
        }
        return valor;
    }

    private String precoDataset(ProdutoCatalogo produto) {
        return produto.getPrecoUsd() == null
                ? "Não informado"
                : String.format(Locale.US, "US$ %.2f", produto.getPrecoUsd());
    }

    private void mostrarInstrucao() {
        campos.clear();
        Label texto = new Label("Selecione um componente para revisar os campos antes da importação.");
        texto.setWrapText(true);
        texto.getStyleClass().add("empty-state");
        formulario.getChildren().setAll(titulo("Revisão do componente"), texto);
        prepararFormulario();
    }

    private void prepararFormulario() {
        formulario.setPadding(new Insets(20));
        if (!formulario.getStyleClass().contains("panel-card")) {
            formulario.getStyleClass().add("panel-card");
        }
    }

    private void definirStatus(String texto, boolean aviso) {
        status.getStyleClass().removeAll("status-ready", "status-warning");
        status.getStyleClass().add(aviso ? "status-warning" : "status-ready");
        status.setText(texto);
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return valor.substring(0, 1).toUpperCase(Locale.ROOT) + valor.substring(1).toLowerCase(Locale.ROOT);
    }

    private Label titulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("card-title");
        return label;
    }
}
