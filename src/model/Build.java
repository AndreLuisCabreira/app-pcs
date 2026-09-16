package model;

public class Build {

    private int id;
    private String nome;
    private int usuarioId;

    private Processador processador;
    private PlacaMae placaMae;
    private PlacaVideo placaVideo;
    private Memoria memoria;
    private SSD ssd;
    private Fonte fonte;

    private boolean favorita;

    public Build() {
    }

    public Build(String nome, int usuarioId) {
        this.nome = nome;
        this.usuarioId = usuarioId;
    }

    public Build(int id, String nome, int usuarioId,
                 Processador processador,
                 PlacaMae placaMae,
                 Memoria memoria,
                 PlacaVideo placaVideo,
                 SSD ssd,
                 Fonte fonte,
                 boolean favorita) {

        this.id = id;
        this.nome = nome;
        this.usuarioId = usuarioId;

        this.processador = processador;
        this.placaMae = placaMae;
        this.memoria = memoria;
        this.placaVideo = placaVideo;
        this.ssd = ssd;
        this.fonte = fonte;

        this.favorita = favorita;
    }

    // GETTERS E SETTERS

    public Processador getProcessador() {
        return processador;
    }

    public void setProcessador(Processador processador) {
        this.processador = processador;
    }

    public PlacaMae getPlacaMae() {
        return placaMae;
    }

    public void setPlacaMae(PlacaMae placaMae) {
        this.placaMae = placaMae;
    }

    public PlacaVideo getPlacaVideo() {
        return placaVideo;
    }

    public void setPlacaVideo(PlacaVideo placaVideo) {
        this.placaVideo = placaVideo;
    }

    public Memoria getMemoria() {
        return memoria;
    }

    public void setMemoria(Memoria memoria) {
        this.memoria = memoria;
    }

    public SSD getSsd() {
        return ssd;
    }

    public void setSsd(SSD ssd) {
        this.ssd = ssd;
    }

    public Fonte getFonte() {
        return fonte;
    }

    public void setFonte(Fonte fonte) {
        this.fonte = fonte;
    }

    public double getPrecoTotal() {
        double total = 0;
        total += processador == null ? 0 : processador.getPreco();
        total += placaMae == null ? 0 : placaMae.getPreco();
        total += placaVideo == null ? 0 : placaVideo.getPreco();
        total += memoria == null ? 0 : memoria.getPreco();
        total += ssd == null ? 0 : ssd.getPreco();
        total += fonte == null ? 0 : fonte.getPreco();
        return total;
    }

    // RESTO

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isFavorita() {
        return favorita;
    }

    public void setFavorita(boolean favorita) {
        this.favorita = favorita;
    }

    @Override
    public String toString() {
        return "Build{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", favorita=" + favorita +
                '}';
    }
}
