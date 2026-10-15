package model;

public class PlacaVideo extends Componente {

    private String fabricante;
    private int memoria;
    private int consumo;
    private int desempenho;

    public PlacaVideo() {}

    public PlacaVideo(int id, String nome, double preco,
                      String fabricante, int memoria,
                      int consumo, int desempenho) {

        super(id, nome, preco);
        this.fabricante = fabricante;
        this.memoria = memoria;
        this.consumo = consumo;
        this.desempenho = desempenho;
    }

    // getters
    public String getFabricante() {
        return fabricante;
    }

    public int getMemoria() {
        return memoria;
    }

    public int getConsumo() {
        return consumo;
    }

    public int getDesempenho() {
        return desempenho;
    }

    //setters
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public void setMemoria(int memoria) {
        this.memoria = memoria;
    }

    public void setConsumo(int consumo) {
        this.consumo = consumo;
    }

    public void setDesempenho(int desempenho) {
        this.desempenho = desempenho;
    }

    @Override
    public String toString() {

        return id +
                " - " +
                nome +
                " | " +
                fabricante +
                " | " +
                memoria + "GB" +
                " | R$ " +
                preco;

    }
}