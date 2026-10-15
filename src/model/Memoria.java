package model;

public class Memoria extends Componente {

    private int capacidade;
    private int frequencia;
    private String tipo;

    public Memoria() {}

    public Memoria(int id, String nome, double preco,
                   int capacidade, int frequencia, String tipo) {

        super(id, nome, preco);
        this.capacidade = capacidade;
        this.frequencia = frequencia;
        this.tipo = tipo;
    }

    // getters
    public int getCapacidade() {
        return capacidade;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public String getTipo() {
        return tipo;
    }

    //setters
    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
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
                frequencia + "MHz" +
                " | " +
                tipo +
                " | R$ " +
                preco;

    }
}