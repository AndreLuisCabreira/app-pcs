package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    private final PasswordService service = new PasswordService();

    @Test
    void criaHashComSaltEValidaASenha() {
        String primeiro = service.criarHash("senha-segura");
        String segundo = service.criarHash("senha-segura");

        assertNotEquals("senha-segura", primeiro);
        assertNotEquals(primeiro, segundo);
        assertTrue(service.verificar("senha-segura", primeiro));
        assertFalse(service.verificar("senha-errada", primeiro));
    }
}
