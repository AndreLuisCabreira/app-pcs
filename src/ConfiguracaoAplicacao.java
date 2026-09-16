import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ConfiguracaoAplicacao {

    private static final String PROPRIEDADE_ARQUIVO_CONFIGURACAO = "intratech.config";
    private static final Map<String, String> ARQUIVO_LOCAL = carregarArquivoLocal();

    private ConfiguracaoAplicacao() {
    }

    public static String obter(String nome, String padrao) {
        String ambiente = System.getenv(nome);
        if (ambiente != null && !ambiente.isBlank()) {
            return ambiente.trim();
        }
        String local = ARQUIVO_LOCAL.get(nome);
        return local == null || local.isBlank() ? padrao : local;
    }

    private static Map<String, String> carregarArquivoLocal() {
        Map<String, String> valores = new HashMap<>();
        Path arquivo = localizarArquivoLocal();
        if (!Files.isRegularFile(arquivo)) {
            return valores;
        }

        try {
            for (String linha : Files.readAllLines(arquivo)) {
                String limpa = linha.trim();
                if (limpa.isEmpty() || limpa.startsWith("#") || !limpa.contains("=")) {
                    continue;
                }
                int separador = limpa.indexOf('=');
                String nome = limpa.substring(0, separador).trim();
                String valor = limpa.substring(separador + 1).trim();
                if (valor.length() >= 2 && valor.startsWith("\"") && valor.endsWith("\"")) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                valores.put(nome, valor);
            }
        } catch (IOException ignored) {
            // A ausência do arquivo local nunca deve impedir a inicialização.
        }
        return valores;
    }

    private static Path localizarArquivoLocal() {
        String caminhoConfigurado = System.getProperty(PROPRIEDADE_ARQUIVO_CONFIGURACAO);
        if (caminhoConfigurado != null && !caminhoConfigurado.isBlank()) {
            return Path.of(caminhoConfigurado.trim()).toAbsolutePath().normalize();
        }
        return Path.of(".env").toAbsolutePath().normalize();
    }
}
