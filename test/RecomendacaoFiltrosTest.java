import org.junit.jupiter.api.Test;
import model.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class RecomendacaoFiltrosTest {
    private List<RecomendacaoJogo> recomendar(String resolucao, int fps, double orcamento) {
        BenchmarkFPS b=new BenchmarkFPS();
        b.setId(1);b.setJogoId(1);b.setProcessadorId(1);b.setPlacaVideoId(1);
        b.setResolucao("2560x1440");b.setQualidade("Alto");b.setFpsMedio(75);b.setFonte("Teste");
        return new RecomendacaoJogoService().recomendar(
            List.of(new Jogo(1,"Jogo",1,1)),List.of(b),
            List.of(new Processador(1,"CPU",700,"AMD","AM4",6,12,65,80)),
            List.of(new PlacaVideo(1,"GPU",1000,"Marca",8,150,80)),
            List.of(new PlacaMae(1,"Placa",400,"Marca","AM4","DDR4",40)),
            List.of(new Memoria(1,"RAM",200,16,3200,"DDR4")),
            List.of(new SSD(1,"SSD",300,1000,3000,2500,"NVMe")),
            List.of(new Fonte(1,"Fonte",250,550,"Bronze")),resolucao,fps,orcamento);
    }
    @Test void aceitaOrcamentoExatoERejeitaValorInferior() {
        assertTrue(recomendar("2560x1440",60,2850).getFirst().disponivel());
        assertFalse(recomendar("2560x1440",60,2849).getFirst().disponivel());
    }
    @Test void filtraResolucaoEMetaDeFpsSemInventarResultados() {
        assertFalse(recomendar("1920x1080",60,5000).getFirst().disponivel());
        assertFalse(recomendar("2560x1440",76,5000).getFirst().disponivel());
    }
    @Test void rejeitaLimitesInvalidos() {
        assertThrows(IllegalArgumentException.class,()->recomendar("2560x1440",0,5000));
        assertThrows(IllegalArgumentException.class,()->recomendar("2560x1440",60,Double.NaN));
        assertThrows(IllegalArgumentException.class,()->recomendar("2560x1440",60,-1));
    }
}
