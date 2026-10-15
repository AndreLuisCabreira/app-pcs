package service;

import model.Build;
import model.Memoria;
import model.PlacaMae;
import model.Processador;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilidadeServiceTest {

    private final CompatibilidadeService service = new CompatibilidadeService();

    @Test
    void aceitaSocketETipoDeMemoriaCompativeis() {
        Build build = criarBuild("AM5", "AM5", "DDR5", "DDR5");
        assertTrue(service.verificarBuild(build));
    }

    @Test
    void rejeitaSocketIncompativel() {
        Build build = criarBuild("AM4", "AM5", "DDR5", "DDR5");
        assertFalse(service.verificarBuild(build));
    }

    @Test
    void rejeitaTipoDeMemoriaIncompativel() {
        Build build = criarBuild("AM5", "AM5", "DDR4", "DDR5");
        assertFalse(service.verificarBuild(build));
    }

    @Test
    void rejeitaBuildIncompleta() {
        assertFalse(service.verificarBuild(new Build()));
    }

    private Build criarBuild(String socketCpu, String socketPlacaMae,
                             String tipoMemoria, String tipoPlacaMae) {
        Build build = new Build();
        build.setProcessador(new Processador(1, "CPU", 0, "Fabricante", socketCpu, 8, 16, 100, 100));
        build.setPlacaMae(new PlacaMae(1, "Placa-mãe", 0, "Fabricante", socketPlacaMae, tipoPlacaMae, 50));
        build.setMemoria(new Memoria(1, "RAM", 0, 16, 3200, tipoMemoria));
        return build;
    }
}
