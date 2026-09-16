import model.Fonte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FonteDAO {

    public void inserir(Fonte f) {
        String sql = "INSERT INTO fonte (nome, potencia, certificacao, preco) VALUES (?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, f.getNome());
            ps.setInt(2, f.getPotencia());
            ps.setString(3, f.getCertificacao());
            ps.setDouble(4, f.getPreco());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível cadastrar a fonte.", e);
        }
    }

    public void atualizar(Fonte f) {
        String sql = "UPDATE fonte SET nome = ?, potencia = ?, certificacao = ?, preco = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getNome());
            ps.setInt(2, f.getPotencia());
            ps.setString(3, f.getCertificacao());
            ps.setDouble(4, f.getPreco());
            ps.setInt(5, f.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Fonte não encontrada para atualização.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível atualizar a fonte.", e);
        }
    }

    public Fonte buscarPorId(int id) {
        String sql = "SELECT * FROM fonte WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Fonte(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("potencia"),
                        rs.getString("certificacao")
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar a fonte.", e);
        }

        return null;
    }

    public List<Fonte> listar() {

        List<Fonte> lista = new ArrayList<>();

        String sql = "SELECT * FROM fonte";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Fonte fonte = new Fonte(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getInt("potencia"),
                    rs.getString("certificacao")
                );

                lista.add(fonte);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar as fontes.", e);
        }

        return lista;
    }
}
