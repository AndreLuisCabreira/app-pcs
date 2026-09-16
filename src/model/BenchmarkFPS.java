package model;

public class BenchmarkFPS {

    private int id;
    private int jogoId;
    private String jogoNome;
    private int processadorId;
    private String processadorNome;
    private double processadorDesempenho;
    private int placaVideoId;
    private String placaVideoNome;
    private String resolucao;
    private String qualidade;
    private int fpsMedio;
    private Integer fpsUmPorCento;
    private String fonte;
    private String observacoes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJogoId() {
        return jogoId;
    }

    public void setJogoId(int jogoId) {
        this.jogoId = jogoId;
    }

    public String getJogoNome() {
        return jogoNome;
    }

    public void setJogoNome(String jogoNome) {
        this.jogoNome = jogoNome;
    }

    public int getProcessadorId() {
        return processadorId;
    }

    public void setProcessadorId(int processadorId) {
        this.processadorId = processadorId;
    }

    public String getProcessadorNome() {
        return processadorNome;
    }

    public void setProcessadorNome(String processadorNome) {
        this.processadorNome = processadorNome;
    }

    public double getProcessadorDesempenho() {
        return processadorDesempenho;
    }

    public void setProcessadorDesempenho(double processadorDesempenho) {
        this.processadorDesempenho = processadorDesempenho;
    }

    public int getPlacaVideoId() {
        return placaVideoId;
    }

    public void setPlacaVideoId(int placaVideoId) {
        this.placaVideoId = placaVideoId;
    }

    public String getPlacaVideoNome() {
        return placaVideoNome;
    }

    public void setPlacaVideoNome(String placaVideoNome) {
        this.placaVideoNome = placaVideoNome;
    }

    public String getResolucao() {
        return resolucao;
    }

    public void setResolucao(String resolucao) {
        this.resolucao = resolucao;
    }

    public String getQualidade() {
        return qualidade;
    }

    public void setQualidade(String qualidade) {
        this.qualidade = qualidade;
    }

    public int getFpsMedio() {
        return fpsMedio;
    }

    public void setFpsMedio(int fpsMedio) {
        this.fpsMedio = fpsMedio;
    }

    public Integer getFpsUmPorCento() {
        return fpsUmPorCento;
    }

    public void setFpsUmPorCento(Integer fpsUmPorCento) {
        this.fpsUmPorCento = fpsUmPorCento;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    @Override
    public String toString() {
        return jogoNome + " · " + placaVideoNome + " · " + processadorNome
                + " · " + resolucao + " / " + qualidade + " · " + fpsMedio + " FPS";
    }
}
