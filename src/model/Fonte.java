package model;

public class Fonte extends Componente {

    private int potencia;
    private String certificacao;

    public Fonte() {}

    public Fonte(int id, String nome, double preco,
                 int potencia, String certificacao) {

        super(id, nome, preco);
        this.potencia = potencia;
        this.certificacao = certificacao;
    }

    // getters
    public int getPotencia() {
        return potencia;
    }

    public String getCertificacao() {
        return certificacao;
    }

    //setters
    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }

    public void setCertificacao(String certificacao) {
        this.certificacao = certificacao;
    }

    @Override
    public String toString() {

        return id +
                " - " +
                nome +
                " | " +
                potencia + "W" +
                " | " +
                certificacao +
                " | R$ " +
                preco;

    }
}