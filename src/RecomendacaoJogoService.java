import model.BenchmarkFPS;
import model.Build;
import model.Fonte;
import model.Jogo;
import model.Memoria;
import model.PlacaMae;
import model.PlacaVideo;
import model.Processador;
import model.RecomendacaoJogo;
import model.SSD;
import service.ConsumoService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecomendacaoJogoService {

    public static final String RESOLUCAO_ALVO = "1920x1080";
    public static final int FPS_ALVO = 60;

    public List<RecomendacaoJogo> listar() {
        return recomendar(
                new JogoDAO().listar(),
                new BenchmarkFPSDAO().listar(),
                new ProcessadorDAO().listar(),
                new PlacaVideoDAO().listar(),
                new PlacaMaeDAO().listar(),
                new MemoriaDAO().listar(),
                new SSDDAO().listar(),
                new FonteDAO().listar()
        );
    }

    public List<RecomendacaoJogo> recomendar(
            List<Jogo> jogos,
            List<BenchmarkFPS> benchmarks,
            List<Processador> processadores,
            List<PlacaVideo> placasVideo,
            List<PlacaMae> placasMae,
            List<Memoria> memorias,
            List<SSD> ssds,
            List<Fonte> fontes
    ) {
        Map<Integer, Processador> cpuPorId = processadores.stream()
                .collect(Collectors.toMap(Processador::getId, Function.identity()));
        Map<Integer, PlacaVideo> gpuPorId = placasVideo.stream()
                .collect(Collectors.toMap(PlacaVideo::getId, Function.identity()));
        List<RecomendacaoJogo> resultado = new ArrayList<>();

        for (Jogo jogo : jogos) {
            RecomendacaoJogo melhor = benchmarks.stream()
                    .filter(item -> item.getJogoId() == jogo.getId())
                    .filter(item -> RESOLUCAO_ALVO.equalsIgnoreCase(item.getResolucao()))
                    .filter(item -> item.getFpsMedio() >= FPS_ALVO)
                    .map(item -> criarRecomendacao(
                            jogo, item, cpuPorId, gpuPorId, placasMae, memorias, ssds, fontes
                    ))
                    .filter(RecomendacaoJogo::disponivel)
                    .min(Comparator
                            .comparingDouble((RecomendacaoJogo item) -> item.build().getPrecoTotal())
                            .thenComparingInt(item -> -nivelQualidade(item.benchmark().getQualidade()))
                            .thenComparingInt(item -> item.benchmark().getFpsMedio()))
                    .orElse(new RecomendacaoJogo(jogo, null, null));
            resultado.add(melhor);
        }

        resultado.sort(Comparator.comparing(item -> item.jogo().getNome(), String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    private RecomendacaoJogo criarRecomendacao(
            Jogo jogo,
            BenchmarkFPS benchmark,
            Map<Integer, Processador> cpuPorId,
            Map<Integer, PlacaVideo> gpuPorId,
            List<PlacaMae> placasMae,
            List<Memoria> memorias,
            List<SSD> ssds,
            List<Fonte> fontes
    ) {
        Processador cpu = cpuPorId.get(benchmark.getProcessadorId());
        PlacaVideo gpu = gpuPorId.get(benchmark.getPlacaVideoId());
        if (cpu == null || gpu == null) {
            return new RecomendacaoJogo(jogo, null, null);
        }

        SSD ssd = maisBarato(ssds.stream().filter(item -> item.getCapacidade() >= 500).toList());
        if (ssd == null) {
            ssd = maisBarato(ssds);
        }
        if (ssd == null) {
            return new RecomendacaoJogo(jogo, null, null);
        }

        Build melhorBuild = null;
        for (PlacaMae placaMae : placasMae) {
            if (!igual(cpu.getSocket(), placaMae.getSocket())) {
                continue;
            }
            Memoria memoria = maisBarata(memoriaCompativel(memorias, placaMae, true));
            if (memoria == null) {
                memoria = maisBarata(memoriaCompativel(memorias, placaMae, false));
            }
            if (memoria == null) {
                continue;
            }

            Build build = new Build("Recomendação para " + jogo.getNome(), 0);
            build.setProcessador(cpu);
            build.setPlacaVideo(gpu);
            build.setPlacaMae(placaMae);
            build.setMemoria(memoria);
            build.setSsd(ssd);

            int potenciaMinima = new ConsumoService().consumoRecomendado(build);
            Fonte fonte = fontes.stream()
                    .filter(item -> item.getPotencia() >= potenciaMinima)
                    .min(Comparator.comparingDouble(Fonte::getPreco))
                    .orElse(null);
            if (fonte == null) {
                continue;
            }
            build.setFonte(fonte);

            if (melhorBuild == null || build.getPrecoTotal() < melhorBuild.getPrecoTotal()) {
                melhorBuild = build;
            }
        }

        return new RecomendacaoJogo(jogo, melhorBuild == null ? null : benchmark, melhorBuild);
    }

    private List<Memoria> memoriaCompativel(List<Memoria> memorias, PlacaMae placaMae, boolean minimo16Gb) {
        return memorias.stream()
                .filter(item -> igual(item.getTipo(), placaMae.getTipoMemoria()))
                .filter(item -> !minimo16Gb || item.getCapacidade() >= 16)
                .toList();
    }

    private <T extends model.Componente> T maisBarato(List<T> itens) {
        return itens.stream().min(Comparator.comparingDouble(model.Componente::getPreco)).orElse(null);
    }

    private Memoria maisBarata(List<Memoria> itens) {
        return maisBarato(itens);
    }

    private boolean igual(String primeiro, String segundo) {
        return primeiro != null && segundo != null && primeiro.equalsIgnoreCase(segundo);
    }

    private int nivelQualidade(String qualidade) {
        if (qualidade == null) return 0;
        return switch (qualidade.toLowerCase()) {
            case "ultra" -> 4;
            case "alto" -> 3;
            case "médio", "medio" -> 2;
            case "baixo" -> 1;
            default -> 0;
        };
    }
}
