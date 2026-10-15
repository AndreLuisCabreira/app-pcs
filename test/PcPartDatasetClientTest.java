import com.fasterxml.jackson.databind.ObjectMapper;
import model.ProdutoCatalogo;
import model.TipoComponenteCatalogo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PcPartDatasetClientTest {

    private final PcPartDatasetClient client = new PcPartDatasetClient(
            null, new ObjectMapper()
    );

    @Test
    void converteCpuComOsCamposDisponiveis() throws Exception {
        String json = """
                [{
                  "name":"AMD Ryzen 7 9800X3D",
                  "price":451.5,
                  "core_count":8,
                  "core_clock":4.7,
                  "boost_clock":5.2,
                  "microarchitecture":"Zen 5",
                  "tdp":120,
                  "graphics":"Radeon"
                }]
                """;

        ProdutoCatalogo cpu = client.lerProdutos(TipoComponenteCatalogo.PROCESSADOR, json).getFirst();

        assertEquals("AMD", cpu.getFabricante());
        assertEquals("8", cpu.buscarDetalhe("Núcleos"));
        assertEquals("120", cpu.buscarDetalhe("TDP"));
        assertFalse(cpu.getIdExterno().isBlank());
    }

    @Test
    void calculaCapacidadeTotalDaMemoria() throws Exception {
        String json = """
                [{"name":"Corsair Vengeance 32 GB","price":94.99,
                  "speed":[5,6000],"modules":[2,16],"cas_latency":36}]
                """;

        ProdutoCatalogo memoria = client.lerProdutos(TipoComponenteCatalogo.MEMORIA, json).getFirst();

        assertEquals("DDR5", memoria.buscarDetalhe("Tipo"));
        assertEquals("6000", memoria.buscarDetalhe("Frequência"));
        assertEquals("32", memoria.buscarDetalhe("Capacidade"));
    }

    @Test
    void removeDiscosQueNaoSaoSsd() throws Exception {
        String json = """
                [
                  {"name":"SSD Exemplo","price":50,"capacity":1000,"type":"SSD","interface":"M.2 PCIe"},
                  {"name":"HDD Exemplo","price":40,"capacity":2000,"type":"7200","interface":"SATA"}
                ]
                """;

        List<ProdutoCatalogo> produtos = client.lerProdutos(TipoComponenteCatalogo.SSD, json);

        assertEquals(1, produtos.size());
        assertEquals("SSD Exemplo", produtos.getFirst().getNome());
    }

    @Test
    void geraIdentidadeDiferenteQuandoEspecificacoesMudam() throws Exception {
        String json = """
                [
                  {"name":"GPU Exemplo","price":100,"chipset":"Chip A","memory":8},
                  {"name":"GPU Exemplo","price":110,"chipset":"Chip A","memory":16}
                ]
                """;

        List<ProdutoCatalogo> produtos = client.lerProdutos(TipoComponenteCatalogo.PLACA_VIDEO, json);

        assertNotEquals(produtos.get(0).getIdExterno(), produtos.get(1).getIdExterno());
    }
}
