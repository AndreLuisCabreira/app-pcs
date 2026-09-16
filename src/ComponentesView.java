import ui.UiSupport;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Componente;
import model.Fonte;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.SSD;
import model.Usuario;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentesView extends VBox {

    private final Usuario usuarioAtual;
    private final AutorizacaoService autorizacaoService = new AutorizacaoService();

    public ComponentesView(Usuario usuarioAtual) {
        this.usuarioAtual = usuarioAtual;
        autorizacaoService.exigirAdmin(usuarioAtual);

        TabPane tabs = new TabPane(
                processadores(),
                placasMae(),
                placasVideo(),
                memorias(),
                ssds(),
                fontes()
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("component-tabs");

        VBox content = new VBox(tabs);
        content.setPadding(new Insets(24, 0, 0, 0));
        VBox.setVgrow(tabs, Priority.ALWAYS);

        getChildren().add(UiSupport.pagina(
                "Catálogo de componentes",
                "Cadastre novas peças ou selecione um item da lista para corrigir seus dados.",
                content
        ));
        VBox.setVgrow(getChildren().getFirst(), Priority.ALWAYS);
    }

    private Tab processadores() {
        TextField nome = UiSupport.texto("Ex.: Ryzen 7 7700");
        TextField fabricante = UiSupport.texto("AMD ou Intel");
        TextField preco = UiSupport.texto("1999,90");
        TextField socket = UiSupport.texto("AM5");
        TextField nucleos = UiSupport.texto("8");
        TextField threads = UiSupport.texto("16");
        TextField consumo = UiSupport.texto("65");
        TextField desempenho = UiSupport.texto("100");
        ProcessadorDAO dao = new ProcessadorDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("FABRICANTE", fabricante),
                UiSupport.campo("PREÇO", preco), UiSupport.campo("SOCKET", socket),
                UiSupport.campo("NÚCLEOS", nucleos), UiSupport.campo("THREADS", threads),
                UiSupport.campo("CONSUMO (W)", consumo), UiSupport.campo("ÍNDICE DE DESEMPENHO", desempenho)
        );

        Supplier<Processador> leitor = () -> {
            Processador item = new Processador();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setFabricante(UiSupport.obrigatorio(fabricante, "Fabricante"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setSocket(UiSupport.obrigatorio(socket, "Socket").toUpperCase());
            item.setNucleos(UiSupport.inteiro(nucleos, "Núcleos"));
            item.setThreads(UiSupport.inteiro(threads, "Threads"));
            item.setConsumo(UiSupport.inteiro(consumo, "Consumo"));
            item.setDesempenho(UiSupport.decimal(desempenho, "Desempenho"));
            return item;
        };

        Consumer<Processador> preencher = item -> {
            nome.setText(item.getNome());
            fabricante.setText(item.getFabricante());
            preco.setText(numero(item.getPreco()));
            socket.setText(item.getSocket());
            nucleos.setText(String.valueOf(item.getNucleos()));
            threads.setText(String.valueOf(item.getThreads()));
            consumo.setText(String.valueOf(item.getConsumo()));
            desempenho.setText(numero(item.getDesempenho()));
        };

        return tab("Processadores", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, fabricante, preco, socket, nucleos, threads, consumo, desempenho), dao::listar);
    }

    private Tab placasMae() {
        TextField nome = UiSupport.texto("Ex.: ASUS TUF B650M");
        TextField fabricante = UiSupport.texto("Fabricante");
        TextField preco = UiSupport.texto("1099,90");
        TextField socket = UiSupport.texto("AM5");
        TextField memoria = UiSupport.texto("DDR5");
        TextField consumo = UiSupport.texto("50");
        PlacaMaeDAO dao = new PlacaMaeDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("FABRICANTE", fabricante),
                UiSupport.campo("PREÇO", preco), UiSupport.campo("SOCKET", socket),
                UiSupport.campo("TIPO DE MEMÓRIA", memoria), UiSupport.campo("CONSUMO (W)", consumo)
        );

        Supplier<PlacaMae> leitor = () -> {
            PlacaMae item = new PlacaMae();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setFabricante(UiSupport.obrigatorio(fabricante, "Fabricante"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setSocket(UiSupport.obrigatorio(socket, "Socket").toUpperCase());
            item.setTipoMemoria(UiSupport.obrigatorio(memoria, "Tipo de memória").toUpperCase());
            item.setConsumo(UiSupport.inteiro(consumo, "Consumo"));
            return item;
        };

        Consumer<PlacaMae> preencher = item -> {
            nome.setText(item.getNome());
            fabricante.setText(item.getFabricante());
            preco.setText(numero(item.getPreco()));
            socket.setText(item.getSocket());
            memoria.setText(item.getTipoMemoria());
            consumo.setText(String.valueOf(item.getConsumo()));
        };

        return tab("Placas-mãe", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, fabricante, preco, socket, memoria, consumo), dao::listar);
    }

    private Tab placasVideo() {
        TextField nome = UiSupport.texto("Ex.: GeForce RTX 4070");
        TextField fabricante = UiSupport.texto("NVIDIA, AMD ou Intel");
        TextField preco = UiSupport.texto("3999,90");
        TextField memoria = UiSupport.texto("12");
        TextField consumo = UiSupport.texto("200");
        TextField desempenho = UiSupport.texto("120");
        PlacaVideoDAO dao = new PlacaVideoDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("FABRICANTE", fabricante),
                UiSupport.campo("PREÇO", preco), UiSupport.campo("VRAM (GB)", memoria),
                UiSupport.campo("CONSUMO (W)", consumo), UiSupport.campo("ÍNDICE DE DESEMPENHO", desempenho)
        );

        Supplier<PlacaVideo> leitor = () -> {
            PlacaVideo item = new PlacaVideo();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setFabricante(UiSupport.obrigatorio(fabricante, "Fabricante"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setMemoria(UiSupport.inteiro(memoria, "VRAM"));
            item.setConsumo(UiSupport.inteiro(consumo, "Consumo"));
            item.setDesempenho(UiSupport.inteiro(desempenho, "Desempenho"));
            return item;
        };

        Consumer<PlacaVideo> preencher = item -> {
            nome.setText(item.getNome());
            fabricante.setText(item.getFabricante());
            preco.setText(numero(item.getPreco()));
            memoria.setText(String.valueOf(item.getMemoria()));
            consumo.setText(String.valueOf(item.getConsumo()));
            desempenho.setText(String.valueOf(item.getDesempenho()));
        };

        return tab("Placas de vídeo", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, fabricante, preco, memoria, consumo, desempenho), dao::listar);
    }

    private Tab memorias() {
        TextField nome = UiSupport.texto("Ex.: Kingston Fury 16 GB");
        TextField preco = UiSupport.texto("329,90");
        TextField capacidade = UiSupport.texto("16");
        TextField frequencia = UiSupport.texto("5600");
        TextField tipo = UiSupport.texto("DDR5");
        MemoriaDAO dao = new MemoriaDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("PREÇO", preco),
                UiSupport.campo("CAPACIDADE (GB)", capacidade), UiSupport.campo("FREQUÊNCIA (MHz)", frequencia),
                UiSupport.campo("TIPO", tipo)
        );

        Supplier<Memoria> leitor = () -> {
            Memoria item = new Memoria();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setCapacidade(UiSupport.inteiro(capacidade, "Capacidade"));
            item.setFrequencia(UiSupport.inteiro(frequencia, "Frequência"));
            item.setTipo(UiSupport.obrigatorio(tipo, "Tipo").toUpperCase());
            return item;
        };

        Consumer<Memoria> preencher = item -> {
            nome.setText(item.getNome());
            preco.setText(numero(item.getPreco()));
            capacidade.setText(String.valueOf(item.getCapacidade()));
            frequencia.setText(String.valueOf(item.getFrequencia()));
            tipo.setText(item.getTipo());
        };

        return tab("Memórias", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, preco, capacidade, frequencia, tipo), dao::listar);
    }

    private Tab ssds() {
        TextField nome = UiSupport.texto("Ex.: Kingston NV2 1 TB");
        TextField preco = UiSupport.texto("399,90");
        TextField capacidade = UiSupport.texto("1000");
        TextField leitura = UiSupport.texto("3500");
        TextField escrita = UiSupport.texto("2800");
        TextField tipo = UiSupport.texto("NVMe");
        SSDDAO dao = new SSDDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("PREÇO", preco),
                UiSupport.campo("CAPACIDADE (GB)", capacidade), UiSupport.campo("LEITURA (MB/s)", leitura),
                UiSupport.campo("ESCRITA (MB/s)", escrita), UiSupport.campo("TIPO", tipo)
        );

        Supplier<SSD> leitor = () -> {
            SSD item = new SSD();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setCapacidade(UiSupport.inteiro(capacidade, "Capacidade"));
            item.setLeitura(UiSupport.inteiro(leitura, "Leitura"));
            item.setEscrita(UiSupport.inteiro(escrita, "Escrita"));
            item.setTipo(UiSupport.obrigatorio(tipo, "Tipo"));
            return item;
        };

        Consumer<SSD> preencher = item -> {
            nome.setText(item.getNome());
            preco.setText(numero(item.getPreco()));
            capacidade.setText(String.valueOf(item.getCapacidade()));
            leitura.setText(String.valueOf(item.getLeitura()));
            escrita.setText(String.valueOf(item.getEscrita()));
            tipo.setText(item.getTipo());
        };

        return tab("SSDs", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, preco, capacidade, leitura, escrita, tipo), dao::listar);
    }

    private Tab fontes() {
        TextField nome = UiSupport.texto("Ex.: Corsair RM750e");
        TextField preco = UiSupport.texto("699,90");
        TextField potencia = UiSupport.texto("750");
        TextField certificacao = UiSupport.texto("80 Plus Gold");
        FonteDAO dao = new FonteDAO();

        VBox fields = campos(
                UiSupport.campo("NOME", nome), UiSupport.campo("PREÇO", preco),
                UiSupport.campo("POTÊNCIA (W)", potencia), UiSupport.campo("CERTIFICAÇÃO", certificacao)
        );

        Supplier<Fonte> leitor = () -> {
            Fonte item = new Fonte();
            item.setNome(UiSupport.obrigatorio(nome, "Nome"));
            item.setPreco(UiSupport.decimal(preco, "Preço"));
            item.setPotencia(UiSupport.inteiro(potencia, "Potência"));
            item.setCertificacao(UiSupport.obrigatorio(certificacao, "Certificação"));
            return item;
        };

        Consumer<Fonte> preencher = item -> {
            nome.setText(item.getNome());
            preco.setText(numero(item.getPreco()));
            potencia.setText(String.valueOf(item.getPotencia()));
            certificacao.setText(item.getCertificacao());
        };

        return tab("Fontes", fields, leitor, dao::inserir, dao::atualizar, preencher,
                () -> limpar(nome, preco, potencia, certificacao), dao::listar);
    }

    private <T extends Componente> Tab tab(
            String titulo,
            VBox fields,
            Supplier<T> leitorFormulario,
            Consumer<T> inserir,
            Consumer<T> atualizar,
            Consumer<T> preencherFormulario,
            Runnable limparFormulario,
            Supplier<List<T>> carregar
    ) {
        ListView<T> list = new ListView<>();
        list.getStyleClass().add("catalog-list");
        list.setPlaceholder(new Label("Nenhum item cadastrado nesta categoria."));
        list.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label name = new Label(item.getNome());
                name.getStyleClass().add("catalog-item-name");
                Label price = new Label(UiSupport.moeda(item.getPreco()));
                price.getStyleClass().add("catalog-item-price");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox row = new HBox(12, name, spacer, price);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });

        AtomicReference<T> itemEmEdicao = new AtomicReference<>();
        Label formTitle = new Label("Novo componente");
        formTitle.getStyleClass().add("card-title");

        Button save = new Button("Salvar componente");
        save.getStyleClass().add("primary-button");
        save.setMaxWidth(Double.MAX_VALUE);

        Button cancel = new Button("Cancelar edição");
        cancel.getStyleClass().add("secondary-button");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setVisible(false);
        cancel.setManaged(false);

        Runnable sairDaEdicao = () -> {
            itemEmEdicao.set(null);
            limparFormulario.run();
            list.getSelectionModel().clearSelection();
            formTitle.setText("Novo componente");
            save.setText("Salvar componente");
            cancel.setVisible(false);
            cancel.setManaged(false);
        };

        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(carregar.get()));

        save.setOnAction(event -> {
            T item;
            boolean editando;
            try {
                autorizacaoService.exigirAdmin(usuarioAtual);
                item = leitorFormulario.get();
                editando = itemEmEdicao.get() != null;
                if (editando) {
                    item.setId(itemEmEdicao.get().getId());
                }
            } catch (RuntimeException e) {
                UiSupport.erro(e);
                return;
            }

            UiSupport.executarComFeedback(save, editando ? "Atualizando..." : "Salvando...", () -> {
                if (editando) {
                    atualizar.accept(item);
                } else {
                    inserir.accept(item);
                }
                return carregar.get();
            }, itens -> {
                sairDaEdicao.run();
                list.setItems(FXCollections.observableArrayList(itens));
                UiSupport.sucesso(editando
                        ? "Componente atualizado com sucesso."
                        : "Componente adicionado ao catálogo.");
            });
        });
        cancel.setOnAction(event -> sairDaEdicao.run());

        VBox form = new VBox(18, formTitle, fields, save, cancel);
        form.setPadding(new Insets(24));
        form.setPrefWidth(380);
        form.getStyleClass().add("panel-card");

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setPrefWidth(400);
        formScroll.getStyleClass().add("transparent-scroll");

        Label hint = new Label("Selecione um item e clique em editar para corrigir seus dados.");
        hint.setWrapText(true);
        hint.getStyleClass().add("muted-text");

        Button edit = new Button("Editar selecionado");
        edit.getStyleClass().add("secondary-button");
        edit.disableProperty().bind(list.getSelectionModel().selectedItemProperty().isNull());
        edit.setOnAction(event -> {
            T selecionado = list.getSelectionModel().getSelectedItem();
            if (selecionado == null) {
                return;
            }
            itemEmEdicao.set(selecionado);
            preencherFormulario.accept(selecionado);
            formTitle.setText("Editar componente");
            save.setText("Salvar alterações");
            cancel.setVisible(true);
            cancel.setManaged(true);
        });

        VBox catalog = new VBox(10, heading("Itens cadastrados"), hint, list, edit);
        catalog.setPadding(new Insets(20));
        catalog.getStyleClass().add("panel-card");
        VBox.setVgrow(list, Priority.ALWAYS);

        HBox body = new HBox(18, formScroll, catalog);
        body.setPadding(new Insets(18));
        HBox.setHgrow(catalog, Priority.ALWAYS);
        refresh.run();

        return new Tab(titulo, body);
    }

    private VBox campos(Node... nodes) {
        return new VBox(14, nodes);
    }

    private Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        return label;
    }

    private String numero(double valor) {
        if (valor == Math.rint(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor).replace('.', ',');
    }

    private void limpar(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }
}
