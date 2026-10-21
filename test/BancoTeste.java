import java.sql.*;
import java.nio.file.*;
import model.*;

final class BancoTeste {
    private final String url = "jdbc:h2:mem:" + java.util.UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    BancoTeste() throws Exception {
        String sql = Files.readString(Path.of("database/schema.sql"));
        sql = sql.substring(sql.indexOf("CREATE TABLE"));
        try (Connection c = abrir(); Statement s = c.createStatement()) {
            for (String comando : sql.split(";")) if (!comando.isBlank()) s.execute(comando);
        }
    }
    Connection abrir() {
        try { return DriverManager.getConnection(url, "sa", ""); }
        catch (SQLException e) { throw new IllegalStateException(e); }
    }
    void executar(String sql) throws SQLException {
        try (Connection c=abrir(); Statement s=c.createStatement()) { s.execute(sql); }
    }
    int contar(String tabela) throws SQLException {
        try (Connection c=abrir(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM " + tabela)) { r.next(); return r.getInt(1); }
    }
    Build build(int usuario) throws SQLException {
        if (contar("processador") == 0) {
            executar("INSERT INTO processador VALUES (1,'CPU', 'AM4',65,700,'AMD',6,12,80)");
            executar("INSERT INTO placa_mae VALUES (1,'Placa','Marca','AM4','DDR4',40,400)");
            executar("INSERT INTO placa_video VALUES (1,'GPU','Marca',8,150,1000,80)");
            executar("INSERT INTO memoria VALUES (1,'RAM',16,3200,'DDR4',200)");
            executar("INSERT INTO ssd VALUES (1,'SSD',1000,3000,2500,'NVMe',300)");
            executar("INSERT INTO fonte VALUES (1,'Fonte',550,'Bronze',250)");
        }
        return new Build(0,"Build",usuario,
            new Processador(1,"CPU",700,"AMD","AM4",6,12,65,80),
            new PlacaMae(1,"Placa",400,"Marca","AM4","DDR4",40),
            new Memoria(1,"RAM",200,16,3200,"DDR4"),
            new PlacaVideo(1,"GPU",1000,"Marca",8,150,80),
            new SSD(1,"SSD",300,1000,3000,2500,"NVMe"),
            new Fonte(1,"Fonte",250,550,"Bronze"),false);
    }
    Usuario usuario(String login) {
        Usuario u = new Usuario(login,login,new service.PasswordService().criarHash("senha-antiga"));
        new UsuarioDAO(this::abrir).inserir(u); return u;
    }
}
