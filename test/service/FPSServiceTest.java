package service;

import model.BenchmarkFPS;
import model.Build;
import model.Jogo;
import model.PlacaVideo;
import model.Processador;
import model.ResultadoFPS;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FPSServiceTest {

    private final FPSService service = new FPSService();

    @Test
    void calculaMediaDeMedicoesExatas() {
        Build build = criarBuild(10, 100, 20);
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);

        ResultadoFPS resultado = service.analisar(build, jogo, "1920x1080", "Alto", List.of(
                benchmark(1, 10, 20, 100, 90, 65),
                benchmark(1, 10, 20, 100, 94, 69)
        ));

        assertEquals(92, resultado.fpsMedio());
        assertEquals(67, resultado.fpsUmPorCento());
        assertEquals(2, resultado.amostras());
        assertEquals("MEDIÇÃO EXATA", resultado.tipoReferencia());
    }

    @Test
    void usaCpuDeReferenciaMaisProximaQuandoGpuEIgual() {
        Build build = criarBuild(10, 115, 20);
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);

        ResultadoFPS resultado = service.analisar(build, jogo, "1920x1080", "Alto", List.of(
                benchmark(1, 11, 20, 110, 88, null),
                benchmark(1, 12, 20, 160, 105, null)
        ));

        assertEquals(88, resultado.fpsMedio());
        assertEquals("REFERÊNCIA PRÓXIMA", resultado.tipoReferencia());
    }

    @Test
    void naoInventaFpsSemBenchmarkDaMesmaGpu() {
        Build build = criarBuild(10, 100, 20);
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);

        ResultadoFPS resultado = service.analisar(build, jogo, "1920x1080", "Alto", List.of(
                benchmark(1, 10, 99, 100, 90, 65)
        ));

        assertFalse(resultado.disponivel());
        assertEquals("SEM DADOS", resultado.tipoReferencia());
    }

    @Test
    void separaResolucaoEQualidade() {
        Build build = criarBuild(10, 100, 20);
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);

        ResultadoFPS resultado = service.analisar(build, jogo, "2560x1440", "Ultra", List.of(
                benchmark(1, 10, 20, 100, 90, 65)
        ));

        assertFalse(resultado.disponivel());
    }

    @Test
    void rejeitaBuildIncompleta() {
        ResultadoFPS resultado = service.analisar(
                new Build(), new Jogo(1, "Jogo", 1, 1), "1920x1080", "Alto", List.of()
        );

        assertFalse(resultado.disponivel());
        assertTrue(resultado.explicacao().contains("processador"));
    }

    private Build criarBuild(int cpuId, double cpuDesempenho, int gpuId) {
        Build build = new Build();
        build.setProcessador(new Processador(cpuId, "CPU", 0, "F", "AM5", 8, 16, 100, cpuDesempenho));
        build.setPlacaVideo(new PlacaVideo(gpuId, "GPU", 0, "F", 8, 200, 100));
        return build;
    }

    private BenchmarkFPS benchmark(
            int jogoId,
            int cpuId,
            int gpuId,
            double cpuDesempenho,
            int fpsMedio,
            Integer fpsUmLow
    ) {
        BenchmarkFPS item = new BenchmarkFPS();
        item.setJogoId(jogoId);
        item.setProcessadorId(cpuId);
        item.setProcessadorNome("CPU " + cpuId);
        item.setProcessadorDesempenho(cpuDesempenho);
        item.setPlacaVideoId(gpuId);
        item.setResolucao("1920x1080");
        item.setQualidade("Alto");
        item.setFpsMedio(fpsMedio);
        item.setFpsUmPorCento(fpsUmLow);
        return item;
    }
}
