package proyecto_laboS.labo.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void encriptaYValidaPasswordConBcrypt() {
        String encoded = passwordService.encode("clave123");

        assertNotEquals("clave123", encoded);
        assertTrue(passwordService.matches("clave123", encoded));
    }

    @Test
    void mantieneCompatibilidadConPasswordsLegacyEnTextoPlano() {
        assertTrue(passwordService.matches("clave123", "clave123"));
    }
}
