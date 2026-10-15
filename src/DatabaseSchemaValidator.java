import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class DatabaseSchemaValidator {

    private static final Set<String> TABELAS_OBRIGATORIAS = Set.of(
            "usuario", "processador", "placa_mae", "placa_video", "memoria",
            "ssd", "fonte", "jogo", "build", "benchmark_fps", "componente_importado"
    );

    public void verificar() {
        try (Connection connection = ConnectionFactory.getConexao()) {
            Set<String> ausentes = new TreeSet<>(TABELAS_OBRIGATORIAS);
            ausentes.removeAll(listarTabelas(connection));
            if (!ausentes.isEmpty()) {
                throw bancoDesatualizado("tabelas ausentes: " + String.join(", ", ausentes));
            }
            if (!colunaExiste(connection, "usuario", "perfil")) {
                throw bancoDesatualizado("coluna ausente: usuario.perfil");
            }
            for (String coluna : Set.of("genero", "descricao", "imagem_url")) {
                if (!colunaExiste(connection, "jogo", coluna)) {
                    throw bancoDesatualizado("coluna ausente: jogo." + coluna);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível verificar a estrutura do banco de dados.", e);
        }
    }

    private Set<String> listarTabelas(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        Set<String> tabelas = new HashSet<>();
        try (ResultSet resultado = metadata.getTables(
                connection.getCatalog(),
                schema(connection),
                "%",
                new String[]{"TABLE"}
        )) {
            while (resultado.next()) {
                tabelas.add(resultado.getString("TABLE_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return tabelas;
    }

    private boolean colunaExiste(Connection connection, String tabela, String coluna) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet colunas = metadata.getColumns(
                connection.getCatalog(),
                schema(connection),
                tabela,
                coluna
        )) {
            return colunas.next();
        }
    }

    private String schema(Connection connection) {
        return ConnectionFactory.isPostgreSQL() ? "public" : null;
    }

    private IllegalStateException bancoDesatualizado(String detalhe) {
        return new IllegalStateException(
                "O banco de dados não está pronto (" + detalhe + "). "
                        + "Execute scripts\\migrate-database.ps1 em uma cópia atualizada do projeto "
                        + "e abra o aplicativo novamente."
        );
    }
}
