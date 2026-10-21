package service;

import model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static service.MontagemPC.Etapa.*;

class MontagemPCTest {
    private final Processador cpu = new Processador(1,"CPU AM4",700,"AMD","AM4",6,12,65,80);
    private final Processador outraCpu = new Processador(2,"CPU AM5",1000,"AMD","AM5",8,16,170,95);
    private final PlacaMae placa = new PlacaMae(1,"AM4 DDR4",400,"Marca","AM4","DDR4",40);
    private final PlacaMae placaDdr5 = new PlacaMae(2,"AM5 DDR5",600,"Marca","AM5","DDR5",50);
    private final Memoria ram = new Memoria(1,"16 GB DDR4",200,16,3200,"DDR4");
    private final Memoria ramDdr5 = new Memoria(2,"16 GB DDR5",300,16,5600,"DDR5");
    private final PlacaVideo gpu = new PlacaVideo(1,"GPU",1000,"Marca",8,150,80);
    private final SSD ssd = new SSD(1,"SSD",300,1000,3000,2500,"NVMe");
    private final Fonte fonte = new Fonte(1,"Fonte 350 W",250,350,"Bronze");
    private final Fonte fonteMaior = new Fonte(2,"Fonte 650 W",350,650,"Bronze");

    private MontagemPC.Catalogo catalogo() {
        return new MontagemPC.Catalogo(List.of(cpu,outraCpu),List.of(placa,placaDdr5),List.of(ram,ramDdr5),
                List.of(gpu),List.of(ssd),List.of(fonte,fonteMaior));
    }
    private MontagemPC completa() {
        MontagemPC e = new MontagemPC(7);
        for (Componente c : List.of(cpu,placa,ram,gpu,ssd,fonte)) { e.selecionar(c); e.avancar(); }
        return e;
    }

    @Test void exigeEscolhaAntesDeAvancarENaoPermitePularEtapas() {
        MontagemPC e = new MontagemPC(7);
        assertEquals(PROCESSADOR,e.etapa()); assertFalse(e.podeAvancar());
        assertThrows(IllegalStateException.class,e::avancar);
        assertThrows(IllegalStateException.class,()->e.irPara(REVISAO));
        e.selecionar(cpu); e.avancar(); assertEquals(PLACA_MAE,e.etapa());
    }
    @Test void voltarPreservaSelecoesEDepoisPermiteAvancarNovamente() {
        MontagemPC e = completa(); e.voltar(); e.voltar();
        assertEquals(SSD,e.etapa()); assertSame(ssd,e.selecionado(SSD));
        e.irPara(PROCESSADOR); assertSame(cpu,e.selecionado(PROCESSADOR));
        e.avancar(); assertSame(placa,e.selecionado(PLACA_MAE));
    }
    @Test void filtraPlacaMaeMemoriaEFonteConformeEscolhas() {
        MontagemPC e = new MontagemPC(7); e.selecionar(outraCpu);e.avancar();
        assertEquals(List.of(placaDdr5),e.opcoes(catalogo())); e.selecionar(placaDdr5);e.avancar();
        assertEquals(List.of(ramDdr5),e.opcoes(catalogo()));
        e.selecionar(ramDdr5); e.avancar();e.selecionar(gpu);e.avancar();e.selecionar(ssd);e.avancar();
        assertEquals(List.of(fonteMaior),e.opcoes(catalogo()));
    }
    @Test void trocaDeCpuRemoveSomenteDependenciasInvalidas() {
        MontagemPC e = completa();e.irPara(PROCESSADOR);List<String> removidas=e.selecionar(outraCpu);
        assertTrue(removidas.contains("Placa-mãe"));assertTrue(removidas.contains("Memória"));
        assertNull(e.selecionado(PLACA_MAE));assertNull(e.selecionado(MEMORIA));assertSame(gpu,e.selecionado(PLACA_VIDEO));
        assertSame(ssd,e.selecionado(SSD));assertFalse(e.podeIrPara(REVISAO));
    }
    @Test void trocaPorCpuDoMesmoSocketPreservaPlacaEMemoriaCompativeis() {
        MontagemPC e = completa();e.irPara(PROCESSADOR);
        Processador similar=new Processador(3,"Outra AM4",900,"AMD","AM4",8,16,65,90);
        assertTrue(e.selecionar(similar).isEmpty());assertSame(placa,e.selecionado(PLACA_MAE));assertSame(ram,e.selecionado(MEMORIA));
    }
    @Test void recalculaConsumoERejeitaFonteInsuficienteDepoisDaTrocaDeGpu() {
        MontagemPC e = completa();e.irPara(PLACA_VIDEO);
        PlacaVideo maior=new PlacaVideo(2,"GPU 300 W",2000,"Marca",12,300,120);
        assertTrue(e.selecionar(maior).contains("Fonte"));assertNull(e.selecionado(FONTE));
        e.irPara(FONTE);assertThrows(IllegalArgumentException.class,()->e.selecionar(fonte));
        assertTrue(e.fonteRecomendada()>350);e.selecionar(fonteMaior);e.avancar();assertEquals(REVISAO,e.etapa());
    }
    @Test void rejeitaPecaDeCategoriaErradaECombinacoesIncompativeis() {
        MontagemPC e=new MontagemPC(7);assertThrows(IllegalArgumentException.class,()->e.selecionar(gpu));
        e.selecionar(cpu);e.avancar();assertThrows(IllegalArgumentException.class,()->e.selecionar(placaDdr5));
        e.selecionar(placa);e.avancar();assertThrows(IllegalArgumentException.class,()->e.selecionar(ramDdr5));
    }
    @Test void salvarExigeSeisPecasENomeEDevolveSnapshotDoRascunho() {
        assertThrows(IllegalArgumentException.class,()->new MontagemPC(7).finalizar());
        MontagemPC e=completa();assertThrows(IllegalArgumentException.class,e::finalizar);
        e.definirNome("  Meu PC  ");e.definirFavorita(true);Build b=e.finalizar();
        assertEquals("Meu PC",b.getNome());assertEquals(7,b.getUsuarioId());assertTrue(b.isFavorita());
        e.definirNome("Outro nome");assertEquals("Meu PC",b.getNome());
    }
    @Test void edicaoComecaNaRevisaoEMantemIdProprietarioESemAlterarOriginal() {
        Build b=completaComNome();b.setId(55);MontagemPC e=new MontagemPC(7);e.editar(b);
        assertEquals(REVISAO,e.etapa());assertFalse(e.modificado());e.definirNome("Editada");
        assertEquals("Meu PC",b.getNome());assertEquals(55,e.finalizar().getId());assertTrue(e.editando());
        assertThrows(SecurityException.class,()->new MontagemPC(8).editar(b));
    }
    @Test void buildAntigaComFonteInvalidaAbreNaEtapaQuePrecisaDeCorrecao() {
        Build b=completaComNome();b.setFonte(new Fonte(4,"Insuficiente",100,100,"Bronze"));
        MontagemPC e=new MontagemPC(7);e.editar(b);assertEquals(FONTE,e.etapa());assertFalse(e.podeAvancar());
    }
    private Build completaComNome() { MontagemPC e=completa();e.definirNome("Meu PC");return e.finalizar(); }
}
