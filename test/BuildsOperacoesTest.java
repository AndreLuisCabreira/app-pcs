import model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class BuildsOperacoesTest {
    @Test void insercaoConfirmadaNaoViraFalhaDeGravacaoQuandoAListaFalha() throws Exception {
        BancoTeste db = new BancoTeste(); Usuario usuario = db.usuario("montagem-insert");
        Build build = db.build(usuario.getId());
        BuildsOperacoes.Resultado resultado = new BuildsOperacoes(daoComFalhaNaLista(db), usuario.getId()).salvar(build);
        assertFalse(resultado.listaAtualizada()); assertNotNull(resultado.erroAtualizacao());
        assertEquals(1, db.contar("build"));
        Build persistida = new BuildDAO(db::abrir).listarPorUsuario(usuario.getId()).getFirst();
        assertEquals(build.getNome(), persistida.getNome());
        assertEquals(usuario.getId(), persistida.getUsuarioId());
    }

    @Test void edicaoConfirmadaMantemIdMesmoSeAListaFalhar() throws Exception {
        BancoTeste db = new BancoTeste(); Usuario usuario = db.usuario("montagem-edit");
        BuildDAO dao = new BuildDAO(db::abrir); dao.inserir(db.build(usuario.getId()));
        Build build = dao.listarPorUsuario(usuario.getId()).getFirst(); int id = build.getId();
        build.setNome("Nome atualizado");
        var resultado = new BuildsOperacoes(daoComFalhaNaLista(db), usuario.getId()).salvar(build);
        assertFalse(resultado.listaAtualizada()); assertEquals(1, db.contar("build"));
        assertEquals("Nome atualizado", dao.buscarPorIdDoUsuario(id, usuario.getId()).getNome());
    }

    @Test void exclusaoConfirmadaNaoViraFalhaDeGravacaoQuandoAListaFalha() throws Exception {
        BancoTeste db = new BancoTeste(); Usuario usuario = db.usuario("montagem-delete");
        BuildDAO dao = new BuildDAO(db::abrir); dao.inserir(db.build(usuario.getId()));
        Build build = dao.listarPorUsuario(usuario.getId()).getFirst();
        var resultado = new BuildsOperacoes(daoComFalhaNaLista(db), usuario.getId()).excluir(build);
        assertFalse(resultado.listaAtualizada()); assertEquals(0, db.contar("build"));
        assertNull(dao.buscarPorIdDoUsuario(build.getId(), usuario.getId()));
    }

    @Test void falhaNaGravacaoContinuaComoErroESemRecarregarALista() {
        AtomicBoolean consultou = new AtomicBoolean();
        DataAccessException falha = new DataAccessException("Falha simulada na gravação", null);
        BuildDAO dao = new BuildDAO() {
            @Override public void inserir(Build build) { throw falha; }
            @Override public List<Build> listarPorUsuario(int id) { consultou.set(true); return List.of(); }
        };
        assertSame(falha, assertThrows(DataAccessException.class,
                () -> new BuildsOperacoes(dao, 7).salvar(new Build("PC", 7))));
        assertFalse(consultou.get());
    }

    @Test void propriedadeInvalidaEhRejeitadaAntesDeEscrever() throws Exception {
        BancoTeste db = new BancoTeste(); Usuario a = db.usuario("dona-a"), b = db.usuario("dona-b");
        Build build = db.build(a.getId()); BuildsOperacoes operacoes = new BuildsOperacoes(new BuildDAO(db::abrir), b.getId());
        assertThrows(SecurityException.class, () -> operacoes.salvar(build));
        assertThrows(SecurityException.class, () -> operacoes.excluir(build));
        assertEquals(0, db.contar("build"));
    }

    @Test void gravacaoNormalRetornaAListaDaContaSemExporBuildsAlheias() throws Exception {
        BancoTeste db = new BancoTeste(); Usuario a = db.usuario("lista-a"), b = db.usuario("lista-b");
        BuildDAO dao = new BuildDAO(db::abrir); dao.inserir(db.build(b.getId()));
        var resultado = new BuildsOperacoes(dao, a.getId()).salvar(db.build(a.getId()));
        assertTrue(resultado.listaAtualizada()); assertNull(resultado.erroAtualizacao());
        assertEquals(1, resultado.builds().size());
        assertEquals(a.getId(), resultado.builds().getFirst().getUsuarioId());
        assertEquals(2, db.contar("build"));
    }

    private BuildDAO daoComFalhaNaLista(BancoTeste db) {
        return new BuildDAO(db::abrir) {
            @Override public List<Build> listarPorUsuario(int usuarioId) {
                throw new DataAccessException("Falha simulada no recarregamento", null);
            }
        };
    }
}
