import model.PerfilUsuario;
import model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutorizacaoServiceTest {

    private final AutorizacaoService service = new AutorizacaoService();

    @Test
    void permiteAdministradorGerenciarComponentes() {
        Usuario admin = new Usuario(1, "Admin", "admin", null, PerfilUsuario.ADMIN);

        assertDoesNotThrow(() -> service.exigirAdmin(admin));
    }

    @Test
    void bloqueiaUsuarioComum() {
        Usuario usuario = new Usuario(2, "Pessoa", "pessoa", null, PerfilUsuario.USUARIO);

        SecurityException erro = assertThrows(
                SecurityException.class,
                () -> service.exigirAdmin(usuario)
        );

        assertEquals("Somente administradores podem adicionar componentes.", erro.getMessage());
    }

    @Test
    void bloqueiaSessaoAusente() {
        assertThrows(SecurityException.class, () -> service.exigirAdmin(null));
    }
}
