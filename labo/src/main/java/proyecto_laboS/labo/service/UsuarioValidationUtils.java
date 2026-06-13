package proyecto_laboS.labo.service;

import java.util.regex.Pattern;

import proyecto_laboS.labo.model.Usuario;

final class UsuarioValidationUtils {

    private static final Pattern NOMBRE_PATTERN = Pattern.compile("^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,10}$");
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{6,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern DIRECCION_PATTERN = Pattern.compile("^[\\p{L}\\d .,'\\-#/]+$");

    private UsuarioValidationUtils() {
    }

    static void validarDatosPersonales(Usuario usuario, String tipoUsuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del " + tipoUsuario + " son obligatorios");
        }

        usuario.setNombre(validarNombre(usuario.getNombre(), "nombre"));
        usuario.setApellido(validarNombre(usuario.getApellido(), "apellido"));
        usuario.setEmail(validarEmail(usuario.getEmail(), tipoUsuario));
        usuario.setTelefono(validarTelefono(usuario.getTelefono(), tipoUsuario));
        usuario.setDireccion(validarDireccion(usuario.getDireccion(), tipoUsuario));
    }

    static String validarDni(String dni, String tipoUsuario) {
        String normalizado = compactarEspacios(dni);
        if (!DNI_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("El DNI del " + tipoUsuario + " es obligatorio y debe contener entre 7 y 10 digitos");
        }
        return normalizado;
    }

    static void validarPasswordCreacion(String password, String tipoUsuario) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("La contrasena del " + tipoUsuario + " es obligatoria");
        }
    }

    private static String validarNombre(String valor, String campo) {
        String normalizado = compactarEspacios(valor);
        if (normalizado.length() < 2 || normalizado.length() > 60 || !NOMBRE_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("El " + campo + " solo puede contener letras y espacios");
        }
        return normalizado;
    }

    private static String validarEmail(String email, String tipoUsuario) {
        String normalizado = compactarEspacios(email).toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("El email del " + tipoUsuario + " no tiene un formato valido");
        }
        return normalizado;
    }

    private static String validarTelefono(String telefono, String tipoUsuario) {
        String normalizado = compactarEspacios(telefono);
        if (!TELEFONO_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("El telefono del " + tipoUsuario + " debe contener solo numeros y entre 6 y 15 digitos");
        }
        return normalizado;
    }

    private static String validarDireccion(String direccion, String tipoUsuario) {
        String normalizado = compactarEspacios(direccion);
        if (normalizado.length() < 3 || normalizado.length() > 120 || !DIRECCION_PATTERN.matcher(normalizado).matches()) {
            throw new IllegalArgumentException("La direccion del " + tipoUsuario + " contiene caracteres no validos");
        }
        return normalizado;
    }

    private static String compactarEspacios(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
