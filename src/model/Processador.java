package model;

public class Processador extends Componente {

    private String fabricante;
    private String socket;
    private int nucleos;
    private int threads;
    private int consumo;
    private double desempenho;

    public Processador() {
    }

    public Processador(int id,
                       String nome,
                       double preco,
                       String fabricante,
                       String socket,
                       int nucleos,
                       int threads,
                       int consumo,
                       double desempenho) {

        super(id, nome, preco);

        this.fabricante = fabricante;
        this.socket = socket;
        this.nucleos = nucleos;
        this.threads = threads;
        this.consumo = consumo;
        this.desempenho = desempenho;
    }

    @Override
    public String toString() {

        return id +
                " - " +
                nome +
                " | " +
                socket +
                " | R$ " +
                preco;

    }

    // getters
    public String getFabricante() {
        return fabricante;
    }

    public String getSocket() {
        return socket;
    }

    public int getNucleos() {
        return nucleos;
    }

    public int getThreads() {
        return threads;
    }

    public int getConsumo() {
        return consumo;
    }

    public double getDesempenho() {
        return desempenho;
    }

    // setters
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public void setNucleos(int nucleos) {
        this.nucleos = nucleos;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setConsumo(int consumo) {
        this.consumo = consumo;
    }

    public void setDesempenho(double desempenho) {
        this.desempenho = desempenho;
    }

}