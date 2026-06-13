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
import proyecto_laboS.labo.controller.dto.ApiDtos.PacienteDto;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.service.PacienteService;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @GetMapping
    public List<PacienteDto> listarPacientes() {
        return pacienteService.listarPacientes().stream().map(ApiDtos::toDto).toList();
    }

    @GetMapping("/{id}")
    public PacienteDto obtenerPaciente(@PathVariable Long id) {
        return ApiDtos.toDto(pacienteService.obtenerPacientePorId(id));
    }

    @PostMapping
    public PacienteDto crearPaciente(@RequestBody Paciente paciente) {
        return ApiDtos.toDto(pacienteService.guardarPaciente(paciente));
    }

    @PutMapping("/{id}")
    public PacienteDto actualizarPaciente(@PathVariable Long id, @RequestBody Paciente paciente) {
        return ApiDtos.toDto(pacienteService.actualizarPaciente(id, paciente));
    }

    @DeleteMapping("/{id}")
    public void eliminarPaciente(@PathVariable Long id) {
        pacienteService.eliminarPaciente(id);
    }
}
