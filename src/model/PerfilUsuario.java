package model;

import java.util.Locale;

public enum PerfilUsuario {
    ADMIN,
    USUARIO;

    public static PerfilUsuario doBanco(String valor) {
        if (valor == null || valor.isBlank()) {
            return USUARIO;
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return USUARIO;
        }
    }
}
