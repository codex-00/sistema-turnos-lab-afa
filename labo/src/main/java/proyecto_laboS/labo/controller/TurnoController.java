package proyecto_laboS.labo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.controller.dto.ApiDtos;
import proyecto_laboS.labo.controller.dto.ApiDtos.TurnoDto;
import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.service.TurnoService;

@RestController
@RequestMapping("/turnos")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @GetMapping
    public List<TurnoDto> listarTurnos(
            @RequestParam(required = false) Long medico,
            @RequestParam(required = false) Long paciente) {
        if (medico != null) {
            return turnoService.listarTurnosPorMedico(medico).stream().map(ApiDtos::toDto).toList();
        }
        if (paciente != null) {
            return turnoService.listarTurnosPorPaciente(paciente).stream().map(ApiDtos::toDto).toList();
        }
        return turnoService.listarTurnos().stream().map(ApiDtos::toDto).toList();
    }

    @PostMapping
    public TurnoDto crearTurno(@RequestBody Turno turno) {
        return ApiDtos.toDto(turnoService.crearTurno(turno));
    }

    @GetMapping("/{id}")
    public TurnoDto obtenerTurno(@PathVariable Long id) {
        return ApiDtos.toDto(turnoService.obtenerTurnoPorId(id));
    }

    @PutMapping("/{id}")
    public TurnoDto actualizarTurno(@PathVariable Long id, @RequestBody Turno turno) {
        return ApiDtos.toDto(turnoService.actualizarTurno(id, turno));
    }

    @PatchMapping("/{id}/estado")
    public TurnoDto cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        return ApiDtos.toDto(turnoService.cambiarEstado(id, estado, rol));
    }

    @DeleteMapping("/{id}")
    public void cancelarTurno(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Rol", required = false) String rol) {
        turnoService.cancelarTurno(id, rol);
    }
}
