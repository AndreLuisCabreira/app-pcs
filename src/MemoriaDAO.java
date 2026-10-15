import model.Memoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemoriaDAO {

    public void inserir(Memoria m) {
        String sql = "INSERT INTO memoria (nome, capacidade, frequencia, tipo, preco) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, m.getNome());
            ps.setInt(2, m.getCapacidade());
            ps.setInt(3, m.getFrequencia());
            ps.setString(4, m.getTipo());
            ps.setDouble(5, m.getPreco());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível cadastrar a memória.", e);
        }
    }

    public void atualizar(Memoria m) {
        String sql = "UPDATE memoria SET nome = ?, capacidade = ?, frequencia = ?, tipo = ?, preco = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getNome());
            ps.setInt(2, m.getCapacidade());
            ps.setInt(3, m.getFrequencia());
            ps.setString(4, m.getTipo());
            ps.setDouble(5, m.getPreco());
            ps.setInt(6, m.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Memória não encontrada para atualização.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível atualizar a memória.", e);
        }
    }

    public Memoria buscarPorId(int id) {
        String sql = "SELECT * FROM memoria WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Memoria(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("capacidade"),
                        rs.getInt("frequencia"),
                        rs.getString("tipo")
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar a memória.", e);
        }

        return null;
    }

    public List<Memoria> listar() {

        List<Memoria> lista = new ArrayList<>();

        String sql = "SELECT * FROM memoria";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Memoria memoria = new Memoria(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("capacidade"),
                        rs.getInt("frequencia"),
                        rs.getString("tipo")
                );

                lista.add(memoria);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar as memórias.", e);
        }

        return lista;
    }
}
