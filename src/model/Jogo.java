package model;

public class Jogo {

    private int id;
    private String nome;

    private int exigenciaCpu;
    private int exigenciaGpu;
    private String genero;
    private String descricao;
    private String imagemUrl;

    public Jogo() {
    }

    public Jogo(int id,
                String nome,
                int exigenciaCpu,
                int exigenciaGpu) {

        this(id, nome, exigenciaCpu, exigenciaGpu, null, null, null);
    }

    public Jogo(int id,
                String nome,
                int exigenciaCpu,
                int exigenciaGpu,
                String genero,
                String descricao,
                String imagemUrl) {

        this.id = id;
        this.nome = nome;
        this.exigenciaCpu = exigenciaCpu;
        this.exigenciaGpu = exigenciaGpu;
        this.genero = genero;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
    }


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

    public int getExigenciaCpu() {
        return exigenciaCpu;
    }

    public void setExigenciaCpu(int exigenciaCpu) {
        this.exigenciaCpu = exigenciaCpu;
    }

    public int getExigenciaGpu() {
        return exigenciaGpu;
    }

    public void setExigenciaGpu(int exigenciaGpu) {
        this.exigenciaGpu = exigenciaGpu;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
