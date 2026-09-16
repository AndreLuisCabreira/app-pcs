import model.BenchmarkFPS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BenchmarkFPSDAO {

    public void inserir(BenchmarkFPS benchmark) {
        String sql = """
                INSERT INTO benchmark_fps
                    (jogo_id, processador_id, placa_video_id, resolucao, qualidade,
                     fps_medio, fps_1_low, fonte, observacoes)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, benchmark.getJogoId());
            statement.setInt(2, benchmark.getProcessadorId());
            statement.setInt(3, benchmark.getPlacaVideoId());
            statement.setString(4, benchmark.getResolucao());
            statement.setString(5, benchmark.getQualidade());
            statement.setInt(6, benchmark.getFpsMedio());
            if (benchmark.getFpsUmPorCento() == null) {
                statement.setNull(7, Types.INTEGER);
            } else {
                statement.setInt(7, benchmark.getFpsUmPorCento());
            }
            statement.setString(8, benchmark.getFonte());
            statement.setString(9, benchmark.getObservacoes());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível salvar o benchmark de FPS.", e);
        }
    }

    public List<BenchmarkFPS> listar() {
        String sql = """
                SELECT b.id, b.jogo_id, j.nome AS jogo_nome,
                       b.processador_id, p.nome AS processador_nome,
                       p.desempenho AS processador_desempenho,
                       b.placa_video_id, pv.nome AS placa_video_nome,
                       b.resolucao, b.qualidade, b.fps_medio, b.fps_1_low,
                       b.fonte, b.observacoes
                FROM benchmark_fps b
                JOIN jogo j ON j.id = b.jogo_id
                JOIN processador p ON p.id = b.processador_id
                JOIN placa_video pv ON pv.id = b.placa_video_id
                ORDER BY j.nome, pv.nome, p.nome, b.resolucao, b.qualidade
                """;
        List<BenchmarkFPS> benchmarks = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                benchmarks.add(mapear(result));
            }
            return benchmarks;
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível listar os benchmarks de FPS.", e);
        }
    }

    private BenchmarkFPS mapear(ResultSet result) throws SQLException {
        BenchmarkFPS benchmark = new BenchmarkFPS();
        benchmark.setId(result.getInt("id"));
        benchmark.setJogoId(result.getInt("jogo_id"));
        benchmark.setJogoNome(result.getString("jogo_nome"));
        benchmark.setProcessadorId(result.getInt("processador_id"));
        benchmark.setProcessadorNome(result.getString("processador_nome"));
        benchmark.setProcessadorDesempenho(result.getDouble("processador_desempenho"));
        benchmark.setPlacaVideoId(result.getInt("placa_video_id"));
        benchmark.setPlacaVideoNome(result.getString("placa_video_nome"));
        benchmark.setResolucao(result.getString("resolucao"));
        benchmark.setQualidade(result.getString("qualidade"));
        benchmark.setFpsMedio(result.getInt("fps_medio"));
        benchmark.setFpsUmPorCento((Integer) result.getObject("fps_1_low"));
        benchmark.setFonte(result.getString("fonte"));
        benchmark.setObservacoes(result.getString("observacoes"));
        return benchmark;
    }
}
