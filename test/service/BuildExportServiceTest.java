package service;
import org.junit.jupiter.api.Test;
import model.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class BuildExportServiceTest {
    private final BuildExportService service=new BuildExportService();
    private final Usuario u=new Usuario(7,"Pessoa","conta","hash-privado");
    private Build build() { return new Build(1,"<script>alert('x')</script>",7,
        new Processador(1,"CPU",700,"AMD","AM4",6,12,65,80),
        new PlacaMae(1,"Placa",400,"Marca","AM4","DDR4",40),
        new Memoria(1,"RAM",200,16,3200,"DDR4"),new PlacaVideo(1,"GPU",1000,"Marca",8,150,80),
        new SSD(1,"SSD",300,1000,3000,2500,"NVMe"),new Fonte(1,"Fonte",250,550,"Bronze"),false); }
    @Test void exportaHtmlSemExecutarTextoNemExporCredenciais() {
        String html=service.html(List.of(build()),u);
        assertTrue(html.contains("&lt;script&gt;"));assertFalse(html.contains("<script>"));
        assertFalse(html.contains("hash-privado"));assertFalse(html.contains("@conta"));assertTrue(html.contains("318 W"));
    }
    @Test void rejeitaBuildDeOutroUsuarioESessaoAusente() {
        Build b=build();b.setUsuarioId(9);
        assertThrows(SecurityException.class,()->service.texto(b,u));
        assertThrows(SecurityException.class,()->service.texto(build(),null));
        assertThrows(IllegalArgumentException.class,()->service.html(List.of(),u));
    }
    @Test void comparaTotaisDeDuasBuildsNoMesmoArquivo() {
        Build a=build(),b=build();b.setNome("Segunda build");b.getProcessador().setPreco(900);
        String html=service.html(List.of(a,b),u);
        assertTrue(html.contains("Segunda build"));assertTrue(html.contains(BuildExportService.moeda(2850)));
        assertTrue(html.contains(BuildExportService.moeda(3050)));
    }
}
