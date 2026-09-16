package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildTest {

    @Test
    void somaOPrecoDosComponentesPresentes() {
        Build build = new Build();
        build.setProcessador(new Processador(1, "CPU", 1000, "F", "AM5", 8, 16, 100, 100));
        build.setPlacaMae(new PlacaMae(1, "PM", 800, "F", "AM5", "DDR5", 50));
        build.setPlacaVideo(new PlacaVideo(1, "GPU", 2500, "F", 8, 200, 100));
        build.setMemoria(new Memoria(1, "RAM", 400, 16, 5000, "DDR5"));
        build.setSsd(new SSD(1, "SSD", 500, 1000, 3000, 2500, "NVMe"));
        build.setFonte(new Fonte(1, "Fonte", 600, 650, "80 Plus Gold"));

        assertEquals(5800, build.getPrecoTotal(), 0.001);
    }

    @Test
    void trataComponentesAusentesComoZero() {
        assertEquals(0, new Build().getPrecoTotal(), 0.001);
    }
}
