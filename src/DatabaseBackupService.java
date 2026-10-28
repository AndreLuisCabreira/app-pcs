import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import model.Usuario;
import service.PasswordService;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/** Data-only backup of the application's allowlisted tables. Never executes SQL from a file. */
public final class DatabaseBackupService {
    private final Supplier<Connection> conexoes;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final long MAX_BYTES = 64L * 1024 * 1024;

    public DatabaseBackupService() { this(ConnectionFactory::getConexao); }
    DatabaseBackupService(Supplier<Connection> conexoes) { this.conexoes = conexoes; }

    public record Backup(int versao, String banco, String criadoEm, Map<String,List<List<JsonNode>>> tabelas) { }

    private void autenticarAdmin(Usuario sessao, String senha) {
        if (sessao == null) throw new SecurityException("Entre com uma conta administradora.");
        Usuario atual = new UsuarioDAO(conexoes).buscarPorId(sessao.getId());
        if (atual == null || !atual.isAdmin() || !new PasswordService().verificar(senha, atual.getSenha())) {
            throw new SecurityException("Conta administradora ou senha atual inválida.");
        }
    }

    public void exportar(Usuario sessao, String senha, Path destino) throws SQLException, IOException {
        autenticarAdmin(sessao, senha);
        Map<String,List<List<JsonNode>>> dados = new LinkedHashMap<>();
        String banco;
        try (Connection c = conexoes.get()) {
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                banco = c.getMetaData().getDatabaseProductName();
                validarTransacoes(c);
                for (String tabela : DatabaseTransfer.tabelas()) {
                    String[] colunas = DatabaseTransfer.colunasDaTabela(tabela);
                    List<List<JsonNode>> linhas = new ArrayList<>();
                    try (Statement s=c.createStatement(); ResultSet rs=s.executeQuery(
                            "SELECT " + String.join(",",colunas) + " FROM " + tabela + " ORDER BY id")) {
                        while (rs.next()) {
                            List<JsonNode> valores=new ArrayList<>();
                            for (int i=1;i<=colunas.length;i++) {
                                Object valor=rs.getObject(i);
                                if (valor==null) valores.add(NullNode.instance);
                                else if (valor instanceof Number || valor instanceof Boolean) valores.add(mapper.valueToTree(valor));
                                else valores.add(mapper.getNodeFactory().textNode(rs.getString(i)));
                            }
                            linhas.add(valores);
                        }
                    }
                    dados.put(tabela,linhas);
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback();throw e; }
        }
        byte[] bytes=mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(new Backup(1,banco,Instant.now().toString(),dados));
        if (bytes.length>MAX_BYTES) throw new IOException("Backup maior que o limite de 64 MiB.");
        Path absoluto=destino.toAbsolutePath();
        Path temp=Files.createTempFile(absoluto.getParent(),"intratech-backup-",".tmp");
        try {
            Files.write(temp,bytes);
            try { Files.move(temp,absoluto,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp,absoluto,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }

    public void restaurar(Usuario sessao, String senha, Path arquivo) throws SQLException, IOException {
        autenticarAdmin(sessao, senha);
        if (Files.size(arquivo)>MAX_BYTES) throw new IOException("Arquivo maior que o limite de 64 MiB.");
        Backup backup=mapper.readValue(Files.readAllBytes(arquivo),Backup.class);
        validar(backup);
        try (Connection c=conexoes.get()) {
            validarTransacoes(c);
            c.setAutoCommit(false);
            try {
                List<String> ordem=new ArrayList<>(DatabaseTransfer.tabelas());
                Collections.reverse(ordem);
                for (String tabela:ordem) try(Statement s=c.createStatement()) {s.executeUpdate("DELETE FROM " + tabela);}
                for (String tabela:DatabaseTransfer.tabelas()) inserir(c,tabela,backup.tabelas().get(tabela));
                if (c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("postgresql")) {
                    for(String tabela:DatabaseTransfer.tabelas()) {
                        long proximo;
                        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COALESCE(MAX(id),0)+1 FROM " + tabela)) {r.next();proximo=r.getLong(1);}
                        // Identity restart is transactional, unlike setval().
                        try(Statement s=c.createStatement()) {s.executeUpdate("ALTER TABLE " + tabela + " ALTER COLUMN id RESTART WITH " + proximo);}
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback();throw e; }
        }
    }

    private void validar(Backup backup) throws IOException {
        if(backup==null || backup.versao()!=1 || backup.tabelas()==null
                || !backup.tabelas().keySet().equals(new HashSet<>(DatabaseTransfer.tabelas()))) {
            throw new IOException("Formato ou versão de backup inválidos.");
        }
        for(String tabela:DatabaseTransfer.tabelas()) {
            List<List<JsonNode>> linhas=backup.tabelas().get(tabela);
            if(linhas==null) throw new IOException("Tabela ausente no backup.");
            for(List<JsonNode> linha:linhas) {
                if(linha==null || linha.size()!=DatabaseTransfer.colunasDaTabela(tabela).length
                        || linha.stream().anyMatch(v->v==null || v.isContainerNode())) throw new IOException("Linha inválida no backup.");
            }
        }
        // A restoration must leave at least one usable administrator account.
        boolean admin=backup.tabelas().get("usuario").stream().anyMatch(r ->
                "ADMIN".equals(r.get(4).asText()) && hashValido(r.get(3).asText())
                        && r.get(2).asText().length() >= 3 && r.get(2).asText().length() <= 80
                        && r.get(2).asText().matches("[\\p{L}\\p{N}._-]+"));
        if(!admin) throw new IOException("O backup precisa conter uma conta administradora com senha protegida.");
    }

    private boolean hashValido(String hash) {
        try {
            String[] partes=hash.split("\\$");
            if(partes.length!=4 || !partes[0].equals("pbkdf2_sha256")) return false;
            int iteracoes=Integer.parseInt(partes[1]);
            return iteracoes>=10000 && iteracoes<=1000000
                    && Base64.getDecoder().decode(partes[2]).length==16
                    && Base64.getDecoder().decode(partes[3]).length==32;
        } catch(IllegalArgumentException e) { return false; }
    }

    private void inserir(Connection c,String tabela,List<List<JsonNode>> linhas) throws SQLException {
        String[] colunas=DatabaseTransfer.colunasDaTabela(tabela);
        int[] tipos=new int[colunas.length];
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT " + String.join(",",colunas) + " FROM " + tabela + " WHERE 1=0")) {
            for(int i=0;i<tipos.length;i++) tipos[i]=r.getMetaData().getColumnType(i+1);
        }
        String sql="INSERT INTO " + tabela + " (" + String.join(",",colunas) + ") VALUES ("
                + String.join(",",Collections.nCopies(colunas.length,"?")) + ")";
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            int lote=0;
            for(List<JsonNode> linha:linhas) {
                for(int i=0;i<colunas.length;i++) {
                    JsonNode n=linha.get(i);
                    if(n.isNull()) ps.setNull(i+1,tipos[i]);
                    else if(tabela.equals("build") && colunas[i].equals("favorita")) ps.setBoolean(i+1,n.isBoolean()?n.booleanValue():n.asInt()!=0);
                    else ps.setObject(i+1,n.asText(),tipos[i]);
                }
                ps.addBatch();
                if(++lote%500==0) ps.executeBatch();
            }
            if(lote%500!=0) ps.executeBatch();
        }
    }

    private void validarTransacoes(Connection c) throws SQLException {
        if(!c.getMetaData().supportsTransactions()) throw new SQLException("O banco não oferece transações.");
        String banco=c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        if(banco.contains("mysql") || banco.contains("mariadb")) {
            try(PreparedStatement ps=c.prepareStatement("SELECT ENGINE FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
                for(String tabela:DatabaseTransfer.tabelas()) {
                    ps.setString(1,tabela);
                    try(ResultSet r=ps.executeQuery()) {
                        if(!r.next() || !"InnoDB".equalsIgnoreCase(r.getString(1))) throw new SQLException("Backup/restauração exige todas as tabelas em InnoDB.");
                    }
                }
            }
        }
    }
}
