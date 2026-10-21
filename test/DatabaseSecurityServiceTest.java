import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSecurityServiceTest {
    @Test void protegeOnzeTabelasERevogaRolesPublicas() throws Exception {
        List<String> comandos=executar(List.of("anon","authenticated"));
        assertEquals(33,comandos.size());
        assertEquals(11,comandos.stream().filter(s->s.contains("ENABLE ROW LEVEL SECURITY")).count());
        assertTrue(comandos.contains("REVOKE ALL ON TABLE public.usuario FROM anon"));
        assertTrue(comandos.contains("REVOKE ALL ON TABLE public.benchmark_fps FROM authenticated"));
        String destino=System.getProperty("intratech.security.sql");
        if (destino!=null) Files.writeString(Path.of(destino),String.join(";\n",comandos)+";\n");
    }
    @Test void aceitaPostgresqlSemRolesEspecificasDoSupabase() throws Exception {
        assertEquals(11,executar(List.of()).size());
    }
    private List<String> executar(List<String> roles) throws Exception {
        List<String> comandos=new ArrayList<>();int[] pos={-1};
        ResultSet rs=(ResultSet)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{ResultSet.class},(o,m,a)->switch(m.getName()) {
            case "next" -> ++pos[0]<roles.size();case "getString" -> roles.get(pos[0]);default -> null;
        });
        PreparedStatement ps=(PreparedStatement)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{PreparedStatement.class},(o,m,a)->m.getName().equals("executeQuery")?rs:null);
        Statement st=(Statement)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Statement.class},(o,m,a)-> {
            if (m.getName().equals("executeUpdate")) {comandos.add((String)a[0]);return 0;}return null;
        });
        Connection c=(Connection)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{Connection.class},(o,m,a)->switch(m.getName()) {
            case "prepareStatement" -> ps;case "createStatement" -> st;default -> null;
        });
        new DatabaseSecurityService().aplicar(c);return comandos;
    }
}
