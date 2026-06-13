package proyecto_laboS.labo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.controller.dto.ApiDtos;
import proyecto_laboS.labo.controller.dto.ApiDtos.AgendaDto;
import proyecto_laboS.labo.controller.dto.ApiDtos.TurnoDto;
import proyecto_laboS.labo.service.AgendaService;

@RestController
@RequestMapping("/agenda")
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @GetMapping
    public List<AgendaDto> obtenerAgenda(
            @RequestParam Long medicoId,
            @RequestParam String fecha,
            @RequestParam(required = false) String especialidad) {
        return agendaService.obtenerAgenda(medicoId, LocalDate.parse(fecha), especialidad)
                .stream().map(ApiDtos::toDto).toList();
    }

    @GetMapping("/dia")
    public List<AgendaDto> obtenerAgendaDia(
            @RequestParam Long medicoId,
            @RequestParam String fecha) {
        return agendaService.obtenerAgendaDia(medicoId, LocalDate.parse(fecha))
                .stream().map(ApiDtos::toDto).toList();
    }

    @GetMapping("/mes")
    public List<AgendaDto> obtenerAgendaMes(
            @RequestParam Long medicoId,
            @RequestParam int anio,
            @RequestParam int mes) {
        return agendaService.obtenerAgendaMes(medicoId, LocalDate.of(anio, mes, 1))
                .stream().map(ApiDtos::toDto).toList();
    }

    @PostMapping("/bloquear")
    public AgendaDto bloquearAgenda(
            @RequestParam Long medicoId,
            @RequestParam String fecha,
            @RequestParam String hora,
            @RequestParam(required = false) String descripcion) {
        return ApiDtos.toDto(agendaService.bloquearAgenda(
                medicoId,
                LocalDate.parse(fecha),
                LocalTime.parse(hora),
                descripcion));
    }

    @PostMapping("/reservar")
    public TurnoDto reservarTurno(
            @RequestParam Long agendaId,
            @RequestParam Long pacienteId) {
        return ApiDtos.toDto(agendaService.reservarTurno(agendaId, pacienteId));
    }

    @PostMapping("/reprogramar")
    public TurnoDto reprogramarTurno(
            @RequestParam Long turnoId,
            @RequestParam Long agendaId,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        return ApiDtos.toDto(agendaService.reprogramarTurno(turnoId, agendaId, rol));
    }
}
