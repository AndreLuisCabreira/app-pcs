import model.Build;
import model.Fonte;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.SSD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BuildDAO {

    private static final String SELECT_COMPLETO = """
            SELECT
                b.id AS build_id, b.nome AS build_nome, b.usuario_id, b.favorita,
                p.id AS p_id, p.nome AS p_nome, p.preco AS p_preco,
                p.fabricante AS p_fabricante, p.socket AS p_socket,
                p.nucleos AS p_nucleos, p.threads AS p_threads,
                p.consumo AS p_consumo, p.desempenho AS p_desempenho,
                pm.id AS pm_id, pm.nome AS pm_nome, pm.preco AS pm_preco,
                pm.fabricante AS pm_fabricante, pm.socket AS pm_socket,
                pm.tipo_memoria AS pm_tipo_memoria, pm.consumo AS pm_consumo,
                pv.id AS pv_id, pv.nome AS pv_nome, pv.preco AS pv_preco,
                pv.fabricante AS pv_fabricante, pv.memoria AS pv_memoria,
                pv.consumo AS pv_consumo, pv.desempenho AS pv_desempenho,
                m.id AS m_id, m.nome AS m_nome, m.preco AS m_preco,
                m.capacidade AS m_capacidade, m.frequencia AS m_frequencia, m.tipo AS m_tipo,
                s.id AS s_id, s.nome AS s_nome, s.preco AS s_preco,
                s.capacidade AS s_capacidade, s.leitura AS s_leitura,
                s.escrita AS s_escrita, s.tipo AS s_tipo,
                f.id AS f_id, f.nome AS f_nome, f.preco AS f_preco,
                f.potencia AS f_potencia, f.certificacao AS f_certificacao
            FROM build b
            JOIN processador p ON p.id = b.processador_id
            JOIN placa_mae pm ON pm.id = b.placa_mae_id
            JOIN placa_video pv ON pv.id = b.placa_video_id
            JOIN memoria m ON m.id = b.memoria_id
            JOIN ssd s ON s.id = b.ssd_id
            JOIN fonte f ON f.id = b.fonte_id
            """;

    public void inserir(Build b) {
        String sql = """
                INSERT INTO build
                (nome, usuario_id, processador_id, placa_mae_id, placa_video_id,
                 memoria_id, ssd_id, fonte_id, favorita)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencherParametros(ps, b);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível cadastrar a build.", e);
        }
    }

    public Build buscarPorId(int id) {
        String sql = SELECT_COMPLETO + " WHERE b.id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearBuild(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar a build.", e);
        }
    }

    public Build buscarPorIdDoUsuario(int id, int usuarioId) {
        String sql = SELECT_COMPLETO + " WHERE b.id = ? AND b.usuario_id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearBuild(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível buscar a build do usuário.", e);
        }
    }

    public List<Build> listar() {
        List<Build> builds = new ArrayList<>();
        String sql = SELECT_COMPLETO + " ORDER BY b.id";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                builds.add(mapearBuild(rs));
            }
            return builds;
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar as builds.", e);
        }
    }

    public List<Build> listarPorUsuario(int usuarioId) {
        List<Build> builds = new ArrayList<>();
        String sql = SELECT_COMPLETO + " WHERE b.usuario_id = ? ORDER BY b.id DESC";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    builds.add(mapearBuild(rs));
                }
            }
            return builds;
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar as builds do usuário.", e);
        }
    }

    public void atualizar(Build b) {
        String sql = """
                UPDATE build
                SET nome = ?, usuario_id = ?, processador_id = ?, placa_mae_id = ?,
                    placa_video_id = ?, memoria_id = ?, ssd_id = ?, fonte_id = ?, favorita = ?
                WHERE id = ?
                """;

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencherParametros(ps, b);
            ps.setInt(10, b.getId());
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Build não encontrada para atualização.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível atualizar a build.", e);
        }
    }

    public void atualizarDoUsuario(Build b, int usuarioId) {
        String sql = """
                UPDATE build
                SET nome = ?, processador_id = ?, placa_mae_id = ?, placa_video_id = ?,
                    memoria_id = ?, ssd_id = ?, fonte_id = ?, favorita = ?
                WHERE id = ? AND usuario_id = ?
                """;

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getNome());
            ps.setInt(2, b.getProcessador().getId());
            ps.setInt(3, b.getPlacaMae().getId());
            ps.setInt(4, b.getPlacaVideo().getId());
            ps.setInt(5, b.getMemoria().getId());
            ps.setInt(6, b.getSsd().getId());
            ps.setInt(7, b.getFonte().getId());
            ps.setBoolean(8, b.isFavorita());
            ps.setInt(9, b.getId());
            ps.setInt(10, usuarioId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Build não encontrada para este usuário.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível atualizar a build do usuário.", e);
        }
    }

    public void excluir(int id) {
        String sql = "DELETE FROM build WHERE id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Build não encontrada para exclusão.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível excluir a build.", e);
        }
    }

    public void excluirDoUsuario(int id, int usuarioId) {
        String sql = "DELETE FROM build WHERE id = ? AND usuario_id = ?";

        try (Connection con = ConnectionFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, usuarioId);
            if (ps.executeUpdate() == 0) {
                throw new DataAccessException("Build não encontrada para este usuário.", null);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível excluir a build do usuário.", e);
        }
    }

    private void preencherParametros(PreparedStatement ps, Build b) throws SQLException {
        ps.setString(1, b.getNome());
        ps.setInt(2, b.getUsuarioId());
        ps.setInt(3, b.getProcessador().getId());
        ps.setInt(4, b.getPlacaMae().getId());
        ps.setInt(5, b.getPlacaVideo().getId());
        ps.setInt(6, b.getMemoria().getId());
        ps.setInt(7, b.getSsd().getId());
        ps.setInt(8, b.getFonte().getId());
        ps.setBoolean(9, b.isFavorita());
    }

    private Build mapearBuild(ResultSet rs) throws SQLException {
        Processador processador = new Processador(
                rs.getInt("p_id"), rs.getString("p_nome"), rs.getDouble("p_preco"),
                rs.getString("p_fabricante"), rs.getString("p_socket"),
                rs.getInt("p_nucleos"), rs.getInt("p_threads"),
                rs.getInt("p_consumo"), rs.getDouble("p_desempenho")
        );
        PlacaMae placaMae = new PlacaMae(
                rs.getInt("pm_id"), rs.getString("pm_nome"), rs.getDouble("pm_preco"),
                rs.getString("pm_fabricante"), rs.getString("pm_socket"),
                rs.getString("pm_tipo_memoria"), rs.getInt("pm_consumo")
        );
        PlacaVideo placaVideo = new PlacaVideo(
                rs.getInt("pv_id"), rs.getString("pv_nome"), rs.getDouble("pv_preco"),
                rs.getString("pv_fabricante"), rs.getInt("pv_memoria"),
                rs.getInt("pv_consumo"), rs.getInt("pv_desempenho")
        );
        Memoria memoria = new Memoria(
                rs.getInt("m_id"), rs.getString("m_nome"), rs.getDouble("m_preco"),
                rs.getInt("m_capacidade"), rs.getInt("m_frequencia"), rs.getString("m_tipo")
        );
        SSD ssd = new SSD(
                rs.getInt("s_id"), rs.getString("s_nome"), rs.getDouble("s_preco"),
                rs.getInt("s_capacidade"), rs.getInt("s_leitura"),
                rs.getInt("s_escrita"), rs.getString("s_tipo")
        );
        Fonte fonte = new Fonte(
                rs.getInt("f_id"), rs.getString("f_nome"), rs.getDouble("f_preco"),
                rs.getInt("f_potencia"), rs.getString("f_certificacao")
        );

        return new Build(
                rs.getInt("build_id"), rs.getString("build_nome"), rs.getInt("usuario_id"),
                processador, placaMae, memoria, placaVideo, ssd, fonte, rs.getBoolean("favorita")
        );
    }
}
