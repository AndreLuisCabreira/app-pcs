package model;

public class Usuario {

    private int id;
    private String nome;
    private String login;
    private String senha;
    private PerfilUsuario perfil = PerfilUsuario.USUARIO;

    public Usuario() {
    }

    public Usuario(String nome, String login, String senha) {
        this(nome, login, senha, PerfilUsuario.USUARIO);
    }

    public Usuario(String nome, String login, String senha, PerfilUsuario perfil) {
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil == null ? PerfilUsuario.USUARIO : perfil;
    }

    public Usuario(int id, String nome, String login, String senha) {
        this(id, nome, login, senha, PerfilUsuario.USUARIO);
    }

    public Usuario(int id, String nome, String login, String senha, PerfilUsuario perfil) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.perfil = perfil == null ? PerfilUsuario.USUARIO : perfil;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil == null ? PerfilUsuario.USUARIO : perfil;
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuario.ADMIN;
    }

    @Override
    public String toString() {
        return "ID: " + id +
                "\nNome: " + nome +
                "\nLogin: " + login +
                "\nPerfil: " + perfil;
    }
}
