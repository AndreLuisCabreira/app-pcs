import model.Usuario;
import model.PerfilUsuario;
import service.PasswordService;

import java.util.Locale;

public class AutenticacaoService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordService passwordService;

    public AutenticacaoService() {
        this(new UsuarioDAO(), new PasswordService());
    }

    AutenticacaoService(UsuarioDAO usuarioDAO, PasswordService passwordService) {
        this.usuarioDAO = usuarioDAO;
        this.passwordService = passwordService;
    }

    public Usuario autenticar(String login, String senha) {
        String loginNormalizado = normalizarLogin(login);
        Usuario usuario = usuarioDAO.buscarPorLogin(loginNormalizado);

        if (usuario == null || !passwordService.verificar(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("Login ou senha inválidos.");
        }

        usuario.setSenha(null);
        return usuario;
    }

    public Usuario cadastrar(String nome, String login, String senha, String confirmacao) {
        String nomeNormalizado = exigirNome(nome);
        String loginNormalizado = normalizarLogin(login);
        validarNovaSenha(senha, confirmacao);

        if (usuarioDAO.buscarPorLogin(loginNormalizado) != null) {
            throw new IllegalArgumentException("Este login já está em uso.");
        }

        PerfilUsuario perfil = usuarioDAO.contarUsuarios() == 0
                ? PerfilUsuario.ADMIN
                : PerfilUsuario.USUARIO;
        Usuario usuario = new Usuario(
                nomeNormalizado,
                loginNormalizado,
                passwordService.criarHash(senha),
                perfil
        );
        usuarioDAO.inserir(usuario);
        usuario.setSenha(null);
        return usuario;
    }

    private String exigirNome(String nome) {
        String valor = nome == null ? "" : nome.trim();
        if (valor.length() < 2) {
            throw new IllegalArgumentException("Informe um nome com pelo menos 2 caracteres.");
        }
        return valor;
    }

    private String normalizarLogin(String login) {
        String valor = login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
        if (valor.length() < 3) {
            throw new IllegalArgumentException("O login deve ter pelo menos 3 caracteres.");
        }
        if (!valor.matches("[\\p{L}\\p{N}._-]+")) {
            throw new IllegalArgumentException("Use apenas letras, números, ponto, hífen ou sublinhado no login.");
        }
        return valor;
    }

    private void validarNovaSenha(String senha, String confirmacao) {
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            throw new IllegalArgumentException("As senhas informadas não coincidem.");
        }
    }
}
