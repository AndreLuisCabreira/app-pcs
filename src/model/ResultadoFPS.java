package model;

public record ResultadoFPS(
        Integer fpsMedio,
        Integer fpsUmPorCento,
        int amostras,
        String tipoReferencia,
        String explicacao,
        String fontes
) {

    public boolean disponivel() {
        return fpsMedio != null;
    }

    public static ResultadoFPS semDados(String explicacao) {
        return new ResultadoFPS(null, null, 0, "SEM DADOS", explicacao, "");
    }
}
