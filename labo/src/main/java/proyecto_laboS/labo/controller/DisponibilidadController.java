package proyecto_laboS.labo.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.controller.dto.ApiDtos;
import proyecto_laboS.labo.controller.dto.ApiDtos.DisponibilidadDto;
import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.service.DisponibilidadService;


@RestController
@RequestMapping("/disponibilidad")
public class DisponibilidadController {

    @Autowired
    private DisponibilidadService disponibilidadService;

    @PostMapping("/crear/{medicoId}")
    public DisponibilidadDto crearDisponibilidad(
            @PathVariable Long medicoId,
            @RequestBody Disponibilidad disponibilidad) {

        return ApiDtos.toDto(disponibilidadService.crearDisponibilidad(medicoId, disponibilidad));
    }

    @GetMapping("/medico/{medicoId}")
    public List<DisponibilidadDto> obtenerPorMedico(@PathVariable Long medicoId) {
        return disponibilidadService.obtenerPorMedico(medicoId).stream().map(ApiDtos::toDto).toList();
    }

    @PutMapping("/{id}")
    public DisponibilidadDto actualizarDisponibilidad(@PathVariable Long id, @RequestBody Disponibilidad disponibilidad) {
        return ApiDtos.toDto(disponibilidadService.actualizarDisponibilidad(id, disponibilidad));
    }

    @DeleteMapping("/{id}")
    public void eliminarDisponibilidad(@PathVariable Long id) {
        disponibilidadService.eliminarDisponibilidad(id);
    }
}
