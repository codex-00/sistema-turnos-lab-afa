package proyecto_laboS.labo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EstadoTurnoTest {

    @Test
    void normalizaEstadoVacioComoPendiente() {
        assertEquals("PENDIENTE", EstadoTurno.normalizar(null));
        assertEquals("PENDIENTE", EstadoTurno.normalizar(""));
    }

    @Test
    void aceptaEstadoRealizado() {
        assertEquals("REALIZADO", EstadoTurno.normalizar("realizado"));
    }

    @Test
    void rechazaEstadosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> EstadoTurno.normalizar("APROBADO"));
        assertThrows(IllegalArgumentException.class, () -> EstadoTurno.normalizar("RECHAZADO"));
        assertThrows(IllegalArgumentException.class, () -> EstadoTurno.normalizar("FINALIZADO"));
    }
}
