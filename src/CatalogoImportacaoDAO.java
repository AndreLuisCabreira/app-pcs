import model.Componente;
import model.Fonte;
import model.Memoria;
import model.PlacaVideo;
import model.Processador;
import model.ProdutoCatalogo;
import model.SSD;
import model.TipoComponenteCatalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CatalogoImportacaoDAO {

    private static final String ORIGEM = "PC_PART_DATASET";

    public int importar(TipoComponenteCatalogo tipo, ProdutoCatalogo produto, Componente componente) {
        try (Connection connection = ConnectionFactory.getConexao()) {
            connection.setAutoCommit(false);
            try {
                if (jaImportado(connection, tipo, produto.getIdExterno())) {
                    throw new IllegalArgumentException("Este produto já foi importado para essa categoria.");
                }
                int componenteId = inserirComponente(connection, tipo, componente);
                registrarImportacao(connection, tipo, produto, componenteId);
                connection.commit();
                return componenteId;
            } catch (RuntimeException | SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível importar o componente.", e);
        }
    }

    private boolean jaImportado(Connection connection, TipoComponenteCatalogo tipo, String idExterno)
            throws SQLException {
        String sql = """
                SELECT 1 FROM componente_importado
                WHERE tipo = ? AND origem = ? AND id_externo = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipo.name());
            statement.setString(2, ORIGEM);
            statement.setString(3, idExterno);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private int inserirComponente(Connection connection, TipoComponenteCatalogo tipo, Componente componente)
            throws SQLException {
        return switch (tipo) {
            case PROCESSADOR -> inserirProcessador(connection, (Processador) componente);
            case PLACA_VIDEO -> inserirPlacaVideo(connection, (PlacaVideo) componente);
            case FONTE -> inserirFonte(connection, (Fonte) componente);
            case MEMORIA -> inserirMemoria(connection, (Memoria) componente);
            case SSD -> inserirSsd(connection, (SSD) componente);
        };
    }

    private int inserirProcessador(Connection connection, Processador item) throws SQLException {
        String sql = """
                INSERT INTO processador
                    (nome, socket, consumo, preco, fabricante, nucleos, threads, desempenho)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = preparado(connection, sql)) {
            statement.setString(1, item.getNome());
            statement.setString(2, item.getSocket());
            statement.setInt(3, item.getConsumo());
            statement.setDouble(4, item.getPreco());
            statement.setString(5, item.getFabricante());
            statement.setInt(6, item.getNucleos());
            statement.setInt(7, item.getThreads());
            statement.setDouble(8, item.getDesempenho());
            return executarInsert(statement);
        }
    }

    private int inserirPlacaVideo(Connection connection, PlacaVideo item) throws SQLException {
        String sql = """
                INSERT INTO placa_video
                    (nome, fabricante, memoria, consumo, preco, desempenho)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = preparado(connection, sql)) {
            statement.setString(1, item.getNome());
            statement.setString(2, item.getFabricante());
            statement.setInt(3, item.getMemoria());
            statement.setInt(4, item.getConsumo());
            statement.setDouble(5, item.getPreco());
            statement.setInt(6, item.getDesempenho());
            return executarInsert(statement);
        }
    }

    private int inserirFonte(Connection connection, Fonte item) throws SQLException {
        String sql = "INSERT INTO fonte (nome, potencia, certificacao, preco) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = preparado(connection, sql)) {
            statement.setString(1, item.getNome());
            statement.setInt(2, item.getPotencia());
            statement.setString(3, item.getCertificacao());
            statement.setDouble(4, item.getPreco());
            return executarInsert(statement);
        }
    }

    private int inserirMemoria(Connection connection, Memoria item) throws SQLException {
        String sql = """
                INSERT INTO memoria (nome, capacidade, frequencia, tipo, preco)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = preparado(connection, sql)) {
            statement.setString(1, item.getNome());
            statement.setInt(2, item.getCapacidade());
            statement.setInt(3, item.getFrequencia());
            statement.setString(4, item.getTipo());
            statement.setDouble(5, item.getPreco());
            return executarInsert(statement);
        }
    }

    private int inserirSsd(Connection connection, SSD item) throws SQLException {
        String sql = """
                INSERT INTO ssd (nome, capacidade, leitura, escrita, tipo, preco)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = preparado(connection, sql)) {
            statement.setString(1, item.getNome());
            statement.setInt(2, item.getCapacidade());
            statement.setInt(3, item.getLeitura());
            statement.setInt(4, item.getEscrita());
            statement.setString(5, item.getTipo());
            statement.setDouble(6, item.getPreco());
            return executarInsert(statement);
        }
    }

    private PreparedStatement preparado(Connection connection, String sql) throws SQLException {
        return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    private int executarInsert(PreparedStatement statement) throws SQLException {
        statement.executeUpdate();
        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("O banco não retornou o ID do componente importado.");
            }
            return keys.getInt(1);
        }
    }

    private void registrarImportacao(
            Connection connection,
            TipoComponenteCatalogo tipo,
            ProdutoCatalogo produto,
            int componenteId
    ) throws SQLException {
        String sql = """
                INSERT INTO componente_importado
                    (tipo, componente_id, origem, id_externo, nome_original)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipo.name());
            statement.setInt(2, componenteId);
            statement.setString(3, ORIGEM);
            statement.setString(4, produto.getIdExterno());
            statement.setString(5, produto.getNome());
            statement.executeUpdate();
        }
    }
}
