package proyecto_laboS.labo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.repository.AdministradorRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;

@Service
public class PacienteServiceImpl implements PacienteService {

    @Autowired
    PacienteRepository pacienteRepository;

    @Autowired
    MedicoRepository medicoRepository;

    @Autowired
    AdministradorRepository administradorRepository;

    @Autowired
    PasswordService passwordService;

    @Override
    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    @Override
    public Paciente obtenerPacientePorId(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    @Override
    public Paciente guardarPaciente(Paciente paciente) {
        validarPaciente(paciente, null);
        UsuarioValidationUtils.validarPasswordCreacion(paciente.getPassword(), "paciente");
        paciente.setPassword(passwordService.encode(paciente.getPassword()));
        return pacienteRepository.save(paciente);
    }

    @Override
    public Paciente actualizarPaciente(Long id, Paciente paciente) {
        return pacienteRepository.findById(id).map(pacienteExistente -> {
            validarPaciente(paciente, id);
            paciente.setIdPaciente(id);
            if (paciente.getFechaRegistro() == null) {
                paciente.setFechaRegistro(pacienteExistente.getFechaRegistro());
            }
            if (paciente.getPassword() == null || paciente.getPassword().isBlank()) {
                paciente.setPassword(pacienteExistente.getPassword());
            } else {
                paciente.setPassword(passwordService.encode(paciente.getPassword()));
            }
            return pacienteRepository.save(paciente);
        }).orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
    }

    @Override
    public void eliminarPaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new IllegalArgumentException("Paciente no encontrado");
        }
        pacienteRepository.deleteById(id);
    }

    private void validarPaciente(Paciente paciente, Long idActual) {
        if (paciente == null) {
            throw new IllegalArgumentException("Los datos del paciente son obligatorios");
        }

        UsuarioValidationUtils.validarDatosPersonales(paciente, "paciente");
        String dni = UsuarioValidationUtils.validarDni(paciente.getDni(), "paciente");
        paciente.setDni(dni);

        pacienteRepository.findByDni(dni)
                .filter(existente -> idActual == null || !existente.getIdPaciente().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese DNI");
                });

        medicoRepository.findByDni(dni)
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese DNI");
                });

        pacienteRepository.findByEmail(paciente.getEmail())
                .filter(existente -> idActual == null || !existente.getIdPaciente().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese email");
                });

        medicoRepository.findByEmail(paciente.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese email");
                });

        administradorRepository.findByEmail(paciente.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un administrador con ese email");
                });

        pacienteRepository.findByNombreIgnoreCaseAndApellidoIgnoreCase(paciente.getNombre(), paciente.getApellido())
                .filter(existente -> idActual == null || !existente.getIdPaciente().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese nombre y apellido");
                });

        medicoRepository.findByNombreIgnoreCaseAndApellidoIgnoreCase(paciente.getNombre(), paciente.getApellido())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese nombre y apellido");
                });
    }
}
