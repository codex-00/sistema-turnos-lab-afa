package proyecto_laboS.labo.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.model.Administrador;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.model.PasswordResetToken;
import proyecto_laboS.labo.repository.AdministradorRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;
import proyecto_laboS.labo.repository.PasswordResetTokenRepository;
import proyecto_laboS.labo.service.EmailService;
import proyecto_laboS.labo.service.PasswordService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.password()) || isBlank(request.rol())) {
            return credencialesInvalidas();
        }

        String rol = request.rol().trim().toLowerCase();
        String email = request.email().trim();

        if ("admin".equals(rol)) {
            Optional<Administrador> administrador = administradorRepository.findByEmail(email)
                    .filter(usuario -> passwordService.matches(request.password(), usuario.getPassword()));
            if (administrador.isPresent()) {
                Administrador usuario = administrador.get();
                AuthUser authUser = new AuthUser(
                        usuario.getIdAdministrador(),
                        usuario.getEmail(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        "admin");
                return ResponseEntity.ok(new LoginResponse(authUser.id(), authUser.id(), authUser.rol(), authUser));
            }

            return credencialesInvalidas();
        }

        if ("medico".equals(rol)) {
            Optional<Medico> medico = medicoRepository.findByEmail(email)
                    .filter(usuario -> passwordService.matches(request.password(), usuario.getPassword()));
            if (medico.isPresent()) {
                Medico usuario = medico.get();
                AuthUser authUser = new AuthUser(
                        usuario.getIdMedico(),
                        usuario.getEmail(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        "medico");
                return ResponseEntity.ok(new LoginResponse(authUser.id(), authUser.id(), authUser.rol(), authUser));
            }

            return credencialesInvalidas();
        }

        if ("paciente".equals(rol)) {
            Optional<Paciente> paciente = pacienteRepository.findByEmail(email)
                    .filter(usuario -> passwordService.matches(request.password(), usuario.getPassword()));
            if (paciente.isPresent()) {
                Paciente usuario = paciente.get();
                AuthUser authUser = new AuthUser(
                        usuario.getIdPaciente(),
                        usuario.getEmail(),
                        usuario.getNombre(),
                        usuario.getApellido(),
                        "paciente");
                return ResponseEntity.ok(new LoginResponse(authUser.id(), authUser.id(), authUser.rol(), authUser));
            }

            return credencialesInvalidas();
        }

        return credencialesInvalidas();
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<?> recuperarPassword(@RequestBody PasswordRecoveryRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.rol())) {
            return ResponseEntity.badRequest().body(new AuthError("INVALID_REQUEST", "Email y rol son obligatorios"));
        }

        String email = request.email().trim();
        String rol = request.rol().trim().toLowerCase();

        if (!existeUsuario(email, rol)) {
            return ResponseEntity.ok(new PasswordRecoveryResponse(
                    "Si el email existe, se enviarán instrucciones de recuperación.",
                    null,
                    false));
        }

        String token = generarToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(email);
        resetToken.setRol(rol);
        resetToken.setTokenHash(hashToken(token));
        resetToken.setFechaCreacion(LocalDateTime.now());
        resetToken.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsado(false);
        passwordResetTokenRepository.save(resetToken);

        boolean mailEnviado = enviarMailRecuperacion(email, token);

        return ResponseEntity.ok(new PasswordRecoveryResponse(
                mailEnviado
                        ? "Se envió un email con el código de recuperación."
                        : "No hay SMTP configurado. Use el token de desarrollo para recuperar la contraseña.",
                mailEnviado ? null : token,
                mailEnviado));
    }

    @PostMapping("/restablecer-password")
    public ResponseEntity<?> restablecerPassword(@RequestBody PasswordResetRequest request) {
        if (request == null || isBlank(request.email()) || isBlank(request.rol())
                || isBlank(request.token()) || isBlank(request.nuevaPassword())) {
            return ResponseEntity.badRequest().body(new AuthError("INVALID_REQUEST", "Todos los campos son obligatorios"));
        }

        String email = request.email().trim();
        String rol = request.rol().trim().toLowerCase();
        String tokenHash = hashToken(request.token().trim());

        Optional<PasswordResetToken> resetToken = passwordResetTokenRepository
                .findFirstByEmailAndRolAndTokenHashAndUsadoFalseOrderByFechaCreacionDesc(email, rol, tokenHash);

        if (resetToken.isEmpty() || resetToken.get().getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthError("INVALID_TOKEN", "El código de recuperación es inválido o está vencido"));
        }

        if (!actualizarPassword(email, rol, request.nuevaPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthError("INVALID_USER", "No se encontro el usuario indicado"));
        }

        PasswordResetToken tokenUsado = resetToken.get();
        tokenUsado.setUsado(true);
        passwordResetTokenRepository.save(tokenUsado);

        return ResponseEntity.ok(new PasswordResetResponse("Contraseña actualizada correctamente"));
    }

    private ResponseEntity<AuthError> credencialesInvalidas() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AuthError("INVALID_CREDENTIALS", "Usuario o contraseña incorrectos"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean existeUsuario(String email, String rol) {
        if ("admin".equals(rol)) {
            return administradorRepository.findByEmail(email).isPresent();
        }

        if ("medico".equals(rol)) {
            return medicoRepository.findByEmail(email).isPresent();
        }

        if ("paciente".equals(rol)) {
            return pacienteRepository.findByEmail(email).isPresent();
        }

        return false;
    }

    private boolean actualizarPassword(String email, String rol, String nuevaPassword) {
        if ("admin".equals(rol)) {
            return administradorRepository.findByEmail(email).map(usuario -> {
                usuario.setPassword(passwordService.encode(nuevaPassword));
                administradorRepository.save(usuario);
                return true;
            }).orElse(false);
        }

        if ("medico".equals(rol)) {
            return medicoRepository.findByEmail(email).map(usuario -> {
                usuario.setPassword(passwordService.encode(nuevaPassword));
                medicoRepository.save(usuario);
                return true;
            }).orElse(false);
        }

        if ("paciente".equals(rol)) {
            return pacienteRepository.findByEmail(email).map(usuario -> {
                usuario.setPassword(passwordService.encode(nuevaPassword));
                pacienteRepository.save(usuario);
                return true;
            }).orElse(false);
        }

        return false;
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el hash de recuperación", ex);
        }
    }

    private boolean enviarMailRecuperacion(String email, String token) {
        return emailService.enviarRecuperacionPassword(email, token);
    }

    public record LoginRequest(String email, String password, String rol) {
    }

    public record LoginResponse(Long token, Long usuarioId, String rol, AuthUser usuario) {
    }

    public record PasswordRecoveryRequest(String email, String rol) {
    }

    public record PasswordRecoveryResponse(String message, String devToken, Boolean mailEnviado) {
    }

    public record PasswordResetRequest(String email, String rol, String token, String nuevaPassword) {
    }

    public record PasswordResetResponse(String message) {
    }

    public record AuthUser(Long id, String email, String nombre, String apellido, String rol) {
    }

    public record AuthError(String code, String message) {
    }
}
