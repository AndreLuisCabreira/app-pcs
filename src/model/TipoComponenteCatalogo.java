package model;

public enum TipoComponenteCatalogo {
    PROCESSADOR("Processador", "cpu.json"),
    PLACA_VIDEO("Placa de vídeo", "video-card.json"),
    FONTE("Fonte", "power-supply.json"),
    MEMORIA("Memória RAM", "memory.json"),
    SSD("SSD", "internal-hard-drive.json");

    private final String nome;
    private final String arquivoDataset;

    TipoComponenteCatalogo(String nome, String arquivoDataset) {
        this.nome = nome;
        this.arquivoDataset = arquivoDataset;
    }

    public String getArquivoDataset() {
        return arquivoDataset;
    }

    @Override
    public String toString() {
        return nome;
    }
}
