import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Applies the initial JDBC schema restrictions to existing PostgreSQL installations. */
public final class DatabaseSecurityService {
    private static final List<String> TABELAS = List.of(
            "usuario", "processador", "placa_mae", "placa_video", "memoria", "ssd",
            "fonte", "jogo", "build", "benchmark_fps", "componente_importado");

    void aplicar(Connection connection) throws SQLException {
        List<String> roles = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT rolname FROM pg_roles WHERE rolname IN ('anon', 'authenticated')");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) roles.add(rs.getString(1));
        }
        for (String tabela : TABELAS) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE public." + tabela + " ENABLE ROW LEVEL SECURITY");
                for (String role : roles) {
                    if (!List.of("anon", "authenticated").contains(role)) throw new SQLException("Role inesperada.");
                    statement.executeUpdate("REVOKE ALL ON TABLE public." + tabela + " FROM " + role);
                }
            }
        }
    }
}
