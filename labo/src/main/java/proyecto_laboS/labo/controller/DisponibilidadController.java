package proyecto_laboS.labo.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.service.DisponibilidadService;


@RestController
@RequestMapping("/disponibilidad")
@CrossOrigin(origins = "*")
public class DisponibilidadController {

    @Autowired
    private DisponibilidadService disponibilidadService;

    @PostMapping("/crear/{medicoId}")
    public Disponibilidad crearDisponibilidad(
            @PathVariable Long medicoId,
            @RequestBody Disponibilidad disponibilidad) {

        return disponibilidadService.crearDisponibilidad(medicoId, disponibilidad);
    }

    @GetMapping("/medico/{medicoId}")
    public List<Disponibilidad> obtenerPorMedico(@PathVariable Long medicoId) {
        return disponibilidadService.obtenerPorMedico(medicoId);
    }
}
