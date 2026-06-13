package proyecto_laboS.labo.controller;

import java.io.UncheckedIOException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import proyecto_laboS.labo.service.exception.EstudioAccessDeniedException;
import proyecto_laboS.labo.service.exception.EstudioNotFoundException;
import proyecto_laboS.labo.service.exception.EstudioValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(EstudioNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EstudioNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "ESTUDIO_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(EstudioAccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(EstudioAccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ESTUDIO_FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(EstudioValidationException.class)
    public ResponseEntity<ApiError> handleValidation(EstudioValidationException ex) {
        return build(HttpStatus.BAD_REQUEST, "ESTUDIO_VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("no encontrado")) {
            return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(UncheckedIOException.class)
    public ResponseEntity<ApiError> handleStorageError(UncheckedIOException ex) {
        log.error("Error de almacenamiento de estudio", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ESTUDIO_STORAGE_ERROR", "No se pudo procesar el archivo del estudio");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message, LocalDateTime.now()));
    }

    public record ApiError(String code, String message, LocalDateTime timestamp) {
    }
}
