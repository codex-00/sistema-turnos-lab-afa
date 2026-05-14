package proyecto_laboS.labo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.service.TurnoService;

@RestController
@RequestMapping("/turnos")
@CrossOrigin(origins = "*") // Permite que el navegador se pueda comunicar con el backend desde cualquier origen
public class TurnoController {
    
    @Autowired
    private TurnoService turnoService;

    // http://localhost:8081/turnos?medico=1
    // http://localhost:8081/turnos?paciente=1
    @GetMapping
    public List<Turno> listarTurnos(@RequestParam(required = false) Long medico, @RequestParam(required = false) Long paciente) {
        if (medico == null && paciente == null) {
            return turnoService.listarTurnos(); // o devolver una lista vacía
        }
        if (medico != null) {
            return turnoService.listarTurnosPorMedico(medico);
        }
        if (paciente != null) {
            return turnoService.listarTurnosPorPaciente(paciente);
        }
        return List.of(); // devolver una lista vacía si no se especifica ni médico ni paciente
    }

    


    // @GetMapping("/especialidades")
    // public List<Medico> listarEspecialidades(@RequestParam(required = false) String especialidad) {
    //    if (especialidad == null) {
    //        return medicoService.listarMedicos(); // o devolver una lista vacía
    //    }
    //    return medicoService.buscarPorEspecialidad(especialidad);
    // }

    @PostMapping 
    public Turno crearTurno(@RequestBody Turno turno) {
        return turnoService.crearTurno(turno);
    }

}
