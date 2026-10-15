import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

public final class DatabaseConnectionCheck {

    private static final Set<String> TABELAS_ESPERADAS = Set.of(
            "usuario", "processador", "placa_mae", "placa_video", "memoria",
            "ssd", "fonte", "jogo", "build", "benchmark_fps", "componente_importado"
    );

    private DatabaseConnectionCheck() {
    }

    public static void main(String[] args) throws Exception {
        new DatabaseSchemaValidator().verificar();
        try (Connection connection = ConnectionFactory.getConexao()) {
            String produto = connection.getMetaData().getDatabaseProductName();
            String versao = connection.getMetaData().getDatabaseProductVersion();

            Set<String> encontradas = new java.util.HashSet<>();
            String sql = """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = ?
                    """;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ConnectionFactory.isPostgreSQL() ? "public" : connection.getCatalog());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        encontradas.add(result.getString("table_name"));
                    }
                }
            }

            Set<String> ausentes = new java.util.TreeSet<>(TABELAS_ESPERADAS);
            ausentes.removeAll(encontradas);

            System.out.println("Conexão: OK");
            System.out.println("Banco: " + produto);
            System.out.println("Versão: " + versao);
            System.out.println("Tabelas do PC Builder: " + (TABELAS_ESPERADAS.size() - ausentes.size())
                    + "/" + TABELAS_ESPERADAS.size());

            if (!ausentes.isEmpty()) {
                throw new IllegalStateException("Tabelas ausentes: " + String.join(", ", ausentes));
            }

            System.out.println("Estrutura: OK");
            System.out.println("Registros:");
            String prefixo = ConnectionFactory.isPostgreSQL() ? "public." : "";
            TABELAS_ESPERADAS.stream().sorted().forEach(tabela -> {
                try (Statement statement = connection.createStatement();
                     ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + prefixo + tabela)) {
                    result.next();
                    System.out.println("  " + tabela + ": " + result.getLong(1));
                } catch (Exception e) {
                    throw new IllegalStateException("Não foi possível contar a tabela " + tabela + ".", e);
                }
            });
        }
    }
}
