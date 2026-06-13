package proyecto_laboS.labo.controller.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.model.Turno;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record DisponibilidadDto(
            Long id,
            DayOfWeek dia,
            LocalTime horaInicio,
            LocalTime horaFin,
            Integer duracionTurno,
            String especialidad) {
    }

    public record MedicoDto(
            Long idMedico,
            String dni,
            String nombre,
            String apellido,
            String email,
            String direccion,
            String telefono,
            LocalDateTime fechaRegistro,
            String especialidad,
            List<String> especialidades,
            String estado,
            List<DisponibilidadDto> disponibilidades) {
    }

    public record PacienteDto(
            Long idPaciente,
            String dni,
            String nombre,
            String apellido,
            String email,
            String direccion,
            String telefono,
            LocalDateTime fechaRegistro,
            String estado) {
    }

    public record TurnoDto(
            Long idTurno,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaDeTurno,
            String estado,
            MedicoDto medico,
            PacienteDto paciente) {
    }

    public record AgendaDto(
            Long id,
            MedicoDto medico,
            PacienteDto paciente,
            LocalDate fecha,
            LocalTime hora,
            Boolean disponible,
            String descripcion,
            String estado) {
    }

    public record EstudioDto(
            Long idEstudio,
            PacienteDto paciente,
            MedicoDto medico,
            String nombre,
            String descripcion,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaDeTurno,
            String nombreArchivo,
            String tipoArchivo,
            Long tamanoArchivo) {
    }

    public static DisponibilidadDto toDto(Disponibilidad disponibilidad) {
        if (disponibilidad == null) {
            return null;
        }
        return new DisponibilidadDto(
                disponibilidad.getId(),
                disponibilidad.getDia(),
                disponibilidad.getHoraInicio(),
                disponibilidad.getHoraFin(),
                disponibilidad.getDuracionTurno(),
                disponibilidad.getEspecialidad());
    }

    public static MedicoDto toDto(Medico medico) {
        if (medico == null) {
            return null;
        }
        List<DisponibilidadDto> disponibilidades = medico.getDisponibilidades() == null
                ? List.of()
                : medico.getDisponibilidades().stream().map(ApiDtos::toDto).toList();
        return new MedicoDto(
                medico.getIdMedico(),
                medico.getDni(),
                medico.getNombre(),
                medico.getApellido(),
                medico.getEmail(),
                medico.getDireccion(),
                medico.getTelefono(),
                medico.getFechaRegistro(),
                medico.getEspecialidad(),
                medico.getEspecialidades(),
                medico.getEstado(),
                disponibilidades);
    }

    public static PacienteDto toDto(Paciente paciente) {
        if (paciente == null) {
            return null;
        }
        return new PacienteDto(
                paciente.getIdPaciente(),
                paciente.getDni(),
                paciente.getNombre(),
                paciente.getApellido(),
                paciente.getEmail(),
                paciente.getDireccion(),
                paciente.getTelefono(),
                paciente.getFechaRegistro(),
                paciente.getEstado());
    }

    public static TurnoDto toDto(Turno turno) {
        if (turno == null) {
            return null;
        }
        return new TurnoDto(
                turno.getIdTurno(),
                turno.getFechaCreacion(),
                turno.getFechaDeTurno(),
                turno.getEstado(),
                toDto(turno.getMedico()),
                toDto(turno.getPaciente()));
    }

    public static AgendaDto toDto(Agenda agenda) {
        if (agenda == null) {
            return null;
        }
        return new AgendaDto(
                agenda.getId(),
                toDto(agenda.getMedico()),
                toDto(agenda.getPaciente()),
                agenda.getFecha(),
                agenda.getHora(),
                agenda.getDisponible(),
                agenda.getDescripcion(),
                agenda.getEstado());
    }

    public static EstudioDto toDto(Estudio estudio) {
        if (estudio == null) {
            return null;
        }
        return new EstudioDto(
                estudio.getIdEstudio(),
                toDto(estudio.getPaciente()),
                toDto(estudio.getMedico()),
                estudio.getNombre(),
                estudio.getDescripcion(),
                estudio.getFechaCreacion(),
                estudio.getFechaDeTurno(),
                estudio.getNombreArchivo(),
                estudio.getTipoArchivo(),
                estudio.getTamanoArchivo());
    }
}
