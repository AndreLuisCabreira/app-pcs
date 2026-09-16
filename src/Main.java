import model.*;
import service.CompatibilidadeService;
import service.ConsumoService;
import service.FPSService;
import service.PasswordService;

import java.util.Scanner;
import java.util.function.Predicate;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static UsuarioDAO usuarioDAO = new UsuarioDAO();
    static ProcessadorDAO processadorDAO = new ProcessadorDAO();
    static PlacaMaeDAO placaMaeDAO = new PlacaMaeDAO();
    static PlacaVideoDAO placaVideoDAO = new PlacaVideoDAO();
    static MemoriaDAO memoriaDAO = new MemoriaDAO();
    static SSDDAO ssdDAO = new SSDDAO();
    static FonteDAO fonteDAO = new FonteDAO();
    static BuildDAO buildDAO = new BuildDAO();
    static JogoDAO jogoDAO = new JogoDAO();

    static CompatibilidadeService compatibilidadeService =
            new CompatibilidadeService();

    static ConsumoService consumoService =
            new ConsumoService();

    static FPSService fpsService =
            new FPSService();

    static PasswordService passwordService =
            new PasswordService();

    public static int lerInteiro(String mensagem) {

        while (true) {

            System.out.print(mensagem);
            String entrada = sc.nextLine().trim();

            if (entrada.isEmpty()) {
                System.out.println("Erro! O campo não pode ficar vazio.");
                continue;
            }

            if (entrada.matches("\\d+")) {

                int numero = Integer.parseInt(entrada);

                if (numero < 0) {
                    System.out.println("Erro! O número não pode ser negativo.");
                    continue;
                }

                return numero;

            } else {

                System.out.println("Erro! Digite apenas números inteiros.");

            }

        }

    }

    public static double lerDouble(String mensagem) {

        while (true) {

            System.out.print(mensagem);
            String entrada = sc.nextLine().trim();

            if (entrada.isEmpty()) {
                System.out.println("Erro! O campo não pode ficar vazio.");
                continue;
            }

            try {
                double numero = Double.parseDouble(entrada);

                if (numero < 0) {
                    System.out.println("Erro! O número não pode ser negativo.");
                    continue;
                }

                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Erro! Digite apenas números válidos.");
            }

        }

    }

    public static int lerIdExistente(String mensagem, Predicate<Integer> validador) {
        while (true) {
            int id = lerInteiro(mensagem);

            if (validador.test(id)) {
                return id;
            }

            System.out.println("ID inválido! Digite um ID que exista.");
        }
    }

    public static void main(String[] args) {

        new DatabaseSchemaValidator().verificar();

        int opcao;

        do {

            System.out.println("\n========== PC BUILDER ==========");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Cadastrar Componentes");
            System.out.println("3 - Criar Build");
            System.out.println("4 - Listar Builds");
            System.out.println("5 - Buscar Build");
            System.out.println("6 - Atualizar Build");
            System.out.println("7 - Excluir Build");
            System.out.println("8 - Compatibilidade");
            System.out.println("9 - Consumo");
            System.out.println("10 - FPS");
            System.out.println("0 - Sair");

            opcao = lerInteiro("Escolha: ");

            if (opcao < 0 || opcao > 10) {
                System.out.println("Opção inválida! Digite um número entre 0 e 10.");
                continue;
            }

            try {
                switch (opcao) {

                    case 1:
                        cadastrarUsuario();
                        break;

                    case 2:
                        cadastrarComponente();
                        break;

                    case 3:
                        criarBuild();
                        break;

                    case 4:
                        listarBuilds();
                        break;

                    case 5:
                        buscarBuild();
                        break;

                    case 6:
                        atualizarBuild();
                        break;

                    case 7:
                        excluirBuild();
                        break;

                    case 8:
                        testarCompatibilidade();
                        break;

                    case 9:
                        calcularConsumo();
                        break;

                    case 10:
                        calcularFPS();
                        break;
                }
            } catch (DataAccessException | IllegalStateException e) {
                System.out.println("Falha na operação: " + e.getMessage());

            }

        } while (opcao != 0);

    }

    public static void cadastrarUsuario() {

        Usuario usuario = new Usuario();

        System.out.println("\n===== CADASTRO DE USUÁRIO =====");

        String nome;
        while (true) {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();

            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
                continue;
            }

            if (!nome.matches("[a-zA-ZÀ-ÿ\s]+")) {
                System.out.println("Erro! O nome deve conter apenas letras.");
                continue;
            }

            break;
        }
        usuario.setNome(nome);

        String login;
        do {
            System.out.print("Login: ");
            login = sc.nextLine().trim();
            if (login.isEmpty()) {
                System.out.println("Erro! O login não pode ficar vazio.");
            }
        } while (login.isEmpty());
        usuario.setLogin(login);

        String senha;
        do {
            System.out.print("Senha: ");
            senha = sc.nextLine().trim();
            if (senha.isEmpty()) {
                System.out.println("Erro! A senha não pode ficar vazia.");
            }
        } while (senha.isEmpty());
        usuario.setSenha(passwordService.criarHash(senha));

        usuarioDAO.inserir(usuario);

        System.out.println("\nUsuário cadastrado com sucesso!");
    }

    public static void cadastrarComponente(){

        int op;

        do{

            System.out.println("\n===== COMPONENTES =====");

            System.out.println("1 - Processador");
            System.out.println("2 - Placa Mãe");
            System.out.println("3 - Placa de Vídeo");
            System.out.println("4 - Memória");
            System.out.println("5 - SSD");
            System.out.println("6 - Fonte");
            System.out.println("0 - Voltar");

            op = lerInteiro("Escolha uma opção: ");

            if (op < 0 || op > 6) {
                System.out.println("Opção inválida! Digite apenas números entre 0 e 6.");
                continue;
            }

            switch(op){

                case 1:

                    cadastrarProcessador();

                    break;

                case 2:

                    cadastrarPlacaMae();

                    break;

                case 3:

                    cadastrarPlacaVideo();

                    break;

                case 4:

                    cadastrarMemoria();

                    break;

                case 5:

                    cadastrarSSD();

                    break;

                case 6:

                    cadastrarFonte();

                    break;

            }

        }while(op != 0);

    }
    public static void cadastrarProcessador(){

        Processador p = new Processador();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        p.setNome(nome);

        p.setPreco(lerDouble("Preço: "));

        String fabricante;
        do {
            System.out.print("Fabricante: ");
            fabricante = sc.nextLine().trim();
            if (fabricante.isEmpty()) {
                System.out.println("Erro! O fabricante não pode ficar vazio.");
            }
        } while (fabricante.isEmpty());
        p.setFabricante(fabricante);

        String socket;
        do {
            System.out.print("Socket: ");
            socket = sc.nextLine().trim();
            if (socket.isEmpty()) {
                System.out.println("Erro! O socket não pode ficar vazio.");
            }
        } while (socket.isEmpty());
        p.setSocket(socket);

        p.setNucleos(lerInteiro("Núcleos: "));

        p.setThreads(lerInteiro("Threads: "));

        p.setConsumo(lerInteiro("Consumo: "));

        p.setDesempenho(lerDouble("Desempenho: "));

        processadorDAO.inserir(p);

        System.out.println("Processador cadastrado com sucesso!");
    }

    public static void cadastrarPlacaMae() {

        PlacaMae placaMae = new PlacaMae();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        placaMae.setNome(nome);

        placaMae.setPreco(lerDouble("Preço: "));

        String fabricante;
        do {
            System.out.print("Fabricante: ");
            fabricante = sc.nextLine().trim();
            if (fabricante.isEmpty()) {
                System.out.println("Erro! O fabricante não pode ficar vazio.");
            }
        } while (fabricante.isEmpty());
        placaMae.setFabricante(fabricante);

        String socket;
        do {
            System.out.print("Socket: ");
            socket = sc.nextLine().trim();
            if (socket.isEmpty()) {
                System.out.println("Erro! O socket não pode ficar vazio.");
            }
        } while (socket.isEmpty());
        placaMae.setSocket(socket);

        String tipoMemoria;
        do {
            System.out.print("Tipo de Memória (DDR4 ou DDR5): ");
            tipoMemoria = sc.nextLine().trim();
            if (tipoMemoria.isEmpty()) {
                System.out.println("Erro! O tipo de memória não pode ficar vazio.");
            }
        } while (tipoMemoria.isEmpty());
        placaMae.setTipoMemoria(tipoMemoria);

        placaMae.setConsumo(lerInteiro("Consumo (W): "));

        placaMaeDAO.inserir(placaMae);

        System.out.println("\nPlaca-mãe cadastrada com sucesso!");
    }

    public static void cadastrarPlacaVideo() {

        PlacaVideo placaVideo = new PlacaVideo();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        placaVideo.setNome(nome);

        placaVideo.setPreco(lerDouble("Preço: "));

        String fabricante;
        do {
            System.out.print("Fabricante: ");
            fabricante = sc.nextLine().trim();
            if (fabricante.isEmpty()) {
                System.out.println("Erro! O fabricante não pode ficar vazio.");
            }
        } while (fabricante.isEmpty());
        placaVideo.setFabricante(fabricante);

        placaVideo.setMemoria(lerInteiro("Memória de Vídeo (GB): "));

        placaVideo.setConsumo(lerInteiro("Consumo (W): "));

        placaVideo.setDesempenho(lerInteiro("Desempenho: "));

        placaVideoDAO.inserir(placaVideo);

        System.out.println("\nPlaca de vídeo cadastrada com sucesso!");
    }

    public static void cadastrarMemoria() {

        Memoria memoria = new Memoria();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        memoria.setNome(nome);

        memoria.setPreco(lerDouble("Preço: "));

        memoria.setCapacidade(lerInteiro("Capacidade (GB): "));

        memoria.setFrequencia(lerInteiro("Frequência (MHz): "));

        String tipo;
        do {
            System.out.print("Tipo (DDR4 ou DDR5): ");
            tipo = sc.nextLine().trim();
            if (tipo.isEmpty()) {
                System.out.println("Erro! O tipo não pode ficar vazio.");
            }
        } while (tipo.isEmpty());
        memoria.setTipo(tipo);

        memoriaDAO.inserir(memoria);

        System.out.println("\nMemória cadastrada com sucesso!");
    }

    public static void cadastrarSSD() {

        SSD ssd = new SSD();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        ssd.setNome(nome);

        ssd.setPreco(lerDouble("Preço: "));

        ssd.setCapacidade(lerInteiro("Capacidade (GB): "));

        ssd.setLeitura(lerInteiro("Velocidade de Leitura (MB/s): "));

        ssd.setEscrita(lerInteiro("Velocidade de Escrita (MB/s): "));

        String tipo;
        do {
            System.out.print("Tipo (SATA ou NVMe): ");
            tipo = sc.nextLine().trim();
            if (tipo.isEmpty()) {
                System.out.println("Erro! O tipo não pode ficar vazio.");
            }
        } while (tipo.isEmpty());
        ssd.setTipo(tipo);

        ssdDAO.inserir(ssd);

        System.out.println("\nSSD cadastrada com sucesso!");
    }

    public static void cadastrarFonte() {

        Fonte fonte = new Fonte();

        String nome;
        do {
            System.out.print("Nome: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        fonte.setNome(nome);

        fonte.setPreco(lerDouble("Preço: "));

        fonte.setPotencia(lerInteiro("Potência (W): "));

        String certificacao;
        do {
            System.out.print("Certificação (80 Plus White, Bronze, Gold...): ");
            certificacao = sc.nextLine().trim();
            if (certificacao.isEmpty()) {
                System.out.println("Erro! A certificação não pode ficar vazia.");
            }
        } while (certificacao.isEmpty());
        fonte.setCertificacao(certificacao);

        fonteDAO.inserir(fonte);

        System.out.println("\nFonte cadastrada com sucesso!");
    }

    public static void criarBuild() {

        Build build = new Build();

        System.out.println("\n========== CRIAR BUILD ==========");

        System.out.print("Nome da Build: ");
        build.setNome(sc.nextLine());

        Usuario usuario;

        while (true) {

            int idUsuario = lerIdExistente("Digite o ID do Usuário: ", id -> usuarioDAO.buscarPorId(id) != null);

            usuario = usuarioDAO.buscarPorId(idUsuario);

            if (usuario != null) {

                build.setUsuarioId(idUsuario);
                break;

            }

            System.out.println("Usuário não encontrado! Digite um ID válido.");
        }

        // ==========================
        // PLACA MÃE
        // ==========================

        System.out.println("\nPLACAS-MÃE DISPONÍVEIS");

        for (PlacaMae pm : placaMaeDAO.listar()) {
            System.out.println(pm);
        }

        PlacaMae placaMae;

        while (true) {

            int idPlacaMae = lerIdExistente("Escolha o ID da Placa-Mãe: ", placaMaeId -> placaMaeDAO.buscarPorId(placaMaeId) != null);

            placaMae = placaMaeDAO.buscarPorId(idPlacaMae);

            if (placaMae == null) {
                System.out.println("Placa-Mãe não encontrada! Digite um ID válido.");
                continue;
            }

            build.setPlacaMae(placaMae);
            break;
        }

        // ==========================
        // PROCESSADOR
        // ==========================

        System.out.println("\nPROCESSADORES COMPATÍVEIS COM O SOCKET " + placaMae.getSocket());

        boolean processadorCompativelEncontrado = false;

        for (Processador p : processadorDAO.listar()) {
            if (p.getSocket().equalsIgnoreCase(placaMae.getSocket())) {
                System.out.println(p);
                processadorCompativelEncontrado = true;
            }
        }

        if (!processadorCompativelEncontrado) {
            System.out.println("Nenhum processador compatível com este socket foi encontrado.");
        }

        while (true) {

            int id = lerIdExistente("Escolha o ID do Processador: ", processadorId -> processadorDAO.buscarPorId(processadorId) != null);

            Processador processador = processadorDAO.buscarPorId(id);

            if (processador != null) {

                if (!processador.getSocket().equalsIgnoreCase(placaMae.getSocket())) {
                    System.out.println("Este processador não é compatível com o socket da placa-mãe escolhida!");
                    continue;
                }

                build.setProcessador(processador);
                break;

            }

            System.out.println("Processador não encontrado! Digite um ID válido.");
        }

        // ==========================
        // PLACA DE VÍDEO
        // ==========================

        System.out.println("\nPLACAS DE VÍDEO DISPONÍVEIS");

        for (PlacaVideo pv : placaVideoDAO.listar()) {
            System.out.println(pv);
        }

        PlacaVideo placaVideo;

        while (true) {

            int idPlacaVideo = lerIdExistente("Escolha o ID da Placa de Vídeo: ", placaVideoId -> placaVideoDAO.buscarPorId(placaVideoId) != null);

            placaVideo = placaVideoDAO.buscarPorId(idPlacaVideo);

            if (placaVideo != null) {

                build.setPlacaVideo(placaVideo);
                break;

            }

            System.out.println("Placa de Vídeo não encontrada! Digite um ID válido.");
        }

        // ==========================
        // MEMÓRIA
        // ==========================

        String tipoMemoriaRequerido = build.getPlacaMae().getTipoMemoria();

        System.out.println("\nMEMÓRIAS COMPATÍVEIS (" + tipoMemoriaRequerido + ")");

        boolean memoriaCompativelEncontrada = false;

        for (Memoria m : memoriaDAO.listar()) {
            if (m.getTipo().equalsIgnoreCase(tipoMemoriaRequerido)) {
                System.out.println(m);
                memoriaCompativelEncontrada = true;
            }
        }

        if (!memoriaCompativelEncontrada) {
            System.out.println("Nenhuma memória compatível com o tipo " + tipoMemoriaRequerido + " foi encontrada.");
        }

        Memoria memoria;

        while (true) {

            int idMemoria = lerInteiro("Escolha o ID da Memória: ");

            memoria = memoriaDAO.buscarPorId(idMemoria);

            if (memoria == null) {
                System.out.println("Memória não encontrada! Digite um ID válido.");
                continue;
            }

            if (!memoria.getTipo().equalsIgnoreCase(tipoMemoriaRequerido)) {
                System.out.println("Esta memória não é compatível com o tipo da placa-mãe escolhida!");
                continue;
            }

            build.setMemoria(memoria);
            break;
        }

        // ==========================
        // SSD
        // ==========================

        System.out.println("\nSSDs DISPONÍVEIS");

        for (SSD s : ssdDAO.listar()) {
            System.out.println(s);
        }

        SSD ssd;

        while (true) {

            int idSSD = lerIdExistente("Escolha o ID do SSD: ", ssdId -> ssdDAO.buscarPorId(ssdId) != null);

            ssd = ssdDAO.buscarPorId(idSSD);

            if (ssd != null) {

                build.setSsd(ssd);
                break;

            }

            System.out.println("SSD não encontrado! Digite um ID válido.");
        }

        // ==========================
        // FONTE
        // ==========================

        System.out.println("\nFONTES DISPONÍVEIS");

        for (Fonte f : fonteDAO.listar()) {
            System.out.println(f);
        }

        Fonte fonte;

        while (true) {

            int idFonte = lerIdExistente("Escolha o ID da Fonte: ", fonteId -> fonteDAO.buscarPorId(fonteId) != null);

            fonte = fonteDAO.buscarPorId(idFonte);

            if (fonte != null) {
                build.setFonte(fonte);
                if (!consumoService.fonteSuporta(build)) {
                    System.out.println("Esta fonte não possui a potência recomendada para a build.");
                    build.setFonte(null);
                    continue;
                }
                break;
            }

            System.out.println("Fonte não encontrada! Digite um ID válido.");
        }

        // ==========================
        // FAVORITA
        // ==========================

        while (true) {
            System.out.print("\nA build é favorita? (true/false): ");
            String favorita = sc.nextLine().trim();

            if (favorita.equalsIgnoreCase("true")) {
                build.setFavorita(true);
                break;
            }

            if (favorita.equalsIgnoreCase("false")) {
                build.setFavorita(false);
                break;
            }

            System.out.println("Erro! Digite apenas true ou false.");
        }

        // ==========================
        // SALVAR
        // ==========================

        buildDAO.inserir(build);

        System.out.println("\nBuild criada com sucesso!");

    }

    public static void listarBuilds() {

        System.out.println("\n========== BUILDS CADASTRADAS ==========\n");

        if (buildDAO.listar().isEmpty()) {
            System.out.println("Nenhuma build cadastrada.");
            return;
        }

        for (Build build : buildDAO.listar()) {

            System.out.println("ID: " + build.getId());
            System.out.println("Nome: " + build.getNome());
            System.out.println("Usuário ID: " + build.getUsuarioId());

            System.out.println("Processador: " +
                    build.getProcessador().getNome());

            System.out.println("Placa-Mãe: " +
                    build.getPlacaMae().getNome());

            System.out.println("Placa de Vídeo: " +
                    build.getPlacaVideo().getNome());

            System.out.println("Memória: " +
                    build.getMemoria().getNome());

            System.out.println("SSD: " +
                    build.getSsd().getNome());

            System.out.println("Fonte: " +
                    build.getFonte().getNome());

            System.out.printf("Preço total: R$ %.2f%n", build.getPrecoTotal());

            System.out.println("Favorita: " +
                    (build.isFavorita() ? "Sim" : "Não"));

            System.out.println("----------------------------------------");
        }
    }

    public static void buscarBuild() {

        System.out.println("\n========== BUSCAR BUILD ==========");

        while (true) {
            int id = lerInteiro("Digite o ID da Build: ");

            Build build = buildDAO.buscarPorId(id);

            if (build == null) {
                System.out.println("\nBuild não encontrada!");
                continue;
            }

            System.out.println("\n========== BUILD ENCONTRADA ==========");
            System.out.println("ID: " + build.getId());
            System.out.println("Nome: " + build.getNome());
            System.out.println("Usuário ID: " + build.getUsuarioId());

            System.out.println("Processador: " +
                    build.getProcessador().getNome());

            System.out.println("Placa-Mãe: " +
                    build.getPlacaMae().getNome());

            System.out.println("Placa de Vídeo: " +
                    build.getPlacaVideo().getNome());

            System.out.println("Memória: " +
                    build.getMemoria().getNome());

            System.out.println("SSD: " +
                    build.getSsd().getNome());

            System.out.println("Fonte: " +
                    build.getFonte().getNome());

            System.out.printf("Preço total: R$ %.2f%n", build.getPrecoTotal());

            System.out.println("Favorita: " +
                    (build.isFavorita() ? "Sim" : "Não"));
            break;
        }
    }

    public static void atualizarBuild() {

        System.out.println("\n========== ATUALIZAR BUILD ==========");

        int id = lerInteiro("Digite o ID da Build: ");

        Build build = buildDAO.buscarPorId(id);

        if (build == null) {
            System.out.println("Build não encontrada!");
            return;
        }

        String nome;
        do {
            System.out.print("Novo nome da Build: ");
            nome = sc.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("Erro! O nome da build não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        build.setNome(nome);

        while (true) {
            System.out.println("\nUSUÁRIOS DISPONÍVEIS");

            for (Usuario u : usuarioDAO.listar()) {
                System.out.println(u);
            }

            int idUsuario = lerInteiro("ID do Usuário: ");
            if (usuarioDAO.buscarPorId(idUsuario) != null) {
                build.setUsuarioId(idUsuario);
                break;
            }

            System.out.println("Erro! Esse usuário não existe. Escolha um ID da lista.");
        }

        // ==========================
        // PROCESSADOR
        // ==========================

        while (true) {
            System.out.println("\nPROCESSADORES DISPONÍVEIS");

            for (Processador p : processadorDAO.listar()) {
                System.out.println(p);
            }

            int idProcessador = lerInteiro("ID do Processador: ");
            Processador processadorSelecionado = processadorDAO.buscarPorId(idProcessador);
            if (processadorSelecionado != null) {
                build.setProcessador(processadorSelecionado);
                break;
            }

            System.out.println("Erro! Esse processador não existe. Escolha um ID da lista.");
        }

        // ==========================
        // PLACA MÃE
        // ==========================

        while (true) {
            System.out.println("\nPLACAS-MÃE DISPONÍVEIS");

            for (PlacaMae pm : placaMaeDAO.listar()) {
                System.out.println(pm);
            }

            int idPlacaMae = lerInteiro("ID da Placa-Mãe: ");
            PlacaMae placaMaeSelecionada = placaMaeDAO.buscarPorId(idPlacaMae);
            if (placaMaeSelecionada == null) {
                System.out.println("Erro! Essa placa-mãe não existe. Escolha um ID da lista.");
                continue;
            }

            if (!placaMaeSelecionada.getSocket().equalsIgnoreCase(build.getProcessador().getSocket())) {
                System.out.println("Erro! Essa placa-mãe não é compatível com o processador selecionado.");
                continue;
            }

            build.setPlacaMae(placaMaeSelecionada);
            break;
        }

        // ==========================
        // PLACA DE VÍDEO
        // ==========================

        while (true) {
            System.out.println("\nPLACAS DE VÍDEO DISPONÍVEIS");

            for (PlacaVideo pv : placaVideoDAO.listar()) {
                System.out.println(pv);
            }

            int idPlacaVideo = lerInteiro("ID da Placa de Vídeo: ");
            PlacaVideo placaVideoSelecionada = placaVideoDAO.buscarPorId(idPlacaVideo);
            if (placaVideoSelecionada != null) {
                build.setPlacaVideo(placaVideoSelecionada);
                break;
            }

            System.out.println("Erro! Essa placa de vídeo não existe. Escolha um ID da lista.");
        }

        // ==========================
        // MEMÓRIA
        // ==========================

        while (true) {
            System.out.println("\nMEMÓRIAS DISPONÍVEIS");

            for (Memoria m : memoriaDAO.listar()) {
                System.out.println(m);
            }

            int idMemoria = lerInteiro("ID da Memória: ");
            Memoria memoriaSelecionada = memoriaDAO.buscarPorId(idMemoria);
            if (memoriaSelecionada != null) {
                if (!memoriaSelecionada.getTipo().equalsIgnoreCase(build.getPlacaMae().getTipoMemoria())) {
                    System.out.println("Erro! A memória não é compatível com a placa-mãe selecionada.");
                    continue;
                }
                build.setMemoria(memoriaSelecionada);
                break;
            }

            System.out.println("Erro! Essa memória não existe. Escolha um ID da lista.");
        }

        // ==========================
        // SSD
        // ==========================

        while (true) {
            System.out.println("\nSSDs DISPONÍVEIS");

            for (SSD s : ssdDAO.listar()) {
                System.out.println(s);
            }

            int idSsd = lerInteiro("ID do SSD: ");
            SSD ssdSelecionado = ssdDAO.buscarPorId(idSsd);
            if (ssdSelecionado != null) {
                build.setSsd(ssdSelecionado);
                break;
            }

            System.out.println("Erro! Esse SSD não existe. Escolha um ID da lista.");
        }

        // ==========================
        // FONTE
        // ==========================

        while (true) {
            System.out.println("\nFONTES DISPONÍVEIS");

            for (Fonte f : fonteDAO.listar()) {
                System.out.println(f);
            }

            int idFonte = lerInteiro("ID da Fonte: ");
            Fonte fonteSelecionada = fonteDAO.buscarPorId(idFonte);
            if (fonteSelecionada != null) {
                build.setFonte(fonteSelecionada);
                if (!consumoService.fonteSuporta(build)) {
                    System.out.println("Erro! A fonte não possui a potência recomendada para a build.");
                    build.setFonte(null);
                    continue;
                }
                break;
            }

            System.out.println("Erro! Essa fonte não existe. Escolha um ID da lista.");
        }

        // ==========================
        // FAVORITA
        // ==========================

        String favorita;
        do {
            System.out.print("Favorita (true/false): ");
            favorita = sc.nextLine().trim();
            if (!favorita.equalsIgnoreCase("true") && !favorita.equalsIgnoreCase("false")) {
                System.out.println("Erro! Digite apenas true ou false.");
            }
        } while (!favorita.equalsIgnoreCase("true") && !favorita.equalsIgnoreCase("false"));
        build.setFavorita(Boolean.parseBoolean(favorita));

        buildDAO.atualizar(build);

        System.out.println("\nBuild atualizada com sucesso!");
    }

    public static void excluirBuild() {

        System.out.println("\n========== EXCLUIR BUILD ==========");

        int id = lerInteiro("Digite o ID da Build: ");

        Build build = buildDAO.buscarPorId(id);

        if (build == null) {
            System.out.println("\nBuild não encontrada!");
            return;
        }

        System.out.println("\nBuild encontrada:");
        System.out.println("ID: " + build.getId());
        System.out.println("Nome: " + build.getNome());

        System.out.print("\nDeseja realmente excluir esta build? (S/N): ");
        String resposta = sc.nextLine();

        if (resposta.equalsIgnoreCase("S")) {

            buildDAO.excluir(id);

            System.out.println("\nBuild excluída com sucesso!");

        } else {

            System.out.println("\nOperação cancelada.");

        }

    }

    public static void testarCompatibilidade() {

        System.out.println("\n========== TESTAR COMPATIBILIDADE ==========");

        while (true) {
            int id = lerInteiro("Digite o ID da Build: ");

            Build build = buildDAO.buscarPorId(id);

            if (build == null) {
                System.out.println("Build não encontrada!\n");
                continue;
            }

            boolean compativel = compatibilidadeService.verificarBuild(build);

            System.out.println("\nResultado da análise:");

            if (compativel) {
                System.out.println("A build é compatível!");
            } else {
                System.out.println("A build possui incompatibilidades!");
            }
            break;
        }
    }

    public static void calcularConsumo() {

        System.out.println("\n========== CALCULAR CONSUMO ==========");

        while (true) {
            int id = lerInteiro("Escolha o ID da build: ");

            Build build = buildDAO.buscarPorId(id);

            if (build == null) {
                System.out.println("Build não encontrada!\n");
                continue;
            }

            int consumo = consumoService.calcularConsumo(build);

            int recomendado = consumoService.consumoRecomendado(build);

            boolean suporta = consumoService.fonteSuporta(build);

            System.out.println("\n========== RESULTADO ==========");

            System.out.println("Build: " + build.getNome());

            System.out.println("Consumo estimado: "
                    + consumo + "W");

            System.out.println("Fonte recomendada: "
                    + recomendado + "W");

            if (suporta) {
                System.out.println("A fonte suporta essa configuração!");
            } else {
                System.out.println("A fonte não suporta essa configuração!");
            }
            break;
        }
    }

    public static void calcularFPS() {

        System.out.println("\n========== CALCULAR FPS ==========");

        while (true) {
            int id = lerInteiro("Escolha o ID da build: ");

            Build build = buildDAO.buscarPorId(id);

            if (build == null) {
                System.out.println("\nBuild não encontrada!");
                continue;
            }

            while (true) {
                System.out.println("\nJOGOS DISPONÍVEIS NO BANCO:");
                for (Jogo jogoCadastrado : jogoDAO.listar()) {
                    System.out.println(jogoCadastrado);
                }

                int idJogo = lerInteiro("Escolha o ID do jogo: ");
                Jogo jogo = jogoDAO.buscarPorId(idJogo);

                if (jogo == null) {
                    System.out.println("Jogo não encontrado! Digite um ID de jogo válido.");
                    continue;
                }

                System.out.print("Resolução (ex.: 1920x1080): ");
                String resolucao = sc.nextLine().trim();
                System.out.print("Qualidade (Baixo, Médio, Alto ou Ultra): ");
                String qualidade = sc.nextLine().trim();

                ResultadoFPS resultado = fpsService.analisar(
                        build,
                        jogo,
                        resolucao,
                        qualidade,
                        new BenchmarkFPSDAO().listar()
                );

                System.out.println("\n========== RESULTADO ==========");

                System.out.println("Build: " + build.getNome());

                System.out.println("Jogo: " + jogo.getNome());
                if (resultado.disponivel()) {
                    System.out.println("FPS médio de referência: " + resultado.fpsMedio());
                    if (resultado.fpsUmPorCento() != null) {
                        System.out.println("FPS 1% low: " + resultado.fpsUmPorCento());
                    }
                    System.out.println(resultado.tipoReferencia() + ": " + resultado.explicacao());
                    if (!resultado.fontes().isBlank()) {
                        System.out.println("Fontes: " + resultado.fontes());
                    }
                } else {
                    System.out.println("Sem resultado: " + resultado.explicacao());
                }
                return;
            }
        }
    }
}
