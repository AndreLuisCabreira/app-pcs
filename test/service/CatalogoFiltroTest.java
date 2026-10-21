package service;
import org.junit.jupiter.api.Test;
import model.*;
import static org.junit.jupiter.api.Assertions.*;
class CatalogoFiltroTest {
    private final Processador cpu=new Processador(1,"Processador econômico",700,"AMD","AM4",6,12,65,80);
    @Test void filtraPorFabricanteSocketEAcentosSemDependerDaOrdem() {
        assertTrue(CatalogoFiltro.corresponde(cpu,"amd economico am4",0,1000));
        assertFalse(CatalogoFiltro.corresponde(cpu,"intel",0,1000));
    }
    @Test void respeitaFaixaDePrecoInclusiva() {
        assertTrue(CatalogoFiltro.corresponde(cpu,"",700,700));
        assertFalse(CatalogoFiltro.corresponde(cpu,"",0,699));
        assertFalse(CatalogoFiltro.corresponde(cpu,"",701,1000));
    }
}
