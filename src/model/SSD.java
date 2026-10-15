package model;

public class SSD extends Componente {

    private int capacidade;
    private int leitura;
    private int escrita;
    private String tipo;

    public SSD() {}

    public SSD(int id, String nome, double preco,
               int capacidade, int leitura, int escrita, String tipo) {

        super(id, nome, preco);
        this.capacidade = capacidade;
        this.leitura = leitura;
        this.escrita = escrita;
        this.tipo = tipo;
    }

    // getters
    public int getCapacidade() { return capacidade; }

    public int getLeitura() { return leitura; }

    public int getEscrita() { return escrita; }

    public String getTipo() { return tipo; }

    //setters
    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public void setLeitura(int leitura) {
        this.leitura = leitura;
    }

    public void setEscrita(int escrita) {
        this.escrita = escrita;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {

        return id +
                " - " +
                nome +
                " | " +
                capacidade + "GB" +
                " | " +
                tipo +
                " | Leitura: " +
                leitura +
                "MB/s | Escrita: " +
                escrita +
                "MB/s | R$ " +
                preco;

    }
}