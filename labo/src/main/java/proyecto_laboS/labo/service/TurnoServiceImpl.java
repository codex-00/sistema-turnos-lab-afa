package proyecto_laboS.labo.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.EstadoTurno;
import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.repository.AgendaRepository;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.TurnoRepository;

@Service
public class TurnoServiceImpl implements TurnoService {

    private static final Set<String> ESTADOS_LIBERAN_AGENDA = Set.of(
            EstadoTurno.CANCELADO.name());

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Override
    @Transactional
    public Turno crearTurno(Turno turno) {
        if (turno.getFechaCreacion() == null) {
            turno.setFechaCreacion(LocalDateTime.now());
        }
        turno.setEstado(EstadoTurno.normalizar(turno.getEstado()));

        if (!horarioDentroDeDisponibilidad(turno)) {
            throw new IllegalArgumentException("Horario fuera de la disponibilidad del medico");
        }
        validarHorarioSinTurnoActivo(turno, null);

        Turno turnoGuardado = turnoRepository.save(turno);
        ocuparAgendaDelTurno(turnoGuardado);
        return turnoGuardado;
    }

    @Override
    public Turno obtenerTurnoPorId(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
    }

    @Override
    @Transactional
    public Turno actualizarTurno(Long id, Turno turno) {
        return turnoRepository.findById(id).map(turnoExistente -> {
            if (estaRealizado(turnoExistente.getEstado()) || estaCancelado(turnoExistente.getEstado())) {
                throw new IllegalArgumentException("No se puede reprogramar un turno " + turnoExistente.getEstado().toLowerCase());
            }
            if (estaPerdido(turnoExistente)) {
                throw new IllegalArgumentException("No se puede reprogramar un turno perdido");
            }
            turno.setIdTurno(id);
            if (turno.getFechaCreacion() == null) {
                turno.setFechaCreacion(turnoExistente.getFechaCreacion());
            }
            if (turno.getEstado() == null || turno.getEstado().isBlank()) {
                turno.setEstado(turnoExistente.getEstado());
            }
            turno.setEstado(EstadoTurno.normalizar(turno.getEstado()));

            if (!horarioDentroDeDisponibilidad(turno)) {
                throw new IllegalArgumentException("Horario fuera de la disponibilidad del medico");
            }
            validarHorarioSinTurnoActivo(turno, id);

            liberarAgendaDelTurno(turnoExistente);

            Turno turnoActualizado = turnoRepository.save(turno);
            ocuparAgendaDelTurno(turnoActualizado);
            return turnoActualizado;
        }).orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
    }

    @Override
    @Transactional
    public Turno cambiarEstado(Long id, String estado) {
        return cambiarEstado(id, estado, null);
    }

    @Override
    @Transactional
    public Turno cambiarEstado(Long id, String estado, String rol) {
        String estadoNormalizado = EstadoTurno.normalizar(estado);
        return turnoRepository.findById(id).map(turno -> {
            if (estaCancelado(turno.getEstado()) && !esAdmin(rol)) {
                if (EstadoTurno.REALIZADO.name().equals(estadoNormalizado)) {
                    throw new IllegalArgumentException("No se puede aprobar un turno cancelado");
                }
                throw new IllegalArgumentException("No se puede modificar un turno cancelado");
            }
            if (estaRealizado(turno.getEstado())) {
                if (estaRealizado(estadoNormalizado)) {
                    throw new IllegalArgumentException("El turno ya ha sido aprobado");
                }
                if (EstadoTurno.CANCELADO.name().equals(estadoNormalizado)) {
                    throw new IllegalArgumentException("No se puede cancelar un turno realizado");
                }
                throw new IllegalArgumentException("No se puede modificar un turno realizado");
            }
            if (estaPerdido(turno)) {
                if (EstadoTurno.REALIZADO.name().equals(estadoNormalizado)) {
                    throw new IllegalArgumentException("No se puede aprobar un turno perdido");
                }
                if (EstadoTurno.CANCELADO.name().equals(estadoNormalizado)) {
                    throw new IllegalArgumentException("No se puede cancelar un turno perdido");
                }
                throw new IllegalArgumentException("No se puede modificar un turno perdido");
            }
            turno.setEstado(estadoNormalizado);
            if (liberaAgenda(estadoNormalizado)) {
                liberarAgendaDelTurno(turno);
            } else {
                validarHorarioSinTurnoActivo(turno, turno.getIdTurno());
                ocuparAgendaDelTurno(turno);
            }
            return turnoRepository.save(turno);
        }).orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));
    }

    @Override
    @Transactional
    public void cancelarTurno(Long id) {
        cancelarTurno(id, null);
    }

    @Override
    @Transactional
    public void cancelarTurno(Long id, String rol) {
        turnoRepository.findById(id).ifPresent(turno -> {
            if (estaRealizado(turno.getEstado())) {
                throw new IllegalArgumentException("No se puede cancelar un turno realizado");
            }
            if (estaPerdido(turno)) {
                throw new IllegalArgumentException("No se puede cancelar un turno perdido");
            }
            liberarAgendaDelTurno(turno);
            turno.setEstado(EstadoTurno.CANCELADO.name());
            turnoRepository.save(turno);
        });
    }

    @Override
    public List<Turno> listarTurnos() {
        return turnoRepository.findAll();
    }

    @Override
    public List<Turno> listarTurnosPorMedico(Long medico) {
        return turnoRepository.findByMedicoIdMedico(medico);
    }

    @Override
    public List<Turno> listarTurnosPorPaciente(Long paciente) {
        return turnoRepository.findByPacienteIdPaciente(paciente);
    }

    private boolean liberaAgenda(String estado) {
        return ESTADOS_LIBERAN_AGENDA.contains(EstadoTurno.normalizar(estado));
    }

    private boolean estaRealizado(String estado) {
        return EstadoTurno.REALIZADO.name().equals(EstadoTurno.normalizar(estado));
    }

    private boolean estaCancelado(String estado) {
        return EstadoTurno.CANCELADO.name().equals(EstadoTurno.normalizar(estado));
    }

    private boolean estaPerdido(Turno turno) {
        if (turno == null || turno.getFechaDeTurno() == null) {
            return false;
        }
        return !estaRealizado(turno.getEstado())
                && !estaCancelado(turno.getEstado())
                && turno.getFechaDeTurno().isBefore(LocalDateTime.now());
    }

    private boolean esAdmin(String rol) {
        return rol != null && rol.equalsIgnoreCase("admin");
    }

    private void liberarAgendaDelTurno(Turno turno) {
        if (turno.getMedico() == null || turno.getFechaDeTurno() == null) {
            return;
        }

        agendaRepository
                .findFirstByMedico_IdMedicoAndFechaAndHoraOrderByIdAsc(
                        turno.getMedico().getIdMedico(),
                        turno.getFechaDeTurno().toLocalDate(),
                        turno.getFechaDeTurno().toLocalTime())
                .ifPresent(agenda -> {
                    agenda.setPaciente(null);
                    agenda.setDisponible(true);
                    agenda.setEstado(null);
                    agenda.setDescripcion(null);
                    agendaRepository.save(agenda);
                });
    }

    private void ocuparAgendaDelTurno(Turno turno) {
        if (turno.getMedico() == null || turno.getFechaDeTurno() == null || liberaAgenda(turno.getEstado())) {
            return;
        }

        Agenda agenda = agendaRepository
                .findFirstByMedico_IdMedicoAndFechaAndHoraOrderByIdAsc(
                        turno.getMedico().getIdMedico(),
                        turno.getFechaDeTurno().toLocalDate(),
                        turno.getFechaDeTurno().toLocalTime())
                .orElseGet(() -> {
                    Agenda nuevaAgenda = new Agenda();
                    nuevaAgenda.setMedico(turno.getMedico());
                    nuevaAgenda.setFecha(turno.getFechaDeTurno().toLocalDate());
                    nuevaAgenda.setHora(turno.getFechaDeTurno().toLocalTime());
                    return nuevaAgenda;
                });

        if (Boolean.FALSE.equals(agenda.getDisponible()) && !perteneceAlMismoPaciente(agenda, turno)) {
            throw new IllegalArgumentException("El horario seleccionado no esta disponible");
        }

        agenda.setPaciente(turno.getPaciente());
        agenda.setDisponible(false);
        agenda.setDescripcion(null);
        agenda.setEstado(turno.getEstado());
        agendaRepository.save(agenda);
    }

    private boolean perteneceAlMismoPaciente(Agenda agenda, Turno turno) {
        if (agenda.getPaciente() == null || turno.getPaciente() == null) {
            return false;
        }
        return agenda.getPaciente().getIdPaciente().equals(turno.getPaciente().getIdPaciente());
    }

    private boolean horarioDentroDeDisponibilidad(Turno turno) {
        if (turno.getMedico() == null || turno.getFechaDeTurno() == null) {
            return true;
        }

        List<Disponibilidad> disponibilidades = disponibilidadRepository.findByMedico_IdMedicoAndDia(
                turno.getMedico().getIdMedico(),
                turno.getFechaDeTurno().getDayOfWeek());

        if (disponibilidades.isEmpty()) {
            return false;
        }

        LocalTime horaTurno = turno.getFechaDeTurno().toLocalTime();

        return disponibilidades.stream().anyMatch(disp -> {
            if (horaTurno.isBefore(disp.getHoraInicio()) || horaTurno.plusMinutes(disp.getDuracionTurno()).isAfter(disp.getHoraFin())) {
                return false;
            }

            long minutosDesdeInicio = Duration.between(disp.getHoraInicio(), horaTurno).toMinutes();
            return minutosDesdeInicio % disp.getDuracionTurno() == 0;
        });
    }

    private void validarHorarioSinTurnoActivo(Turno turno, Long turnoIdPermitido) {
        if (turno.getMedico() == null || turno.getFechaDeTurno() == null || liberaAgenda(turno.getEstado())) {
            return;
        }

        boolean ocupado = turnoRepository
                .findByMedico_IdMedicoAndFechaDeTurnoAndEstadoNotIn(
                        turno.getMedico().getIdMedico(),
                        turno.getFechaDeTurno(),
                        ESTADOS_LIBERAN_AGENDA)
                .stream()
                .anyMatch(turnoExistente -> turnoIdPermitido == null || !turnoIdPermitido.equals(turnoExistente.getIdTurno()));

        if (ocupado) {
            throw new IllegalArgumentException("El horario seleccionado no esta disponible");
        }
    }
}
