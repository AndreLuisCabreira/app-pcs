package model;

public record RecomendacaoJogo(
        Jogo jogo,
        BenchmarkFPS benchmark,
        Build build
) {
    public boolean disponivel() {
        return benchmark != null && build != null;
    }
}
