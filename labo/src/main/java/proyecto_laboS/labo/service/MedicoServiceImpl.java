package proyecto_laboS.labo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.repository.AdministradorRepository;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;

@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private PasswordService passwordService;

    @Override
    public List<Medico> listarMedicos() {
        List<Medico> medicos = medicoRepository.findAll();
        for (Medico medico : medicos) {
            medico.setDisponibilidades(disponibilidadRepository.findByMedico(medico));
        }
        return medicos;
    }

    @Override
    public Optional<Medico> obtenerMedicoPorId(Long id) {
        Optional<Medico> medico = medicoRepository.findById(id);
        medico.ifPresent(value -> value.setDisponibilidades(disponibilidadRepository.findByMedico(value)));
        return medico;
    }

    @Override
    @Transactional
    public Medico guardarMedico(Medico medico) {
        validarMedico(medico, null);
        UsuarioValidationUtils.validarPasswordCreacion(medico.getPassword(), "medico");
        medico.setPassword(passwordService.encode(medico.getPassword()));
        List<Disponibilidad> disponibilidades = medico.getDisponibilidades();
        asignarMedicoADisponibilidades(medico, disponibilidades);
        disponibilidadService.validarDisponibilidadesSinSolapamientos(disponibilidades);
        Medico medicoGuardado = medicoRepository.save(medico);
        guardarDisponibilidades(medicoGuardado, disponibilidades);
        medicoGuardado.setDisponibilidades(null);
        return medicoGuardado;
    }

    @Override
    @Transactional
    public Medico actualizarMedico(Long id, Medico medico) {
        Optional<Medico> medicoExistente = medicoRepository.findById(id);
        if (medicoExistente.isEmpty()) {
            throw new IllegalArgumentException("Medico no encontrado");
        }

        validarMedico(medico, id);
        List<Disponibilidad> disponibilidades = medico.getDisponibilidades();
        asignarMedicoADisponibilidades(medico, disponibilidades);
        disponibilidadService.validarDisponibilidadesSinSolapamientos(disponibilidades);
        medico.setIdMedico(id);
        if (medico.getFechaRegistro() == null) {
            medico.setFechaRegistro(medicoExistente.get().getFechaRegistro());
        }
        if (medico.getEstado() == null || medico.getEstado().isBlank()) {
            medico.setEstado(medicoExistente.get().getEstado());
        }
        if (medico.getPassword() == null || medico.getPassword().isBlank()) {
            medico.setPassword(medicoExistente.get().getPassword());
        } else {
            medico.setPassword(passwordService.encode(medico.getPassword()));
        }

        Medico medicoGuardado = medicoRepository.save(medico);
        if (disponibilidades != null) {
            disponibilidadRepository.deleteAll(disponibilidadRepository.findByMedico(medicoGuardado));
            guardarDisponibilidades(medicoGuardado, disponibilidades);
        }
        medicoGuardado.setDisponibilidades(null);
        return medicoGuardado;
    }

    @Override
    public void eliminarMedico(Long id) {
        if (!medicoRepository.existsById(id)) {
            throw new IllegalArgumentException("Medico no encontrado");
        }
        medicoRepository.deleteById(id);
    }

    @Override
    public List<Medico> buscarPorEspecialidad(String especialidad) {
        String normalizada = normalizarEspecialidad(especialidad);
        return listarMedicos().stream()
                .filter(medico -> medico.getEspecialidades().stream().anyMatch(item -> item.equalsIgnoreCase(normalizada)))
                .toList();
    }

    @Override
    public List<Medico> buscarPorNombre(String nombre) {
        return medicoRepository.findByNombre(nombre);
    }

    private void guardarDisponibilidades(Medico medico, List<Disponibilidad> disponibilidades) {
        if (disponibilidades == null) {
            return;
        }

        for (Disponibilidad disponibilidad : disponibilidades) {
            disponibilidad.setId(null);
            disponibilidad.setMedico(medico);
            disponibilidadRepository.save(disponibilidad);
        }
    }

    private void asignarMedicoADisponibilidades(Medico medico, List<Disponibilidad> disponibilidades) {
        if (disponibilidades == null) {
            return;
        }
        disponibilidades.forEach(disponibilidad -> disponibilidad.setMedico(medico));
    }

    private void validarMedico(Medico medico, Long idActual) {
        if (medico == null) {
            throw new IllegalArgumentException("Los datos del medico son obligatorios");
        }

        UsuarioValidationUtils.validarDatosPersonales(medico, "medico");
        String dni = UsuarioValidationUtils.validarDni(medico.getDni(), "medico");
        medico.setDni(dni);

        String especialidad = medico.getEspecialidad() != null ? medico.getEspecialidad().trim() : "";
        if (medico.getEspecialidades() != null && !medico.getEspecialidades().isEmpty()) {
            especialidad = normalizarEspecialidades(medico.getEspecialidades());
        }
        especialidad = normalizarEspecialidades(List.of(especialidad.split(",")));
        medico.setEspecialidad(especialidad);

        medicoRepository.findByDni(dni)
                .filter(existente -> idActual == null || !existente.getIdMedico().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese DNI");
                });

        pacienteRepository.findByDni(dni)
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese DNI");
                });

        medicoRepository.findByEmail(medico.getEmail())
                .filter(existente -> idActual == null || !existente.getIdMedico().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese email");
                });

        pacienteRepository.findByEmail(medico.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese email");
                });

        administradorRepository.findByEmail(medico.getEmail())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un administrador con ese email");
                });

        medicoRepository.findByNombreIgnoreCaseAndApellidoIgnoreCase(medico.getNombre(), medico.getApellido())
                .filter(existente -> idActual == null || !existente.getIdMedico().equals(idActual))
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un medico con ese nombre y apellido");
                });

        pacienteRepository.findByNombreIgnoreCaseAndApellidoIgnoreCase(medico.getNombre(), medico.getApellido())
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe un paciente con ese nombre y apellido");
                });
    }

    private String normalizarEspecialidades(List<String> especialidades) {
        List<String> normalizadas = especialidades.stream()
                .map(this::normalizarEspecialidad)
                .distinct()
                .toList();
        if (normalizadas.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una especialidad del medico");
        }
        return String.join(", ", normalizadas);
    }

    private String normalizarEspecialidad(String especialidad) {
        String normalizada = especialidad == null ? "" : especialidad.trim().replaceAll("\\s+", " ");
        if (!normalizada.matches("^[\\p{L} ]{3,60}$")) {
            throw new IllegalArgumentException("La especialidad del medico es obligatoria y solo puede contener letras y espacios");
        }
        return normalizada;
    }
}
