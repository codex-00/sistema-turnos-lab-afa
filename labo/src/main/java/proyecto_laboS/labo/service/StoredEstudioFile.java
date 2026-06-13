package proyecto_laboS.labo.service;

import java.nio.file.Path;

public record StoredEstudioFile(
        String originalFileName,
        String internalFileName,
        String contentType,
        long size,
        Path path) {
}
