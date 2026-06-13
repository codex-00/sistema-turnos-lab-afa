package proyecto_laboS.labo.service;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.MedicoRepository;

@Service
public class DisponibilidadService {

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    public Disponibilidad crearDisponibilidad(Long medicoId, Disponibilidad disponibilidad) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado"));

        disponibilidad.setMedico(medico);
        completarEspecialidad(disponibilidad);
        validarDisponibilidad(disponibilidad, null);
        return disponibilidadRepository.save(disponibilidad);
    }

    public List<Disponibilidad> obtenerPorMedico(Long medicoId) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado"));
        return disponibilidadRepository.findByMedico(medico);
    }

    public Disponibilidad actualizarDisponibilidad(Long id, Disponibilidad disponibilidad) {
        return disponibilidadRepository.findById(id).map(disponibilidadExistente -> {
            disponibilidad.setId(id);
            if (disponibilidad.getMedico() == null) {
                disponibilidad.setMedico(disponibilidadExistente.getMedico());
            } else if (disponibilidad.getMedico().getIdMedico() != null) {
                disponibilidad.setMedico(medicoRepository.findById(disponibilidad.getMedico().getIdMedico())
                        .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado")));
            }
            completarEspecialidad(disponibilidad);
            validarDisponibilidad(disponibilidad, id);
            return disponibilidadRepository.save(disponibilidad);
        }).orElseThrow(() -> new IllegalArgumentException("Disponibilidad no encontrada"));
    }

    public void eliminarDisponibilidad(Long id) {
        if (!disponibilidadRepository.existsById(id)) {
            throw new IllegalArgumentException("Disponibilidad no encontrada");
        }
        disponibilidadRepository.deleteById(id);
    }

    public void validarDisponibilidadesSinSolapamientos(List<Disponibilidad> disponibilidades) {
        if (disponibilidades == null) {
            return;
        }

        for (int i = 0; i < disponibilidades.size(); i++) {
            Disponibilidad actual = disponibilidades.get(i);
            completarEspecialidad(actual);
            validarCampos(actual, false);

            for (int j = i + 1; j < disponibilidades.size(); j++) {
                Disponibilidad otra = disponibilidades.get(j);
                completarEspecialidad(otra);
                validarCampos(otra, false);
                if (actual.getDia() == otra.getDia() && seSolapan(actual, otra)) {
                    throw new IllegalArgumentException("Ya existe disponibilidad para ese dia y rango horario");
                }
            }
        }
    }

    private void validarDisponibilidad(Disponibilidad disponibilidad, Long idIgnorado) {
        validarCampos(disponibilidad, true);

        Long medicoId = disponibilidad.getMedico().getIdMedico();
        disponibilidadRepository.findByMedico_IdMedicoAndDia(medicoId, disponibilidad.getDia())
                .stream()
                .filter(existente -> idIgnorado == null || !idIgnorado.equals(existente.getId()))
                .filter(existente -> seSolapan(disponibilidad, existente))
                .findFirst()
                .ifPresent(existente -> {
                    throw new IllegalArgumentException("Ya existe disponibilidad para ese dia y rango horario");
                });
    }

    private void validarCampos(Disponibilidad disponibilidad, boolean requiereMedico) {
        if (disponibilidad == null || (requiereMedico && disponibilidad.getMedico() == null) || disponibilidad.getDia() == null
                || disponibilidad.getHoraInicio() == null || disponibilidad.getHoraFin() == null
                || disponibilidad.getDuracionTurno() == null) {
            throw new IllegalArgumentException("Complete medico, dia, horarios y duracion");
        }

        if (!disponibilidad.getHoraInicio().isBefore(disponibilidad.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        if (disponibilidad.getDuracionTurno() <= 0) {
            throw new IllegalArgumentException("La duracion del turno debe ser mayor a cero");
        }

        long minutosDisponibles = Duration.between(disponibilidad.getHoraInicio(), disponibilidad.getHoraFin()).toMinutes();
        if (disponibilidad.getDuracionTurno() > minutosDisponibles) {
            throw new IllegalArgumentException("La duracion del turno no puede superar el rango horario");
        }

        validarEspecialidad(disponibilidad);
    }

    private boolean seSolapan(Disponibilidad primera, Disponibilidad segunda) {
        return primera.getHoraInicio().isBefore(segunda.getHoraFin())
                && primera.getHoraFin().isAfter(segunda.getHoraInicio());
    }

    private void completarEspecialidad(Disponibilidad disponibilidad) {
        if (disponibilidad == null || disponibilidad.getMedico() == null) {
            return;
        }
        if (disponibilidad.getEspecialidad() == null || disponibilidad.getEspecialidad().isBlank()) {
            disponibilidad.setEspecialidad(disponibilidad.getMedico().getEspecialidades().stream().findFirst().orElse(null));
        }
    }

    private void validarEspecialidad(Disponibilidad disponibilidad) {
        String especialidad = disponibilidad.getEspecialidad() == null ? "" : disponibilidad.getEspecialidad().trim().replaceAll("\\s+", " ");
        if (especialidad.isBlank()) {
            throw new IllegalArgumentException("Seleccione la especialidad de la disponibilidad");
        }
        if (disponibilidad.getMedico() != null && disponibilidad.getMedico().getEspecialidades().stream().noneMatch(item -> item.equalsIgnoreCase(especialidad))) {
            throw new IllegalArgumentException("La especialidad de la disponibilidad debe pertenecer al medico");
        }
        disponibilidad.setEspecialidad(especialidad);
    }
}
