import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class DatabaseLatencyCheck {

    private static final int AMOSTRAS = 6;

    private DatabaseLatencyCheck() {
    }

    public static void main(String[] args) {
        long total = 0;
        for (int indice = 1; indice <= AMOSTRAS; indice++) {
            long inicio = System.nanoTime();
            try (Connection connection = ConnectionFactory.getConexao();
                 PreparedStatement statement = connection.prepareStatement("SELECT 1");
                 ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getInt(1) != 1) {
                    throw new IllegalStateException("O banco não retornou o valor esperado.");
                }
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao medir a conexão " + indice + ".", e);
            }
            long duracao = (System.nanoTime() - inicio) / 1_000_000;
            total += duracao;
            System.out.println("Amostra " + indice + ": " + duracao + " ms");
        }
        System.out.println("Média: " + (total / AMOSTRAS) + " ms");
    }
}
