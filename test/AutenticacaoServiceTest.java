import model.Usuario;
import model.PerfilUsuario;
import org.junit.jupiter.api.Test;
import service.PasswordService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutenticacaoServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void autenticaComCredenciaisValidasSemExporOHashNaSessao() {
        Usuario existente = new Usuario(7, "Sylvie", "sylvie", passwordService.criarHash("senha-segura"));
        FakeUsuarioDAO dao = new FakeUsuarioDAO(existente);
        AutenticacaoService service = new AutenticacaoService(dao, passwordService);

        Usuario autenticado = service.autenticar("  SYLVIE ", "senha-segura");

        assertEquals(7, autenticado.getId());
        assertEquals("sylvie", dao.ultimoLoginConsultado);
        assertNull(autenticado.getSenha());
    }

    @Test
    void rejeitaSenhaIncorretaComMensagemGenerica() {
        Usuario existente = new Usuario(7, "Sylvie", "sylvie", passwordService.criarHash("senha-segura"));
        AutenticacaoService service = new AutenticacaoService(
                new FakeUsuarioDAO(existente), passwordService
        );

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.autenticar("sylvie", "senha-incorreta")
        );

        assertEquals("Login ou senha inválidos.", erro.getMessage());
    }

    @Test
    void cadastraComLoginNormalizadoESenhaProtegida() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO(null);
        AutenticacaoService service = new AutenticacaoService(dao, passwordService);

        Usuario cadastrado = service.cadastrar(
                "  Sylvie Silva  ", " Sylvie.PC ", "senha-segura", "senha-segura"
        );

        assertEquals(42, cadastrado.getId());
        assertEquals("Sylvie Silva", cadastrado.getNome());
        assertEquals("sylvie.pc", cadastrado.getLogin());
        assertEquals(PerfilUsuario.ADMIN, cadastrado.getPerfil());
        assertNull(cadastrado.getSenha());
        assertNotNull(dao.hashSalvo);
        assertFalse("senha-segura".equals(dao.hashSalvo));
        assertTrue(passwordService.verificar("senha-segura", dao.hashSalvo));
    }

    @Test
    void cadastraContaComumQuandoJaExisteAdministrador() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO(null, 1);
        AutenticacaoService service = new AutenticacaoService(dao, passwordService);

        Usuario cadastrado = service.cadastrar(
                "Novo usuário", "novo.usuario", "senha-segura", "senha-segura"
        );

        assertEquals(PerfilUsuario.USUARIO, cadastrado.getPerfil());
    }

    @Test
    void naoElevaNovaContaQuandoHaUsuariosMasNenhumAdmin() {
        FakeUsuarioDAO dao = new FakeUsuarioDAO(null, 2);
        AutenticacaoService service = new AutenticacaoService(dao, passwordService);

        Usuario cadastrado = service.cadastrar(
                "Conta comum", "conta.comum", "senha-segura", "senha-segura"
        );

        assertEquals(PerfilUsuario.USUARIO, cadastrado.getPerfil());
    }

    @Test
    void impedeCadastroComLoginExistente() {
        Usuario existente = new Usuario(3, "Outra pessoa", "existente", "hash");
        AutenticacaoService service = new AutenticacaoService(
                new FakeUsuarioDAO(existente), passwordService
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar("Nome", "existente", "senha-segura", "senha-segura")
        );
    }

    private static final class FakeUsuarioDAO extends UsuarioDAO {
        private final Usuario existente;
        private final int quantidadeUsuarios;
        private String ultimoLoginConsultado;
        private String hashSalvo;

        private FakeUsuarioDAO(Usuario existente) {
            this(existente, existente == null ? 0 : 1);
        }

        private FakeUsuarioDAO(Usuario existente, int quantidadeUsuarios) {
            this.existente = existente;
            this.quantidadeUsuarios = quantidadeUsuarios;
        }

        @Override
        public Usuario buscarPorLogin(String login) {
            ultimoLoginConsultado = login;
            return existente;
        }

        @Override
        public void inserir(Usuario usuario) {
            hashSalvo = usuario.getSenha();
            usuario.setId(42);
        }

        @Override
        public int contarUsuarios() {
            return quantidadeUsuarios;
        }
    }
}
