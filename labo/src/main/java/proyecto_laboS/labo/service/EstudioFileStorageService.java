package proyecto_laboS.labo.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import proyecto_laboS.labo.config.EstudioStorageProperties;
import proyecto_laboS.labo.service.exception.EstudioValidationException;

@Service
public class EstudioFileStorageService {

    private final EstudioStorageProperties properties;
    private Path rootDirectory;

    public EstudioFileStorageService(EstudioStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        try {
            rootDirectory = Paths.get(properties.getStorageDirectory()).toAbsolutePath().normalize();
            Files.createDirectories(rootDirectory);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo crear el directorio de almacenamiento de estudios", ex);
        }
    }

    public StoredEstudioFile store(MultipartFile file) {
        validatePdf(file);

        String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        String internalFileName = UUID.randomUUID() + ".pdf";
        Path target = rootDirectory.resolve(internalFileName).normalize();

        if (!target.startsWith(rootDirectory)) {
            throw new EstudioValidationException("Ruta de archivo invalida");
        }

        try {
            file.transferTo(target);
            return new StoredEstudioFile(
                    originalFileName,
                    internalFileName,
                    file.getContentType(),
                    file.getSize(),
                    target);
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo almacenar el estudio PDF", ex);
        }
    }

    public Optional<Path> resolveExisting(String storedPath) {
        if (!StringUtils.hasText(storedPath)) {
            return Optional.empty();
        }

        Path path = Paths.get(storedPath).toAbsolutePath().normalize();
        if (!path.startsWith(rootDirectory) || !Files.exists(path) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }

    public Optional<Path> resolveExistingByFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return Optional.empty();
        }

        String cleaned = StringUtils.cleanPath(fileName.trim());
        Path fileNamePath = Paths.get(cleaned).getFileName();
        if (fileNamePath == null || cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            return Optional.empty();
        }

        Path path = rootDirectory.resolve(fileNamePath.toString()).toAbsolutePath().normalize();
        if (!path.startsWith(rootDirectory) || !Files.exists(path) || !Files.isRegularFile(path)) {
            return Optional.empty();
        }

        return Optional.of(path);
    }

    public void deleteQuietly(String storedPath) {
        Optional<Path> path = resolveExisting(storedPath);

        try {
            if (path.isPresent()) {
                Files.deleteIfExists(path.get());
            }
        } catch (IOException ignored) {
            // La eliminacion del registro no debe fallar por un archivo ya inaccesible.
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EstudioValidationException("El archivo PDF es obligatorio");
        }

        if (file.getSize() > properties.getMaxFileSize().toBytes()) {
            throw new EstudioValidationException("El PDF supera el tamano maximo permitido");
        }

        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedMimeTypes().contains(contentType)) {
            throw new EstudioValidationException("Solo se permiten archivos PDF con MIME type application/pdf");
        }

        String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        if (!originalFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new EstudioValidationException("Solo se permiten archivos con extension .pdf");
        }
    }

    private String sanitizeOriginalFileName(String fileName) {
        String cleaned = StringUtils.cleanPath(fileName == null ? "" : fileName.trim());
        String onlyName = Paths.get(cleaned).getFileName() == null ? "" : Paths.get(cleaned).getFileName().toString();

        if (!StringUtils.hasText(onlyName) || cleaned.contains("..") || cleaned.contains("/") || cleaned.contains("\\")) {
            throw new EstudioValidationException("Nombre de archivo invalido");
        }

        return onlyName.replaceAll("[^a-zA-Z0-9._ -]", "_");
    }
}
