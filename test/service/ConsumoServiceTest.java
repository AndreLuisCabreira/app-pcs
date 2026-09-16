package service;

import model.Build;
import model.Fonte;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.SSD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumoServiceTest {

    private final ConsumoService service = new ConsumoService();

    @Test
    void calculaConsumoMargemEFonteSuficiente() {
        Build build = criarBuild(500);
        assertEquals(360, service.calcularConsumo(build));
        assertEquals(432, service.consumoRecomendado(build));
        assertTrue(service.fonteSuporta(build));
    }

    @Test
    void rejeitaFonteAbaixoDaRecomendacao() {
        assertFalse(service.fonteSuporta(criarBuild(400)));
    }

    @Test
    void rejeitaBuildSemFonte() {
        assertFalse(service.fonteSuporta(new Build()));
    }

    private Build criarBuild(int potenciaFonte) {
        Build build = new Build();
        build.setProcessador(new Processador(1, "CPU", 0, "F", "AM5", 8, 16, 100, 100));
        build.setPlacaMae(new PlacaMae(1, "PM", 0, "F", "AM5", "DDR5", 50));
        build.setPlacaVideo(new PlacaVideo(1, "GPU", 0, "F", 8, 200, 100));
        build.setMemoria(new Memoria(1, "RAM", 0, 16, 5000, "DDR5"));
        build.setSsd(new SSD(1, "SSD", 0, 1000, 3000, 2500, "NVMe"));
        build.setFonte(new Fonte(1, "Fonte", 0, potenciaFonte, "80 Plus"));
        return build;
    }
}
