import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseBackupServiceTest {
    @TempDir Path pasta;
    private Usuario admin(BancoTeste db) throws Exception {
        Usuario u=db.usuario("admin");db.executar("UPDATE usuario SET perfil='ADMIN' WHERE id="+u.getId());
        return new UsuarioDAO(db::abrir).buscarPorId(u.getId());
    }
    @Test void salvaERestauraContasBuildsEMetadadosDeJogos() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);BuildDAO dao=new BuildDAO(db::abrir);
        Build b=db.build(admin.getId());b.setFavorita(true);dao.inserir(b);
        db.executar("UPDATE jogo SET genero='Ação',descricao='Texto preservado',imagem_url='https://example.org/capa.webp' WHERE id=1");
        db.executar("INSERT INTO benchmark_fps (jogo_id,processador_id,placa_video_id,resolucao,qualidade,fps_medio,fonte) VALUES (1,1,1,'1920x1080','Alto',90,'Fonte')");
        Path arquivo=pasta.resolve("backup.json");DatabaseBackupService service=new DatabaseBackupService(db::abrir);
        service.exportar(admin,"senha-antiga",arquivo);
        Build atual=dao.listarPorUsuario(admin.getId()).getFirst();atual.setNome("Alterada");dao.atualizarDoUsuario(atual,admin.getId());
        db.executar("UPDATE jogo SET genero=NULL,descricao=NULL,imagem_url=NULL");
        service.restaurar(admin,"senha-antiga",arquivo);
        Build restaurada=dao.listarPorUsuario(admin.getId()).getFirst();assertEquals("Build",restaurada.getNome());assertTrue(restaurada.isFavorita());
        try(Connection c=db.abrir();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM jogo WHERE id=1")) {
            r.next();assertEquals("Ação",r.getString("genero"));assertEquals("Texto preservado",r.getString("descricao"));assertEquals("https://example.org/capa.webp",r.getString("imagem_url"));
        }
        assertEquals(1,db.contar("benchmark_fps"));
        assertNotNull(new AutenticacaoService(new UsuarioDAO(db::abrir),new service.PasswordService()).autenticar("admin","senha-antiga"));
    }
    @Test void falhaDeIntegridadeReverteTodaARestauracao() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);BuildDAO dao=new BuildDAO(db::abrir);dao.inserir(db.build(admin.getId()));
        DatabaseBackupService service=new DatabaseBackupService(db::abrir);Path arquivo=pasta.resolve("backup.json");service.exportar(admin,"senha-antiga",arquivo);
        ObjectMapper m=new ObjectMapper();ObjectNode json=(ObjectNode)m.readTree(arquivo.toFile());
        ((com.fasterxml.jackson.databind.node.ArrayNode)json.path("tabelas").path("build").get(0)).set(2,m.getNodeFactory().numberNode(99999));
        m.writeValue(arquivo.toFile(),json);
        Build atual=dao.listarPorUsuario(admin.getId()).getFirst();atual.setNome("Trabalho posterior");dao.atualizarDoUsuario(atual,admin.getId());
        assertThrows(SQLException.class,()->service.restaurar(admin,"senha-antiga",arquivo));
        assertEquals("Trabalho posterior",dao.listarPorUsuario(admin.getId()).getFirst().getNome());assertEquals(1,db.contar("usuario"));
    }
    @Test void exigeSenhaCorretaEPerfilAtualDoBanco() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);DatabaseBackupService service=new DatabaseBackupService(db::abrir);Path arquivo=pasta.resolve("backup.json");
        assertThrows(SecurityException.class,()->service.exportar(admin,"errada",arquivo));assertFalse(Files.exists(arquivo));
        db.executar("UPDATE usuario SET perfil='USUARIO'");
        assertThrows(SecurityException.class,()->service.exportar(admin,"senha-antiga",arquivo));
    }
    @Test void rejeitaFormatoInvalidoAntesDeApagarDados() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);Path arquivo=pasta.resolve("backup.json");
        Files.writeString(arquivo,"{\"versao\":999,\"tabelas\":{}}");
        assertThrows(java.io.IOException.class,()->new DatabaseBackupService(db::abrir).restaurar(admin,"senha-antiga",arquivo));assertEquals(1,db.contar("usuario"));
    }
    @Test void rejeitaHashMalformadoQueImpediriaNovoLogin() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);Path arquivo=pasta.resolve("backup.json");DatabaseBackupService service=new DatabaseBackupService(db::abrir);
        service.exportar(admin,"senha-antiga",arquivo);ObjectMapper m=new ObjectMapper();ObjectNode json=(ObjectNode)m.readTree(arquivo.toFile());
        ((com.fasterxml.jackson.databind.node.ArrayNode)json.path("tabelas").path("usuario").get(0)).set(3,m.getNodeFactory().textNode("pbkdf2_sha256$invalido"));m.writeValue(arquivo.toFile(),json);
        assertThrows(java.io.IOException.class,()->service.restaurar(admin,"senha-antiga",arquivo));assertEquals(1,db.contar("usuario"));
    }
    @Test void naoAceitaBackupSemContaAdministradora() throws Exception {
        BancoTeste db=new BancoTeste();Usuario admin=admin(db);Path arquivo=pasta.resolve("backup.json");DatabaseBackupService service=new DatabaseBackupService(db::abrir);
        service.exportar(admin,"senha-antiga",arquivo);ObjectMapper m=new ObjectMapper();ObjectNode json=(ObjectNode)m.readTree(arquivo.toFile());
        ((com.fasterxml.jackson.databind.node.ArrayNode)json.path("tabelas").path("usuario").get(0)).set(4,m.getNodeFactory().textNode("USUARIO"));m.writeValue(arquivo.toFile(),json);
        assertThrows(java.io.IOException.class,()->service.restaurar(admin,"senha-antiga",arquivo));assertTrue(new UsuarioDAO(db::abrir).buscarPorId(admin.getId()).isAdmin());
    }
}
