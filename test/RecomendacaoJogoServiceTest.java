import model.BenchmarkFPS;
import model.Fonte;
import model.Jogo;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.RecomendacaoJogo;
import model.SSD;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecomendacaoJogoServiceTest {

    private final RecomendacaoJogoService service = new RecomendacaoJogoService();

    @Test
    void recomendaConfiguracaoCompativelMaisBarataQueAtingeSessentaFps() {
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);
        Processador cpuBarata = new Processador(10, "CPU econômica", 700, "AMD", "AM4", 6, 12, 65, 80);
        Processador cpuCara = new Processador(11, "CPU cara", 1700, "AMD", "AM5", 8, 16, 105, 120);
        PlacaVideo gpuBarata = new PlacaVideo(20, "GPU econômica", 1200, "NVIDIA", 8, 160, 80);
        PlacaVideo gpuCara = new PlacaVideo(21, "GPU cara", 3200, "NVIDIA", 12, 250, 130);

        List<RecomendacaoJogo> resultado = service.recomendar(
                List.of(jogo),
                List.of(
                        benchmark(1, 10, 20, 62),
                        benchmark(1, 11, 21, 120)
                ),
                List.of(cpuBarata, cpuCara),
                List.of(gpuBarata, gpuCara),
                List.of(
                        new PlacaMae(30, "Placa AM4", 500, "A", "AM4", "DDR4", 45),
                        new PlacaMae(31, "Placa AM5", 900, "A", "AM5", "DDR5", 55)
                ),
                List.of(
                        new Memoria(40, "16 GB DDR4", 250, 16, 3200, "DDR4"),
                        new Memoria(41, "16 GB DDR5", 500, 16, 5600, "DDR5")
                ),
                List.of(new SSD(50, "SSD 1 TB", 350, 1000, 3000, 2500, "NVMe")),
                List.of(new Fonte(60, "Fonte 550 W", 300, 550, "80 Plus Bronze"))
        );

        RecomendacaoJogo recomendacao = resultado.getFirst();
        assertTrue(recomendacao.disponivel());
        assertEquals("CPU econômica", recomendacao.build().getProcessador().getNome());
        assertEquals("GPU econômica", recomendacao.build().getPlacaVideo().getNome());
        assertEquals(62, recomendacao.benchmark().getFpsMedio());
    }

    @Test
    void informaIndisponibilidadeSemBenchmarkDeSessentaFps() {
        Jogo jogo = new Jogo(1, "Jogo", 1, 1);
        List<RecomendacaoJogo> resultado = service.recomendar(
                List.of(jogo), List.of(benchmark(1, 10, 20, 59)),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );

        assertFalse(resultado.getFirst().disponivel());
    }

    private BenchmarkFPS benchmark(int jogoId, int cpuId, int gpuId, int fps) {
        BenchmarkFPS item = new BenchmarkFPS();
        item.setJogoId(jogoId);
        item.setProcessadorId(cpuId);
        item.setPlacaVideoId(gpuId);
        item.setResolucao("1920x1080");
        item.setQualidade("Alto");
        item.setFpsMedio(fps);
        item.setFonte("Fonte de teste");
        return item;
    }
}
