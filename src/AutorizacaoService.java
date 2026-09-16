import model.Usuario;

public class AutorizacaoService {

    public void exigirAdmin(Usuario usuario) {
        if (usuario == null || !usuario.isAdmin()) {
            throw new SecurityException("Somente administradores podem adicionar componentes.");
        }
    }
}
