package proyecto_laboS.labo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import proyecto_laboS.labo.model.Administrador;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.service.AdministradorService;
import proyecto_laboS.labo.service.MedicoService;
import proyecto_laboS.labo.service.PacienteService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private AdministradorService administradorService;

    @GetMapping
    public List<UsuarioDto> listarUsuarios() {
        List<UsuarioDto> usuarios = new ArrayList<>();

        pacienteService.listarPacientes().forEach(paciente -> usuarios.add(toDto(paciente)));
        medicoService.listarMedicos().forEach(medico -> usuarios.add(toDto(medico)));
        administradorService.listarAdministradores().forEach(admin -> usuarios.add(toDto(admin)));

        return usuarios;
    }

    @PostMapping("/admin")
    public UsuarioDto crearAdministrador(@RequestBody UsuarioDto usuario) {
        Administrador administrador = new Administrador();
        administrador.setNombre(usuario.nombre());
        administrador.setApellido(usuario.apellido());
        administrador.setEmail(usuario.email());
        administrador.setDireccion(usuario.direccion());
        administrador.setTelefono(usuario.telefono());
        administrador.setEstado(usuario.estado() != null ? usuario.estado() : "ACTIVO");
        administrador.setPassword(usuario.password());
        return toDto(administradorService.guardarAdministrador(administrador));
    }

    @PutMapping("/{rol}/{id}")
    public UsuarioDto actualizarUsuario(@PathVariable String rol, @PathVariable Long id, @RequestBody UsuarioDto usuario) {
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }

        switch (rol.toLowerCase()) {
            case "paciente":
                return toDto(updatePaciente(id, usuario));
            case "medico":
                return toDto(updateMedico(id, usuario));
            case "admin":
                return toDto(updateAdministrador(id, usuario));
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
    }

    @DeleteMapping("/{rol}/{id}")
    public void eliminarUsuario(@PathVariable String rol, @PathVariable Long id) {
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio");
        }

        switch (rol.toLowerCase()) {
            case "paciente":
                pacienteService.eliminarPaciente(id);
                break;
            case "medico":
                medicoService.eliminarMedico(id);
                break;
            case "admin":
                Administrador admin = administradorService.obtenerAdministradorPorId(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador no encontrado"));
                if (administradorService.esAdministradorPrincipal(admin)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede eliminar el administrador principal");
                }
                administradorService.eliminarAdministrador(id);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
    }

    private Paciente updatePaciente(Long id, UsuarioDto usuario) {
        Paciente pacienteExistente = pacienteService.obtenerPacientePorId(id);
        if (pacienteExistente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        Paciente pacienteActualizado = new Paciente();
        pacienteActualizado.setIdPaciente(id);
        pacienteActualizado.setNombre(usuario.nombre());
        pacienteActualizado.setApellido(usuario.apellido());
        pacienteActualizado.setEmail(usuario.email());
        pacienteActualizado.setDni(usuario.dni());
        pacienteActualizado.setDireccion(usuario.direccion());
        pacienteActualizado.setTelefono(usuario.telefono());
        pacienteActualizado.setEstado(usuario.estado() != null ? usuario.estado() : pacienteExistente.getEstado());
        pacienteActualizado.setPassword(pacienteExistente.getPassword());
        pacienteActualizado.setFechaRegistro(pacienteExistente.getFechaRegistro());

        return pacienteService.actualizarPaciente(id, pacienteActualizado);
    }

    private Medico updateMedico(Long id, UsuarioDto usuario) {
        Medico medicoExistente = medicoService.obtenerMedicoPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Médico no encontrado"));

        Medico medicoActualizado = new Medico();
        medicoActualizado.setIdMedico(id);
        medicoActualizado.setNombre(usuario.nombre());
        medicoActualizado.setApellido(usuario.apellido());
        medicoActualizado.setEmail(usuario.email());
        medicoActualizado.setDni(usuario.dni() != null ? usuario.dni() : medicoExistente.getDni());
        medicoActualizado.setDireccion(usuario.direccion());
        medicoActualizado.setTelefono(usuario.telefono());
        medicoActualizado.setEstado(usuario.estado() != null ? usuario.estado() : medicoExistente.getEstado());
        medicoActualizado.setPassword(medicoExistente.getPassword());
        medicoActualizado.setFechaRegistro(medicoExistente.getFechaRegistro());
        medicoActualizado.setEspecialidad(medicoExistente.getEspecialidad());

        return medicoService.actualizarMedico(id, medicoActualizado);
    }

    private Administrador updateAdministrador(Long id, UsuarioDto usuario) {
        Administrador adminExistente = administradorService.obtenerAdministradorPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador no encontrado"));

        if (administradorService.esAdministradorPrincipal(adminExistente)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede modificar el administrador principal");
        }

        Administrador adminActualizado = new Administrador();
        adminActualizado.setIdAdministrador(id);
        adminActualizado.setNombre(usuario.nombre());
        adminActualizado.setApellido(usuario.apellido());
        adminActualizado.setEmail(usuario.email());
        adminActualizado.setDireccion(usuario.direccion());
        adminActualizado.setTelefono(usuario.telefono());
        adminActualizado.setEstado(usuario.estado() != null ? usuario.estado() : adminExistente.getEstado());
        adminActualizado.setPassword(usuario.password() != null && !usuario.password().isBlank() ? usuario.password() : adminExistente.getPassword());
        adminActualizado.setFechaRegistro(adminExistente.getFechaRegistro());

        return administradorService.actualizarAdministrador(id, adminActualizado);
    }

    private UsuarioDto toDto(Paciente paciente) {
        return new UsuarioDto(
                paciente.getIdPaciente(),
                "paciente",
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getEmail(),
                paciente.getDni(),
                paciente.getDireccion(),
                paciente.getTelefono(),
                paciente.getEstado(),
                null,
                administradorService.esAdministradorPrincipal(null));
    }

    private UsuarioDto toDto(Medico medico) {
        return new UsuarioDto(
                medico.getIdMedico(),
                "medico",
                medico.getNombre(),
                medico.getApellido(),
                medico.getEmail(),
                medico.getDni(),
                medico.getDireccion(),
                medico.getTelefono(),
                medico.getEstado(),
                null,
                administradorService.esAdministradorPrincipal(null));
    }

    private UsuarioDto toDto(Administrador administrador) {
        return new UsuarioDto(
                administrador.getIdAdministrador(),
                "admin",
                administrador.getNombre(),
                administrador.getApellido(),
                administrador.getEmail(),
                null,
                administrador.getDireccion(),
                administrador.getTelefono(),
                administrador.getEstado(),
                null,
                administradorService.esAdministradorPrincipal(administrador));
    }

    public static record UsuarioDto(
            Long id,
            String rol,
            String nombre,
            String apellido,
            String email,
            String dni,
            String direccion,
            String telefono,
            String estado,
            String password,
            boolean principal)
    {
    }
}
