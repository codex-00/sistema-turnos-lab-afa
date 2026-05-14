package proyecto_laboS.labo.service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Disponibilidad;
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

    // =========================================
    // OBTENER AGENDA
    // =========================================

    public List<Agenda> obtenerAgenda(Long medicoId, LocalDate fecha) {

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));

        generarAgenda(medico, fecha);

        return agendaRepository.findByMedico_IdMedicoAndFechaAndDisponibleTrue(medicoId, fecha);
    }

    // =========================================
    // GENERAR TURNOS AUTOMATICAMENTE
    // =========================================

    public void generarAgenda(Medico medico, LocalDate fecha) {

        DayOfWeek dia = fecha.getDayOfWeek();

        List<Disponibilidad> disponibilidades =
                disponibilidadRepository.findByMedicoAndDia(medico, dia);

        for (Disponibilidad disp : disponibilidades) {

            LocalTime hora = disp.getHoraInicio();

            while (hora.isBefore(disp.getHoraFin())) {

                boolean existe = agendaRepository
                        .existsByMedico_IdMedicoAndFechaAndHora(
                                medico.getIdMedico(),
                                fecha,
                                hora
                        );

                if (!existe) {

                    Agenda agenda = new Agenda();

                    agenda.setMedico(medico);
                    agenda.setFecha(fecha);
                    agenda.setHora(hora);
                    agenda.setDisponible(true);

                    agendaRepository.save(agenda);
                }

                hora = hora.plusMinutes(disp.getDuracionTurno());
            }
        }
    }

    // =========================================
    // RESERVAR TURNO
    // =========================================

    public Turno reservarTurno(Long agendaId, Long pacienteId) {

        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));

        if (!Boolean.TRUE.equals(agenda.getDisponible())) {
            throw new RuntimeException("El turno seleccionado ya no esta disponible");
        }

        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        agenda.setPaciente(paciente);
        agenda.setDisponible(false);

        agendaRepository.save(agenda);

        Turno turno = new Turno();
        turno.setPaciente(paciente);
        turno.setMedico(agenda.getMedico());
        turno.setFechaCreacion(java.time.LocalDateTime.now());
        turno.setFechaDeTurno(java.time.LocalDateTime.of(agenda.getFecha(), agenda.getHora()));

        return turnoRepository.save(turno);
    }
}
