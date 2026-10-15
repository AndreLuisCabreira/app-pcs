import model.PlacaMae;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlacaMaeDAO {

    public void inserir(PlacaMae p) {
        String sql = "INSERT INTO placa_mae (nome, fabricante, socket, tipo_memoria, consumo, preco) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNome());
            ps.setString(2, p.getFabricante());
            ps.setString(3, p.getSocket());
            ps.setString(4, p.getTipoMemoria());
            ps.setInt(5, p.getConsumo());
            ps.setDouble(6, p.getPreco());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível cadastrar a placa-mãe.", e);
        }
    }

    public void atualizar(PlacaMae p) {
        String sql = "UPDATE placa_mae SET nome = ?, fabricante = ?, socket = ?, tipo_memoria = ?, consumo = ?, preco = ? WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getFabricante());
            ps.setString(3, p.getSocket());
            ps.setString(4, p.getTipoMemoria());
            ps.setInt(5, p.getConsumo());
            ps.setDouble(6, p.getPreco());
            ps.setInt(7, p.getId());

            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Placa-mãe não encontrada para atualização.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível atualizar a placa-mãe.", e);
        }
    }

    public PlacaMae buscarPorId(int id) {
        String sql = "SELECT * FROM placa_mae WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PlacaMae(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getString("fabricante"),
                        rs.getString("socket"),
                        rs.getString("tipo_memoria"),
                        rs.getInt("consumo")
                );
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar a placa-mãe.", e);
        }

        return null;
    }

    public List<PlacaMae> listar() {

        List<PlacaMae> lista = new ArrayList<>();

        String sql = "SELECT * FROM placa_mae";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                PlacaMae placaMae = new PlacaMae(

                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getString("fabricante"),
                    rs.getString("socket"),
                    rs.getString("tipo_memoria"),
                    rs.getInt("consumo")
                );

                lista.add(placaMae);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar as placas-mãe.", e);
        }

        return lista;
    }
}
