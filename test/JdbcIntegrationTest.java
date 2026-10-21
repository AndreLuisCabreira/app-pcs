import org.junit.jupiter.api.Test;
import model.*;
import java.sql.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class JdbcIntegrationTest {
    @Test void buildsSaoIsoladasEOperacoesDeOutroUsuarioSaoRejeitadas() throws Exception {
        BancoTeste db = new BancoTeste();
        Usuario a=db.usuario("conta-a"), b=db.usuario("conta-b");
        BuildDAO dao=new BuildDAO(db::abrir);dao.inserir(db.build(a.getId()));
        Build original=dao.listarPorUsuario(a.getId()).getFirst();
        assertTrue(dao.listarPorUsuario(b.getId()).isEmpty());
        assertNull(dao.buscarPorIdDoUsuario(original.getId(),b.getId()));
        original.setNome("Tentativa de alteração");
        assertThrows(DataAccessException.class,()->dao.atualizarDoUsuario(original,b.getId()));
        assertThrows(DataAccessException.class,()->dao.excluirDoUsuario(original.getId(),b.getId()));
        assertEquals("Build",dao.buscarPorIdDoUsuario(original.getId(),a.getId()).getNome());
        original.setNome("Meu PC ' OR '1'='1");dao.atualizarDoUsuario(original,a.getId());
        assertEquals(original.getNome(),dao.buscarPorIdDoUsuario(original.getId(),a.getId()).getNome());
        dao.excluirDoUsuario(original.getId(),a.getId());assertEquals(0,db.contar("build"));
    }
    @Test void importacaoDuplicadaNaoCriaOutroComponente() throws Exception {
        BancoTeste db=new BancoTeste();CatalogoImportacaoDAO dao=new CatalogoImportacaoDAO(db::abrir);
        ProdutoCatalogo produto=new ProdutoCatalogo("externo","Fonte","Marca",null,Map.of());
        Fonte fonte=new Fonte(0,"Fonte",250,550,"Bronze");
        assertTrue(dao.importar(TipoComponenteCatalogo.FONTE,produto,fonte)>0);
        assertThrows(IllegalArgumentException.class,()->dao.importar(TipoComponenteCatalogo.FONTE,produto,fonte));
        assertEquals(1,db.contar("fonte"));assertEquals(1,db.contar("componente_importado"));
    }
    @Test void falhaNoRegistroDaImportacaoReverteAInsercaoDaPeca() throws Exception {
        BancoTeste db=new BancoTeste();CatalogoImportacaoDAO dao=new CatalogoImportacaoDAO(db::abrir);
        ProdutoCatalogo invalido=new ProdutoCatalogo("x","x".repeat(300),"Marca",null,Map.of());
        assertThrows(DataAccessException.class,()->dao.importar(TipoComponenteCatalogo.FONTE,invalido,new Fonte(0,"Fonte",100,500,"Bronze")));
        assertEquals(0,db.contar("fonte"));assertEquals(0,db.contar("componente_importado"));
    }
    @Test void alteraSenhaComReautenticacaoENaoMantemHashNaSessao() throws Exception {
        BancoTeste db=new BancoTeste();UsuarioDAO dao=new UsuarioDAO(db::abrir);db.usuario("conta");
        AutenticacaoService service=new AutenticacaoService(dao,new service.PasswordService());
        Usuario sessao=service.autenticar("conta","senha-antiga");
        service.alterarSenha(sessao,"senha-antiga","senha-nova","senha-nova");
        assertThrows(IllegalArgumentException.class,()->service.autenticar("conta","senha-antiga"));
        assertEquals(sessao.getId(),service.autenticar("conta","senha-nova").getId());assertNull(sessao.getSenha());
    }
    @Test void rejeitaTrocaDeSenhaInvalidaSemModificarConta() throws Exception {
        BancoTeste db=new BancoTeste();UsuarioDAO dao=new UsuarioDAO(db::abrir);db.usuario("conta");
        AutenticacaoService service=new AutenticacaoService(dao,new service.PasswordService());
        Usuario sessao=service.autenticar("conta","senha-antiga");
        assertThrows(IllegalArgumentException.class,()->service.alterarSenha(sessao,"errada","senha-nova","senha-nova"));
        assertThrows(IllegalArgumentException.class,()->service.alterarSenha(sessao,"senha-antiga","curta","curta"));
        assertThrows(IllegalArgumentException.class,()->service.alterarSenha(sessao,"senha-antiga","senha-nova","outra"));
        assertThrows(IllegalArgumentException.class,()->service.alterarSenha(sessao,"senha-antiga","senha-antiga","senha-antiga"));
        assertThrows(SecurityException.class,()->service.alterarSenha(null,"senha-antiga","senha-nova","senha-nova"));
        assertNotNull(service.autenticar("conta","senha-antiga"));
    }
    @Test void trocaConcorrenteNaoSobrescreveSenhaQueJaMudou() throws Exception {
        BancoTeste db=new BancoTeste();Usuario u=db.usuario("conta");UsuarioDAO dao=new UsuarioDAO(db::abrir);
        String anterior=dao.buscarPorId(u.getId()).getSenha();
        dao.alterarSenha(u.getId(),anterior,"novo-hash");
        assertThrows(IllegalArgumentException.class,()->dao.alterarSenha(u.getId(),anterior,"outro-hash"));
        assertEquals("novo-hash",dao.buscarPorId(u.getId()).getSenha());
    }
    @Test void transferenciaPreservaMetadadosDosJogos() throws Exception {
        BancoTeste origem=new BancoTeste(), destino=new BancoTeste();
        origem.executar("UPDATE jogo SET genero='Ação', descricao='Descrição com acento', imagem_url='https://example.org/capa.webp' WHERE id=1");
        destino.executar("DELETE FROM jogo");
        String[] colunas=DatabaseTransfer.colunasDaTabela("jogo");
        try(Connection a=origem.abrir();Connection b=destino.abrir()) {
            DatabaseTransfer.copiarTabela(a,b,"jogo",colunas);
            try(Statement s=b.createStatement();ResultSet r=s.executeQuery("SELECT * FROM jogo WHERE id=1")) {
                assertTrue(r.next());assertEquals("Ação",r.getString("genero"));
                assertEquals("Descrição com acento",r.getString("descricao"));assertEquals("https://example.org/capa.webp",r.getString("imagem_url"));
            }
        }
    }
    @Test void atualizadorPodeSerExecutadoDuasVezesNoBancoDescartavel() throws Exception {
        BancoTeste db=new BancoTeste();
        db.executar("ALTER TABLE jogo DROP COLUMN imagem_url");
        try(Connection c=db.abrir()) {
            DatabaseMigrationService m=new DatabaseMigrationService();m.aplicar(c);m.aplicar(c);
            try(ResultSet r=c.getMetaData().getColumns(null,null,"jogo","imagem_url")) { assertTrue(r.next()); }
        }
    }
}
