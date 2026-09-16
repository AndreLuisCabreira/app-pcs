package model;

public class PlacaMae extends Componente {

    private String fabricante;
    private String socket;
    private String tipoMemoria;
    private int consumo;

    public PlacaMae() {}

    public PlacaMae(int id, String nome, double preco,
                    String fabricante, String socket,
                    String tipoMemoria, int consumo) {

        super(id, nome, preco);
        this.fabricante = fabricante;
        this.socket = socket;
        this.tipoMemoria = tipoMemoria;
        this.consumo = consumo;
    }

    // getters
    public String getFabricante() {
        return fabricante;
    }

    public String getSocket() {
        return socket;
    }

    public String getTipoMemoria() {
        return tipoMemoria;
    }

    public int getConsumo() {
        return consumo;
    }

    // setters
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public void setTipoMemoria(String tipoMemoria) {
        this.tipoMemoria = tipoMemoria;
    }

    public void setConsumo(int consumo) {
        this.consumo = consumo;
    }

    @Override
    public String toString() {

        return id +
                " - " +
                nome +
                " | " +
                socket +
                " | " +
                tipoMemoria +
                " | R$ " +
                preco;

    }
}