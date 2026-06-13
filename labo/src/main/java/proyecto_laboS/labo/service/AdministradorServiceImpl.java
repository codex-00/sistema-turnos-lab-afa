package proyecto_laboS.labo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Administrador;
import proyecto_laboS.labo.repository.AdministradorRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;

@Service
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PasswordService passwordService;

    @Override
    public List<Administrador> listarAdministradores() {
        return administradorRepository.findAll();
    }

    @Override
    public Optional<Administrador> obtenerAdministradorPorId(Long id) {
        return administradorRepository.findById(id);
    }

    @Override
    public Administrador guardarAdministrador(Administrador administrador) {
        validarAdministrador(administrador, null);
        UsuarioValidationUtils.validarPasswordCreacion(administrador.getPassword(), "administrador");
        administrador.setPassword(passwordService.encode(administrador.getPassword()));
        return administradorRepository.save(administrador);
    }

    @Override
    public Administrador actualizarAdministrador(Long id, Administrador administrador) {
        return administradorRepository.findById(id).map(existing -> {
            validarAdministrador(administrador, id);
            administrador.setIdAdministrador(id);
            if (administrador.getFechaRegistro() == null) {
                administrador.setFechaRegistro(existing.getFechaRegistro());
            }
            if (administrador.getPassword() == null || administrador.getPassword().isBlank() || administrador.getPassword().equals(existing.getPassword())) {
                administrador.setPassword(existing.getPassword());
            } else {
                administrador.setPassword(passwordService.encode(administrador.getPassword()));
            }
            return administradorRepository.save(administrador);
        }).orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado"));
    }

    @Override
    public void eliminarAdministrador(Long id) {
        if (!administradorRepository.existsById(id)) {
            throw new IllegalArgumentException("Administrador no encontrado");
        }
        administradorRepository.deleteById(id);
    }

    @Override
    public boolean esAdministradorPrincipal(Administrador administrador) {
        return administrador != null && "admin@clinica.com".equalsIgnoreCase(administrador.getEmail());
    }

    private void validarAdministrador(Administrador administrador, Long idActual) {
        if (administrador == null) {
            throw new IllegalArgumentException("Los datos del administrador son obligatorios");
        }

        UsuarioValidationUtils.validarDatosPersonales(administrador, "administrador");

        administradorRepository.findByEmail(administrador.getEmail())
                .filter(existente -> idActual == null || !existente.getIdAdministrador().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un administrador con ese email");
                });

        medicoRepository.findByEmail(administrador.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese email");
                });

        pacienteRepository.findByEmail(administrador.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese email");
                });
    }
}
