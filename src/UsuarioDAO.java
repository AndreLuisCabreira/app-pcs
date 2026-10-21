import model.Usuario;
import model.PerfilUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private final java.util.function.Supplier<Connection> conexoes;

    public UsuarioDAO() { this(ConnectionFactory::getConexao); }

    UsuarioDAO(java.util.function.Supplier<Connection> conexoes) {
        this.conexoes = java.util.Objects.requireNonNull(conexoes);
    }


    public void inserir(Usuario u) {
        String sql = "INSERT INTO usuario (nome, login, senha, perfil) VALUES (?, ?, ?, ?)";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNome());
            ps.setString(2, u.getLogin());
            ps.setString(3, u.getSenha());
            ps.setString(4, u.getPerfil().name());

            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    u.setId(chaves.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível cadastrar o usuário.", e);
        }
    }

    public Usuario buscarPorLogin(String login) {
        String sql = "SELECT id, nome, login, senha, perfil FROM usuario WHERE login = ?";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível consultar o usuário.", e);
        }
        return null;
    }

    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar o usuário.", e);
        }

        return null;
    }

    public List<Usuario> listar() {

        List<Usuario> lista = new ArrayList<>();

        String sql = "SELECT * FROM usuario";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                lista.add(mapearUsuario(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar os usuários.", e);
        }

        return lista;
    }

    public void alterarSenha(int usuarioId, String hashAnterior, String hashNovo) {
        String sql = "UPDATE usuario SET senha = ? WHERE id = ? AND senha = ?";
        try (Connection con = conexoes.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hashNovo);
            ps.setInt(2, usuarioId);
            ps.setString(3, hashAnterior);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("A senha mudou ou a conta não está disponível. Entre novamente.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível alterar a senha.", e);
        }
    }

    public boolean existeAdmin() {
        String sql = "SELECT EXISTS(SELECT 1 FROM usuario WHERE perfil = 'ADMIN')";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível consultar o perfil administrativo.", e);
        }
    }

    public int contarUsuarios() {
        String sql = "SELECT COUNT(*) FROM usuario";

        try (Connection con = conexoes.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível contar os usuários.", e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("login"),
                rs.getString("senha"),
                PerfilUsuario.doBanco(rs.getString("perfil"))
        );
    }
}
