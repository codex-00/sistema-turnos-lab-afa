package proyecto_laboS.labo.model;

import java.util.Arrays;

public enum EstadoTurno {
    PENDIENTE,
    REALIZADO,
    CANCELADO,
    REPROGRAMADO;

    public static String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return PENDIENTE.name();
        }

        String normalizado = estado.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(valor -> valor.name().equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de turno invalido: " + estado))
                .name();
    }
}
