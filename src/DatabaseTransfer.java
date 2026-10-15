import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseTransfer {

    private static final Map<String, String[]> TABELAS = new LinkedHashMap<>();

    static {
        TABELAS.put("usuario", colunas("id,nome,login,senha,perfil"));
        TABELAS.put("processador", colunas("id,nome,socket,consumo,preco,fabricante,nucleos,threads,desempenho"));
        TABELAS.put("placa_mae", colunas("id,nome,fabricante,socket,tipo_memoria,consumo,preco"));
        TABELAS.put("placa_video", colunas("id,nome,fabricante,memoria,consumo,preco,desempenho"));
        TABELAS.put("memoria", colunas("id,nome,capacidade,frequencia,tipo,preco"));
        TABELAS.put("ssd", colunas("id,nome,capacidade,leitura,escrita,tipo,preco"));
        TABELAS.put("fonte", colunas("id,nome,potencia,certificacao,preco"));
        TABELAS.put("jogo", colunas("id,nome,exigencia_cpu,exigencia_gpu"));
        TABELAS.put("build", colunas("id,nome,usuario_id,processador_id,placa_mae_id,placa_video_id,memoria_id,ssd_id,fonte_id,favorita"));
        TABELAS.put("benchmark_fps", colunas("id,jogo_id,processador_id,placa_video_id,resolucao,qualidade,fps_medio,fps_1_low,fonte,observacoes,criado_em"));
        TABELAS.put("componente_importado", colunas("id,tipo,componente_id,origem,id_externo,nome_original,importado_em"));
    }

    private DatabaseTransfer() {
    }

    public static void main(String[] args) throws Exception {
        boolean executar = java.util.Arrays.asList(args).contains("--execute");
        boolean substituir = java.util.Arrays.asList(args).contains("--replace");

        String sourceUrl = ambiente("MIGRATION_SOURCE_URL",
                "jdbc:mysql://localhost:3306/pcbuilder?useSSL=false&serverTimezone=UTC");
        String sourceUser = ambiente("MIGRATION_SOURCE_USER", "root");
        String sourcePassword = System.getenv().getOrDefault("MIGRATION_SOURCE_PASSWORD", "");

        Class.forName("com.mysql.cj.jdbc.Driver");
        Class.forName("org.postgresql.Driver");

        try (Connection source = DriverManager.getConnection(sourceUrl, sourceUser, sourcePassword);
             Connection target = ConnectionFactory.getConexao()) {
            validarBancos(source, target);
            Map<String, Long> sourceCounts = contar(source, null);
            Map<String, Long> targetCounts = contar(target, "public");

            imprimir("MariaDB origem", sourceCounts);
            imprimir("Supabase destino", targetCounts);

            if (!executar) {
                System.out.println("Prévia concluída. Nenhum dado foi alterado.");
                return;
            }
            if (!substituir && targetCounts.values().stream().anyMatch(total -> total > 0)) {
                throw new IllegalStateException(
                        "O Supabase contém dados. Use --replace somente após conferir a prévia."
                );
            }

            transferir(source, target, sourceCounts);
        }
    }

    private static void transferir(
            Connection source,
            Connection target,
            Map<String, Long> sourceCounts
    ) throws SQLException {
        target.setAutoCommit(false);
        try {
            truncarDestino(target);
            for (Map.Entry<String, String[]> tabela : TABELAS.entrySet()) {
                copiarTabela(source, target, tabela.getKey(), tabela.getValue());
            }
            reajustarSequencias(target);

            Map<String, Long> targetCounts = contar(target, "public");
            if (!sourceCounts.equals(targetCounts)) {
                throw new SQLException(
                        "A validação das contagens falhou. Origem=" + sourceCounts + ", destino=" + targetCounts
                );
            }

            target.commit();
            imprimir("Supabase após migração", targetCounts);
            System.out.println("Migração concluída e validada com sucesso.");
        } catch (Exception e) {
            target.rollback();
            if (e instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw new SQLException("A migração falhou e foi revertida.", e);
        } finally {
            target.setAutoCommit(true);
        }
    }

    private static void truncarDestino(Connection target) throws SQLException {
        String nomes = TABELAS.keySet().stream()
                .map(nome -> "public." + nome)
                .collect(java.util.stream.Collectors.joining(", "));
        try (Statement statement = target.createStatement()) {
            statement.executeUpdate("TRUNCATE TABLE " + nomes + " RESTART IDENTITY CASCADE");
        }
    }

    private static void copiarTabela(
            Connection source,
            Connection target,
            String tabela,
            String[] colunas
    ) throws SQLException {
        String lista = String.join(",", colunas);
        String parametros = String.join(",", java.util.Collections.nCopies(colunas.length, "?"));
        String select = "SELECT " + lista + " FROM " + tabela + " ORDER BY id";
        String insert = "INSERT INTO public." + tabela + " (" + lista + ") VALUES (" + parametros + ")";

        try (PreparedStatement leitura = source.prepareStatement(select);
             ResultSet rows = leitura.executeQuery();
             PreparedStatement escrita = target.prepareStatement(insert)) {
            ResultSetMetaData metadata = rows.getMetaData();
            int lote = 0;
            while (rows.next()) {
                for (int indice = 1; indice <= colunas.length; indice++) {
                    Object valor = rows.getObject(indice);
                    if (valor == null) {
                        escrita.setNull(indice, tipoPostgres(metadata.getColumnType(indice)));
                    } else if (tabela.equals("build") && colunas[indice - 1].equals("favorita")) {
                        escrita.setBoolean(indice, rows.getBoolean(indice));
                    } else {
                        escrita.setObject(indice, valor);
                    }
                }
                escrita.addBatch();
                lote++;
                if (lote % 500 == 0) {
                    escrita.executeBatch();
                }
            }
            if (lote % 500 != 0) {
                escrita.executeBatch();
            }
            System.out.println("Copiada " + tabela + ": " + lote);
        }
    }

    private static int tipoPostgres(int sourceType) {
        return switch (sourceType) {
            case Types.TINYINT -> Types.BOOLEAN;
            default -> sourceType;
        };
    }

    private static void reajustarSequencias(Connection target) throws SQLException {
        String sql = "SELECT setval(pg_get_serial_sequence(?, 'id'), "
                + "COALESCE((SELECT MAX(id) FROM public.%s), 1), "
                + "EXISTS (SELECT 1 FROM public.%s))";
        for (String tabela : TABELAS.keySet()) {
            String query = sql.formatted(tabela, tabela);
            try (PreparedStatement statement = target.prepareStatement(query)) {
                statement.setString(1, "public." + tabela);
                statement.executeQuery();
            }
        }
    }

    private static Map<String, Long> contar(Connection connection, String schema) throws SQLException {
        Map<String, Long> counts = new LinkedHashMap<>();
        String prefixo = schema == null ? "" : schema + ".";
        for (String tabela : TABELAS.keySet()) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + prefixo + tabela)) {
                result.next();
                counts.put(tabela, result.getLong(1));
            }
        }
        return counts;
    }

    private static void validarBancos(Connection source, Connection target) throws SQLException {
        String sourceName = source.getMetaData().getDatabaseProductName().toLowerCase();
        String targetName = target.getMetaData().getDatabaseProductName().toLowerCase();
        if (!sourceName.contains("mysql") && !sourceName.contains("mariadb")) {
            throw new IllegalStateException("A origem não é MariaDB/MySQL.");
        }
        if (!targetName.contains("postgresql")) {
            throw new IllegalStateException("O destino configurado em .env não é PostgreSQL/Supabase.");
        }
    }

    private static void imprimir(String titulo, Map<String, Long> counts) {
        System.out.println(titulo + ":");
        counts.forEach((tabela, total) -> System.out.println("  " + tabela + ": " + total));
    }

    private static String ambiente(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    private static String[] colunas(String valor) {
        return valor.split(",");
    }
}
