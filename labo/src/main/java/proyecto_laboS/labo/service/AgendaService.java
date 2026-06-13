package proyecto_laboS.labo.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.EstadoTurno;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.repository.AgendaRepository;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;
import proyecto_laboS.labo.repository.TurnoRepository;

@Service
public class AgendaService {

    private static final Set<String> ESTADOS_LIBERAN_AGENDA = Set.of(
            EstadoTurno.CANCELADO.name());

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    public List<Agenda> obtenerAgenda(Long medicoId, LocalDate fecha) {
        return obtenerAgenda(medicoId, fecha, null);
    }

    public List<Agenda> obtenerAgenda(Long medicoId, LocalDate fecha, String especialidad) {
        Medico medico = buscarMedico(medicoId);
        generarAgenda(medico, fecha);
        sincronizarTurnosDelDia(medico, fecha);
        return agendaRepository.findByMedico_IdMedicoAndFechaAndDisponibleTrueOrderByHoraAsc(medicoId, fecha)
                .stream()
                .filter(agenda -> perteneceAEspecialidad(medico, agenda, especialidad))
                .toList();
    }

    public List<Agenda> obtenerAgendaDia(Long medicoId, LocalDate fecha) {
        Medico medico = buscarMedico(medicoId);
        generarAgenda(medico, fecha);
        sincronizarTurnosDelDia(medico, fecha);
        return agendaRepository.findByMedico_IdMedicoAndFechaOrderByHoraAsc(medicoId, fecha);
    }

    public List<Agenda> obtenerAgendaMes(Long medicoId, LocalDate primerDiaDelMes) {
        Medico medico = buscarMedico(medicoId);
        LocalDate ultimoDiaDelMes = primerDiaDelMes.withDayOfMonth(primerDiaDelMes.lengthOfMonth());

        for (LocalDate fecha = primerDiaDelMes; !fecha.isAfter(ultimoDiaDelMes); fecha = fecha.plusDays(1)) {
            generarAgenda(medico, fecha);
            sincronizarTurnosDelDia(medico, fecha);
        }

        return agendaRepository.findByMedico_IdMedicoAndFechaBetweenOrderByFechaAscHoraAsc(
                medicoId,
                primerDiaDelMes,
                ultimoDiaDelMes);
    }

    public Agenda bloquearAgenda(Long medicoId, LocalDate fecha, LocalTime hora, String descripcion) {
        Medico medico = buscarMedico(medicoId);

        if (agendaRepository.existsByMedico_IdMedicoAndFechaAndHora(medicoId, fecha, hora)) {
            throw new IllegalArgumentException("Ya existe un evento o turno para ese horario");
        }

        Agenda agenda = new Agenda();
        agenda.setMedico(medico);
        agenda.setFecha(fecha);
        agenda.setHora(hora);
        agenda.setDisponible(false);
        agenda.setDescripcion(descripcion);
        agenda.setEstado("BLOQUEADO");

        return agendaRepository.save(agenda);
    }

    private void sincronizarTurnosDelDia(Medico medico, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        List<Turno> turnos = turnoRepository.findByMedico_IdMedicoAndFechaDeTurnoBetween(
                medico.getIdMedico(), inicio, fin);
        Set<LocalTime> horariosOcupados = new HashSet<>();

        for (Turno turno : turnos) {
            if (!liberaAgenda(turno.getEstado()) && turno.getFechaDeTurno() != null) {
                horariosOcupados.add(turno.getFechaDeTurno().toLocalTime());
            }
        }

        agendaRepository.findByMedico_IdMedicoAndFecha(medico.getIdMedico(), fecha)
                .stream()
                .filter(agenda -> !Boolean.TRUE.equals(agenda.getDisponible()))
                .filter(agenda -> !"BLOQUEADO".equalsIgnoreCase(agenda.getEstado()))
                .filter(agenda -> !horariosOcupados.contains(agenda.getHora()))
                .forEach(agenda -> {
                    agenda.setPaciente(null);
                    agenda.setDisponible(true);
                    agenda.setEstado(null);
                    agenda.setDescripcion(null);
                    agendaRepository.save(agenda);
                });

        for (Turno turno : turnos) {
            if (liberaAgenda(turno.getEstado())) {
                continue;
            }

            LocalTime horaTurno = turno.getFechaDeTurno().toLocalTime();
            Agenda agenda = agendaRepository.findByMedico_IdMedicoAndFechaAndHora(
                    medico.getIdMedico(), fecha, horaTurno)
                    .orElseGet(() -> {
                        Agenda nueva = new Agenda();
                        nueva.setMedico(medico);
                        nueva.setFecha(fecha);
                        nueva.setHora(horaTurno);
                        return nueva;
                    });

            agenda.setPaciente(turno.getPaciente());
            agenda.setDisponible(false);
            agenda.setEstado(turno.getEstado());
            try {
                agendaRepository.save(agenda);
            } catch (DataIntegrityViolationException ignored) {
                // Otro request pudo generar el mismo horario entre la lectura y el guardado.
            }
        }
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

    public synchronized void generarAgenda(Medico medico, LocalDate fecha) {
        DayOfWeek dia = fecha.getDayOfWeek();
        List<Disponibilidad> disponibilidades = disponibilidadRepository.findByMedicoAndDia(medico, dia);

        for (Disponibilidad disp : disponibilidades) {
            LocalTime hora = disp.getHoraInicio();

            while (!hora.plusMinutes(disp.getDuracionTurno()).isAfter(disp.getHoraFin())) {
                boolean existe = agendaRepository.existsByMedico_IdMedicoAndFechaAndHora(
                        medico.getIdMedico(),
                        fecha,
                        hora);

                if (!existe) {
                    Agenda agenda = new Agenda();
                    agenda.setMedico(medico);
                    agenda.setFecha(fecha);
                    agenda.setHora(hora);
                    agenda.setDisponible(true);

                    try {
                        agendaRepository.saveAndFlush(agenda);
                    } catch (DataIntegrityViolationException ignored) {
                        // Otro request pudo crear el mismo slot simultaneamente.
                    }
                }

                hora = hora.plusMinutes(disp.getDuracionTurno());
            }
        }
    }

    private boolean perteneceAEspecialidad(Medico medico, Agenda agenda, String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            return true;
        }
        String normalizada = especialidad.trim();
        return disponibilidadRepository.findByMedicoAndDia(medico, agenda.getFecha().getDayOfWeek())
                .stream()
                .filter(disponibilidad -> disponibilidad.getEspecialidad() != null && disponibilidad.getEspecialidad().equalsIgnoreCase(normalizada))
                .anyMatch(disponibilidad -> !agenda.getHora().isBefore(disponibilidad.getHoraInicio())
                        && !agenda.getHora().plusMinutes(disponibilidad.getDuracionTurno()).isAfter(disponibilidad.getHoraFin()));
    }

    @Transactional
    public Turno reservarTurno(Long agendaId, Long pacienteId) {
        Agenda agenda = agendaRepository.findByIdForUpdate(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));

        if (!Boolean.TRUE.equals(agenda.getDisponible())) {
            throw new IllegalArgumentException("El turno seleccionado ya no esta disponible");
        }

        validarHorarioSinTurnoActivo(
                agenda.getMedico().getIdMedico(),
                LocalDateTime.of(agenda.getFecha(), agenda.getHora()),
                null);

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        agenda.setPaciente(paciente);
        agenda.setDisponible(false);
        agenda.setDescripcion(null);
        agenda.setEstado(EstadoTurno.PENDIENTE.name());
        agendaRepository.save(agenda);

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setMedico(agenda.getMedico());
        turno.setFechaCreacion(LocalDateTime.now());
        turno.setFechaDeTurno(LocalDateTime.of(agenda.getFecha(), agenda.getHora()));
        turno.setEstado(EstadoTurno.PENDIENTE.name());

        return turnoRepository.save(turno);
    }

    @Transactional
    public Turno reprogramarTurno(Long turnoId, Long agendaId) {
        return reprogramarTurno(turnoId, agendaId, null);
    }

    @Transactional
    public Turno reprogramarTurno(Long turnoId, Long agendaId, String rol) {
        Turno turno = turnoRepository.findById(turnoId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));

        if (estaRealizado(turno.getEstado())) {
            throw new IllegalArgumentException("No se puede reprogramar un turno realizado");
        }
        if (estaCancelado(turno.getEstado()) && !esAdmin(rol)) {
            throw new IllegalArgumentException("No se puede reprogramar un turno cancelado");
        }
        if (estaPerdido(turno)) {
            throw new IllegalArgumentException("No se puede reprogramar un turno perdido");
        }

        Agenda nuevaAgenda = agendaRepository.findByIdForUpdate(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Turno no encontrado"));

        if (!Boolean.TRUE.equals(nuevaAgenda.getDisponible())) {
            throw new IllegalArgumentException("El turno seleccionado ya no esta disponible");
        }

        validarHorarioSinTurnoActivo(
                nuevaAgenda.getMedico().getIdMedico(),
                LocalDateTime.of(nuevaAgenda.getFecha(), nuevaAgenda.getHora()),
                turno.getIdTurno());

        if (turno.getFechaDeTurno() != null && turno.getMedico() != null) {
            agendaRepository
                    .findFirstByMedico_IdMedicoAndFechaAndHoraOrderByIdAsc(
                            turno.getMedico().getIdMedico(),
                            turno.getFechaDeTurno().toLocalDate(),
                            turno.getFechaDeTurno().toLocalTime())
                    .ifPresent(agendaAnterior -> {
                        agendaAnterior.setPaciente(null);
                        agendaAnterior.setDisponible(true);
                        agendaAnterior.setEstado(null);
                        agendaAnterior.setDescripcion(null);
                        agendaRepository.save(agendaAnterior);
                    });
        }

        nuevaAgenda.setPaciente(turno.getPaciente());
        nuevaAgenda.setDisponible(false);
        nuevaAgenda.setDescripcion(null);
        nuevaAgenda.setEstado(EstadoTurno.REPROGRAMADO.name());
        agendaRepository.save(nuevaAgenda);

        turno.setMedico(nuevaAgenda.getMedico());
        turno.setFechaDeTurno(LocalDateTime.of(nuevaAgenda.getFecha(), nuevaAgenda.getHora()));
        turno.setEstado(EstadoTurno.REPROGRAMADO.name());

        return turnoRepository.save(turno);
    }

    private boolean esAdmin(String rol) {
        return rol != null && rol.equalsIgnoreCase("admin");
    }

    private boolean estaPerdido(Turno turno) {
        if (turno == null || turno.getFechaDeTurno() == null) {
            return false;
        }
        return !estaRealizado(turno.getEstado())
                && !estaCancelado(turno.getEstado())
                && turno.getFechaDeTurno().isBefore(LocalDateTime.now());
    }

    private Medico buscarMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado"));
    }

    private void validarHorarioSinTurnoActivo(Long medicoId, LocalDateTime fechaDeTurno, Long turnoIdPermitido) {
        boolean ocupado = turnoRepository
                .findByMedico_IdMedicoAndFechaDeTurnoAndEstadoNotIn(
                        medicoId,
                        fechaDeTurno,
                        ESTADOS_LIBERAN_AGENDA)
                .stream()
                .anyMatch(turno -> turnoIdPermitido == null || !turnoIdPermitido.equals(turno.getIdTurno()));

        if (ocupado) {
            throw new IllegalArgumentException("El horario seleccionado ya no esta disponible");
        }
    }
}
