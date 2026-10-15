import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.ProdutoCatalogo;
import model.TipoComponenteCatalogo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PcPartDatasetClient {

    static final String BASE_URL =
            "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final Map<TipoComponenteCatalogo, List<ProdutoCatalogo>> cache =
            new EnumMap<>(TipoComponenteCatalogo.class);

    public PcPartDatasetClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(12)).build(), new ObjectMapper());
    }

    PcPartDatasetClient(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public synchronized List<ProdutoCatalogo> listar(TipoComponenteCatalogo tipo, boolean atualizar) {
        if (!atualizar && cache.containsKey(tipo)) {
            return cache.get(tipo);
        }

        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(BASE_URL + tipo.getArquivoDataset())
                )
                .timeout(Duration.ofSeconds(40))
                .header("Accept", "application/json")
                .header("User-Agent", "IntraTech-PC-Builder")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "O GitHub respondeu com erro HTTP " + response.statusCode() + "."
                );
            }
            List<ProdutoCatalogo> produtos = List.copyOf(lerProdutos(tipo, response.body()));
            cache.put(tipo, produtos);
            return produtos;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("O carregamento do dataset foi interrompido.", e);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Não foi possível carregar o PC Part Dataset.", e);
        }
    }

    List<ProdutoCatalogo> lerProdutos(TipoComponenteCatalogo tipo, String json) throws IOException {
        JsonNode raiz = mapper.readTree(json);
        if (!raiz.isArray()) {
            throw new IOException("O arquivo do dataset não contém uma lista JSON.");
        }

        List<ProdutoCatalogo> produtos = new ArrayList<>();
        for (JsonNode item : raiz) {
            if (tipo == TipoComponenteCatalogo.SSD
                    && !"SSD".equalsIgnoreCase(item.path("type").asText())) {
                continue;
            }
            String nome = item.path("name").asText("").trim();
            if (nome.isBlank()) {
                continue;
            }
            JsonNode preco = item.path("price");
            Map<String, String> detalhes = detalhes(tipo, item);
            String identidade = tipo.name() + "|" + nome + "|" + detalhes;
            String idExterno = UUID.nameUUIDFromBytes(
                    identidade.getBytes(StandardCharsets.UTF_8)
            ).toString();

            produtos.add(new ProdutoCatalogo(
                    idExterno,
                    nome,
                    inferirFabricante(nome),
                    preco.isNumber() ? preco.asDouble() : null,
                    detalhes
            ));
        }
        return produtos;
    }

    private Map<String, String> detalhes(TipoComponenteCatalogo tipo, JsonNode item) {
        Map<String, String> detalhes = new LinkedHashMap<>();
        switch (tipo) {
            case PROCESSADOR -> {
                adicionarNumero(detalhes, "Núcleos", item, "core_count");
                adicionarNumero(detalhes, "TDP", item, "tdp");
                adicionarNumero(detalhes, "Clock base", item, "core_clock");
                adicionarNumero(detalhes, "Clock boost", item, "boost_clock");
                adicionarTexto(detalhes, "Microarquitetura", item, "microarchitecture");
                adicionarTexto(detalhes, "Gráficos integrados", item, "graphics");
                if (item.path("smt").isBoolean()) {
                    detalhes.put("SMT", item.path("smt").asBoolean() ? "Sim" : "Não");
                }
            }
            case PLACA_VIDEO -> {
                adicionarTexto(detalhes, "Chipset", item, "chipset");
                adicionarNumero(detalhes, "VRAM", item, "memory");
                adicionarNumero(detalhes, "Clock base", item, "core_clock");
                adicionarNumero(detalhes, "Clock boost", item, "boost_clock");
                adicionarNumero(detalhes, "Comprimento", item, "length");
            }
            case FONTE -> {
                adicionarNumero(detalhes, "Potência", item, "wattage");
                adicionarTexto(detalhes, "Eficiência", item, "efficiency");
                adicionarTexto(detalhes, "Formato", item, "type");
                adicionarTexto(detalhes, "Modular", item, "modular");
            }
            case MEMORIA -> {
                JsonNode velocidade = item.path("speed");
                if (velocidade.isArray() && velocidade.size() >= 2) {
                    detalhes.put("Tipo", "DDR" + velocidade.get(0).asInt());
                    detalhes.put("Frequência", velocidade.get(1).asText());
                }
                JsonNode modulos = item.path("modules");
                if (modulos.isArray() && modulos.size() >= 2) {
                    int quantidade = modulos.get(0).asInt();
                    int tamanho = modulos.get(1).asInt();
                    detalhes.put("Capacidade", String.valueOf(quantidade * tamanho));
                    detalhes.put("Módulos", quantidade + " x " + tamanho + " GB");
                }
                adicionarNumero(detalhes, "Latência CAS", item, "cas_latency");
            }
            case SSD -> {
                adicionarNumero(detalhes, "Capacidade", item, "capacity");
                adicionarTexto(detalhes, "Tipo", item, "type");
                adicionarTexto(detalhes, "Formato", item, "form_factor");
                adicionarTexto(detalhes, "Interface", item, "interface");
                adicionarNumero(detalhes, "Cache", item, "cache");
            }
        }
        return detalhes;
    }

    private void adicionarNumero(Map<String, String> detalhes, String nome, JsonNode item, String propriedade) {
        JsonNode valor = item.path(propriedade);
        if (valor.isNumber()) {
            detalhes.put(nome, valor.asText());
        }
    }

    private void adicionarTexto(Map<String, String> detalhes, String nome, JsonNode item, String propriedade) {
        JsonNode valor = item.path(propriedade);
        if (valor.isTextual() && !valor.asText().isBlank()) {
            detalhes.put(nome, valor.asText());
        } else if (valor.isBoolean()) {
            detalhes.put(nome, valor.asBoolean() ? "Sim" : "Não");
        }
    }

    private String inferirFabricante(String nome) {
        String[] fabricantesCompostos = {
                "Western Digital", "Silicon Power", "Cooler Master", "be quiet!",
                "Team Group", "Thermalright", "Super Flower"
        };
        String minusculo = nome.toLowerCase(Locale.ROOT);
        for (String fabricante : fabricantesCompostos) {
            if (minusculo.startsWith(fabricante.toLowerCase(Locale.ROOT))) {
                return fabricante;
            }
        }
        int espaco = nome.indexOf(' ');
        return espaco > 0 ? nome.substring(0, espaco) : nome;
    }
}
