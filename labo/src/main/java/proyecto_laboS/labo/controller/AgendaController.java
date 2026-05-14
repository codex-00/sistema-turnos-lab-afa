package proyecto_laboS.labo.controller;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.model.Agenda;
import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.service.AgendaService;


@RestController
@RequestMapping("/agenda")
@CrossOrigin(origins = "*")
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    // =========================================
    // OBTENER TURNOS DE UN MEDICO
    // =========================================

    @GetMapping
    public List<Agenda> obtenerAgenda(
            @RequestParam Long medicoId,
            @RequestParam String fecha
    ) {

        return agendaService.obtenerAgenda(
                medicoId,
                LocalDate.parse(fecha)
        );
    }

    @PostMapping("/reservar")
    public Turno reservarTurno(
            @RequestParam Long agendaId,
            @RequestParam Long pacienteId
    ) {
        return agendaService.reservarTurno(agendaId, pacienteId);
    }
}
