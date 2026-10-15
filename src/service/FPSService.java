package service;

import model.BenchmarkFPS;
import model.Build;
import model.Jogo;
import model.ResultadoFPS;

import java.util.List;

public class FPSService {

    public ResultadoFPS analisar(
            Build build,
            Jogo jogo,
            String resolucao,
            String qualidade,
            List<BenchmarkFPS> benchmarks
    ) {
        if (build == null || build.getProcessador() == null || build.getPlacaVideo() == null || jogo == null) {
            return ResultadoFPS.semDados("A build precisa ter processador e placa de vídeo.");
        }
        if (resolucao == null || qualidade == null || benchmarks == null) {
            return ResultadoFPS.semDados("Informe resolução, qualidade e benchmarks.");
        }

        List<BenchmarkFPS> mesmoCenario = benchmarks.stream()
                .filter(item -> item.getJogoId() == jogo.getId())
                .filter(item -> item.getResolucao().equalsIgnoreCase(resolucao))
                .filter(item -> item.getQualidade().equalsIgnoreCase(qualidade))
                .toList();

        List<BenchmarkFPS> exatos = mesmoCenario.stream()
                .filter(item -> item.getProcessadorId() == build.getProcessador().getId())
                .filter(item -> item.getPlacaVideoId() == build.getPlacaVideo().getId())
                .toList();
        if (!exatos.isEmpty()) {
            return resumir(
                    exatos,
                    "MEDIÇÃO EXATA",
                    "Média de benchmarks com o mesmo processador e a mesma placa de vídeo."
            );
        }

        List<BenchmarkFPS> mesmaGpu = mesmoCenario.stream()
                .filter(item -> item.getPlacaVideoId() == build.getPlacaVideo().getId())
                .toList();
        if (mesmaGpu.isEmpty()) {
            return ResultadoFPS.semDados(
                    "Ainda não há benchmark deste jogo com a placa de vídeo da build nessa configuração."
            );
        }

        double desempenhoAlvo = build.getProcessador().getDesempenho();
        BenchmarkFPS cpuMaisProxima = mesmaGpu.stream()
                .min((a, b) -> Double.compare(
                        Math.abs(a.getProcessadorDesempenho() - desempenhoAlvo),
                        Math.abs(b.getProcessadorDesempenho() - desempenhoAlvo)
                ))
                .orElseThrow();
        List<BenchmarkFPS> referencias = mesmaGpu.stream()
                .filter(item -> item.getProcessadorId() == cpuMaisProxima.getProcessadorId())
                .toList();

        return resumir(
                referencias,
                "REFERÊNCIA PRÓXIMA",
                "Mesma placa de vídeo; processador de referência: " + cpuMaisProxima.getProcessadorNome() + "."
        );
    }

    private ResultadoFPS resumir(List<BenchmarkFPS> itens, String tipo, String explicacao) {
        int fpsMedio = (int) Math.round(itens.stream()
                .mapToInt(BenchmarkFPS::getFpsMedio)
                .average()
                .orElse(0));
        List<Integer> lows = itens.stream()
                .map(BenchmarkFPS::getFpsUmPorCento)
                .filter(valor -> valor != null)
                .toList();
        Integer fpsUmPorCento = lows.isEmpty()
                ? null
                : (int) Math.round(lows.stream().mapToInt(Integer::intValue).average().orElse(0));

        String fontes = String.join(", ", itens.stream()
                .map(BenchmarkFPS::getFonte)
                .filter(fonte -> fonte != null && !fonte.isBlank())
                .distinct()
                .toList());

        return new ResultadoFPS(fpsMedio, fpsUmPorCento, itens.size(), tipo, explicacao, fontes);
    }
}
