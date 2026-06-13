package proyecto_laboS.labo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.EstadoTurno;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.repository.AgendaRepository;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.EstudioRepository;
import proyecto_laboS.labo.repository.MedicoRepository;
import proyecto_laboS.labo.repository.PacienteRepository;
import proyecto_laboS.labo.repository.TurnoRepository;

@SpringBootTest
class AgendaTurnosContinuidadTest {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private AgendaRepository agendaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private EstudioRepository estudioRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    private Medico medico;
    private Paciente paciente;
    private LocalDate fecha;

    @BeforeEach
    void setUp() {
        estudioRepository.deleteAll();
        turnoRepository.deleteAll();
        agendaRepository.deleteAll();
        disponibilidadRepository.deleteAll();
        pacienteRepository.deleteAll();
        medicoRepository.deleteAll();

        medico = new Medico();
        medico.setNombre("Laura");
        medico.setApellido("Suarez");
        medico.setDni("30000001");
        medico.setEmail("laura.test@salud.com");
        medico.setPassword("secret");
        medico.setDireccion("Calle 1");
        medico.setTelefono("1111");
        medico.setEspecialidad("Clinico");
        medico = medicoRepository.save(medico);

        paciente = new Paciente();
        paciente.setNombre("Ana");
        paciente.setApellido("Perez");
        paciente.setDni("40000001");
        paciente.setEmail("ana.test@mail.com");
        paciente.setPassword("secret");
        paciente.setDireccion("Calle 2");
        paciente.setTelefono("2222");
        paciente = pacienteRepository.save(paciente);

        fecha = LocalDate.of(2026, 6, 15);

        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setMedico(medico);
        disponibilidad.setDia(DayOfWeek.MONDAY);
        disponibilidad.setHoraInicio(LocalTime.of(8, 0));
        disponibilidad.setHoraFin(LocalTime.of(10, 0));
        disponibilidad.setDuracionTurno(30);
        disponibilidad.setEspecialidad("Clinico");
        disponibilidadRepository.save(disponibilidad);
    }

    @Test
    void reservarOcupaYCancelarLiberaElHorario() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));

        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());

        assertThat(horariosDisponibles()).doesNotContain(LocalTime.of(8, 0));

        turnoService.cancelarTurno(turno.getIdTurno());

        assertThat(horariosDisponibles()).contains(LocalTime.of(8, 0));
    }

    @Test
    void reprogramarLiberaHorarioAnteriorYOcupaHorarioNuevo() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());

        Agenda ochoTreinta = buscarDisponible(LocalTime.of(8, 30));

        agendaService.reprogramarTurno(turno.getIdTurno(), ochoTreinta.getId());

        assertThat(horariosDisponibles()).contains(LocalTime.of(8, 0));
        assertThat(horariosDisponibles()).doesNotContain(LocalTime.of(8, 30));
    }

    @Test
    void aprobarTurnoOcupadoPorSiMismoNoFallaYSegundoAprobarInformaQueYaFueAprobado() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());

        Turno aprobado = turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.REALIZADO.name());

        assertThat(aprobado.getEstado()).isEqualTo(EstadoTurno.REALIZADO.name());
        assertThat(horariosDisponibles()).doesNotContain(LocalTime.of(8, 0));
        assertThatThrownBy(() -> turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.REALIZADO.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El turno ya ha sido aprobado");
    }

    @Test
    void turnoAprobadoNoSePuedeCancelarNiReprogramar() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());
        turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.REALIZADO.name());

        Agenda ochoTreinta = buscarDisponible(LocalTime.of(8, 30));

        assertThatThrownBy(() -> turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.CANCELADO.name()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede cancelar un turno realizado");
        assertThatThrownBy(() -> agendaService.reprogramarTurno(turno.getIdTurno(), ochoTreinta.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede reprogramar un turno realizado");
    }

    @Test
    void turnoCanceladoNoSePuedeReprogramar() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());
        turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.CANCELADO.name());

        Agenda ochoTreinta = buscarDisponible(LocalTime.of(8, 30));

        assertThatThrownBy(() -> agendaService.reprogramarTurno(turno.getIdTurno(), ochoTreinta.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede reprogramar un turno cancelado");
    }

    @Test
    void administradorPuedeReprogramarTurnoCancelado() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());
        turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.CANCELADO.name());

        Agenda ochoTreinta = buscarDisponible(LocalTime.of(8, 30));

        Turno reprogramado = agendaService.reprogramarTurno(turno.getIdTurno(), ochoTreinta.getId(), "admin");

        assertThat(reprogramado.getEstado()).isEqualTo(EstadoTurno.REPROGRAMADO.name());
        assertThat(reprogramado.getFechaDeTurno().toLocalTime()).isEqualTo(LocalTime.of(8, 30));
    }

    @Test
    void disponibilidadNoPuedeTenerInicioPosteriorOIgualAlFin() {
        Disponibilidad invalida = nuevaDisponibilidad(LocalTime.of(12, 0), LocalTime.of(8, 0), 30);

        assertThatThrownBy(() -> disponibilidadService.crearDisponibilidad(medico.getIdMedico(), invalida))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La hora de inicio debe ser anterior a la hora de fin");
    }

    @Test
    void disponibilidadNoPuedeSolaparseEnElMismoDiaPeroPuedeSerContigua() {
        Disponibilidad solapada = nuevaDisponibilidad(LocalTime.of(9, 0), LocalTime.of(11, 0), 30);

        assertThatThrownBy(() -> disponibilidadService.crearDisponibilidad(medico.getIdMedico(), solapada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe disponibilidad para ese dia y rango horario");

        Disponibilidad contigua = disponibilidadService.crearDisponibilidad(
                medico.getIdMedico(),
                nuevaDisponibilidad(LocalTime.of(10, 0), LocalTime.of(11, 0), 30));

        assertThat(contigua.getId()).isNotNull();
    }

    @Test
    void agendaNoGeneraTurnosQueTerminanDespuesDeLaDisponibilidad() {
        agendaRepository.deleteAll();
        disponibilidadRepository.deleteAll();
        disponibilidadService.crearDisponibilidad(
                medico.getIdMedico(),
                nuevaDisponibilidad(LocalTime.of(8, 0), LocalTime.of(10, 0), 45));

        assertThat(horariosDisponibles()).containsExactly(LocalTime.of(8, 0), LocalTime.of(8, 45));
    }

    @Test
    void soloAdministradorPuedeAprobarTurnoCancelado() {
        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));
        Turno turno = agendaService.reservarTurno(ocho.getId(), paciente.getIdPaciente());
        turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.CANCELADO.name());

        assertThatThrownBy(() -> turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.REALIZADO.name(), "medico"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede aprobar un turno cancelado");

        Turno realizado = turnoService.cambiarEstado(turno.getIdTurno(), EstadoTurno.REALIZADO.name(), "admin");

        assertThat(realizado.getEstado()).isEqualTo(EstadoTurno.REALIZADO.name());
    }

    @Test
    void turnoPerdidoNoPuedeAprobarseNiReprogramarseAunqueSeaAdministrador() {
        Turno turno = new Turno();
        turno.setMedico(medico);
        turno.setPaciente(paciente);
        turno.setFechaCreacion(LocalDateTime.now().minusDays(2));
        turno.setFechaDeTurno(LocalDateTime.now().minusHours(1));
        turno.setEstado(EstadoTurno.PENDIENTE.name());
        turno = turnoRepository.save(turno);

        Agenda ocho = buscarDisponible(LocalTime.of(8, 0));

        Long turnoId = turno.getIdTurno();
        assertThatThrownBy(() -> turnoService.cambiarEstado(turnoId, EstadoTurno.REALIZADO.name(), "medico"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede aprobar un turno perdido");
        assertThatThrownBy(() -> agendaService.reprogramarTurno(turnoId, ocho.getId(), "medico"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede reprogramar un turno perdido");
        assertThatThrownBy(() -> turnoService.cambiarEstado(turnoId, EstadoTurno.REALIZADO.name(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede aprobar un turno perdido");
        assertThatThrownBy(() -> agendaService.reprogramarTurno(turnoId, ocho.getId(), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede reprogramar un turno perdido");
        assertThatThrownBy(() -> turnoService.cancelarTurno(turnoId, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede cancelar un turno perdido");
    }

    @Test
    void pacienteNoPermiteDniONombreApellidoDuplicados() {
        Paciente mismoDni = nuevoPaciente("Maria", "Lopez", paciente.getDni(), "maria.lopez@mail.com");
        assertThatThrownBy(() -> pacienteService.guardarPaciente(mismoDni))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe un paciente con ese DNI");

        Paciente mismoNombre = nuevoPaciente(paciente.getNombre(), paciente.getApellido(), "41000002", "ana.repetida@mail.com");
        assertThatThrownBy(() -> pacienteService.guardarPaciente(mismoNombre))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe un paciente con ese nombre y apellido");
    }

    @Test
    void medicoNoPermiteDatosPersonalesInvalidosNiDniDePaciente() {
        Medico nombreInvalido = nuevoMedico("Juan2", "Ramos", "41000003", "juan.ramos@salud.com");
        assertThatThrownBy(() -> medicoService.guardarMedico(nombreInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre solo puede contener letras y espacios");

        Medico dniPaciente = nuevoMedico("Clara", "Ramos", paciente.getDni(), "clara.ramos@salud.com");
        assertThatThrownBy(() -> medicoService.guardarMedico(dniPaciente))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ya existe un paciente con ese DNI");
    }

    @Test
    void medicoPuedeTenerVariasEspecialidadesYDisponibilidadAsociada() {
        Medico multi = nuevoMedico("Marcela", "Ruiz", "41000004", "marcela.ruiz@salud.com");
        multi.setEspecialidades(List.of("Clinico", "Pediatria"));
        Disponibilidad disponibilidad = nuevaDisponibilidad(LocalTime.of(10, 0), LocalTime.of(11, 0), 30);
        disponibilidad.setEspecialidad("Pediatria");
        multi.setDisponibilidades(List.of(disponibilidad));

        Medico guardado = medicoService.guardarMedico(multi);

        assertThat(guardado.getEspecialidades()).containsExactly("Clinico", "Pediatria");
        assertThat(medicoService.buscarPorEspecialidad("Pediatria"))
                .extracting(Medico::getIdMedico)
                .contains(guardado.getIdMedico());
    }

    private Agenda buscarDisponible(LocalTime hora) {
        return agendaService.obtenerAgenda(medico.getIdMedico(), fecha)
                .stream()
                .filter(agenda -> hora.equals(agenda.getHora()))
                .findFirst()
                .orElseThrow();
    }

    private List<LocalTime> horariosDisponibles() {
        return agendaService.obtenerAgenda(medico.getIdMedico(), fecha)
                .stream()
                .map(Agenda::getHora)
                .toList();
    }

    private Disponibilidad nuevaDisponibilidad(LocalTime inicio, LocalTime fin, int duracion) {
        Disponibilidad disponibilidad = new Disponibilidad();
        disponibilidad.setDia(DayOfWeek.MONDAY);
        disponibilidad.setHoraInicio(inicio);
        disponibilidad.setHoraFin(fin);
        disponibilidad.setDuracionTurno(duracion);
        return disponibilidad;
    }

    private Paciente nuevoPaciente(String nombre, String apellido, String dni, String email) {
        Paciente nuevo = new Paciente();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setDni(dni);
        nuevo.setEmail(email);
        nuevo.setPassword("secret");
        nuevo.setDireccion("Calle 10");
        nuevo.setTelefono("11223344");
        return nuevo;
    }

    private Medico nuevoMedico(String nombre, String apellido, String dni, String email) {
        Medico nuevo = new Medico();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setDni(dni);
        nuevo.setEmail(email);
        nuevo.setPassword("secret");
        nuevo.setDireccion("Calle 20");
        nuevo.setTelefono("44332211");
        nuevo.setEspecialidad("Clinico");
        return nuevo;
    }
}
