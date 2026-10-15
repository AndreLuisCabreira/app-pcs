import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String URL = normalizarUrl(ConfiguracaoAplicacao.obter(
            "DB_URL",
            "jdbc:mysql://localhost:3306/pcbuilder?useSSL=false&serverTimezone=UTC"
    ));

    private static final String USER = ConfiguracaoAplicacao.obter("DB_USER", "pcbuilder");

    private static final String PASSWORD = ConfiguracaoAplicacao.obter("DB_PASSWORD", "");

    private static final boolean ALLOW_EMPTY_PASSWORD = Boolean.parseBoolean(
            ConfiguracaoAplicacao.obter("DB_ALLOW_EMPTY_PASSWORD", "false")
    );

    public static Connection getConexao(){
        if (PASSWORD.isBlank() && !ALLOW_EMPTY_PASSWORD) {
            throw new IllegalStateException(
                    "Defina DB_PASSWORD ou habilite DB_ALLOW_EMPTY_PASSWORD apenas no ambiente local."
            );
        }

        try {
            return DataSourceHolder.INSTANCE.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Não foi possível conectar ao banco de dados.", e);
        }
    }

    public static void fechar() {
        if (DataSourceHolder.INICIALIZADO) {
            DataSourceHolder.INSTANCE.close();
        }
    }

    public static boolean isPostgreSQL() {
        return URL.startsWith("jdbc:postgresql:");
    }

    public static String nomeBanco() {
        return isPostgreSQL() ? "Supabase" : "MariaDB";
    }

    private static String normalizarUrl(String url) {
        String valor = url == null ? "" : url.trim();
        if (valor.startsWith("postgresql://")) {
            valor = "jdbc:" + valor;
        } else if (valor.startsWith("postgres://")) {
            valor = "jdbc:postgresql://" + valor.substring("postgres://".length());
        }
        if (valor.startsWith("jdbc:postgresql:") && !valor.toLowerCase().contains("sslmode=")) {
            valor += valor.contains("?") ? "&sslmode=require" : "?sslmode=require";
        }
        return valor;
    }

    private static HikariDataSource criarDataSource() {
        HikariConfig configuracao = new HikariConfig();
        configuracao.setPoolName("IntraTechPool");
        configuracao.setJdbcUrl(URL);
        configuracao.setUsername(USER);
        configuracao.setPassword(PASSWORD);
        configuracao.setDriverClassName(isPostgreSQL()
                ? "org.postgresql.Driver"
                : "com.mysql.cj.jdbc.Driver");
        configuracao.setMaximumPoolSize(numeroConfigurado("DB_POOL_SIZE", 4));
        configuracao.setMinimumIdle(1);
        configuracao.setConnectionTimeout(numeroConfigurado("DB_CONNECTION_TIMEOUT_MS", 10_000));
        configuracao.setValidationTimeout(3_000);
        configuracao.setIdleTimeout(120_000);
        configuracao.setKeepaliveTime(120_000);
        configuracao.setMaxLifetime(600_000);
        configuracao.setInitializationFailTimeout(-1);
        if (isPostgreSQL()) {
            configuracao.addDataSourceProperty("tcpKeepAlive", "true");
        }
        return new HikariDataSource(configuracao);
    }

    private static int numeroConfigurado(String nome, int padrao) {
        String valor = ConfiguracaoAplicacao.obter(nome, String.valueOf(padrao));
        try {
            int numero = Integer.parseInt(valor);
            return numero > 0 ? numero : padrao;
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    private static final class DataSourceHolder {
        private static final HikariDataSource INSTANCE = criarDataSource();
        private static final boolean INICIALIZADO = true;
    }

}
