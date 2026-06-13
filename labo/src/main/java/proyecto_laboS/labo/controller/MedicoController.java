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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.controller.dto.ApiDtos;
import proyecto_laboS.labo.controller.dto.ApiDtos.MedicoDto;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.service.MedicoService;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    @Autowired
    private MedicoService medicoService;

    @GetMapping
    public List<MedicoDto> listarMedicos() {
        return medicoService.listarMedicos().stream().map(ApiDtos::toDto).toList();
    }

    @GetMapping("/{id}")
    public MedicoDto obtenerMedico(@PathVariable Long id) {
        return medicoService.obtenerMedicoPorId(id)
                .map(ApiDtos::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Medico no encontrado"));
    }

    @PostMapping
    public MedicoDto crearMedico(@RequestBody Medico medico) {
        return ApiDtos.toDto(medicoService.guardarMedico(medico));
    }

    @PutMapping("/{id}")
    public MedicoDto actualizarMedico(@PathVariable Long id, @RequestBody Medico medico) {
        return ApiDtos.toDto(medicoService.actualizarMedico(id, medico));
    }

    @DeleteMapping("/{id}")
    public void eliminarMedico(@PathVariable Long id) {
        medicoService.eliminarMedico(id);
    }

    @GetMapping("/especialidades")
    public List<MedicoDto> listarEspecialidades(@RequestParam(required = false) String especialidad) {
        List<Medico> medicos = especialidad == null
                ? medicoService.listarMedicos()
                : medicoService.buscarPorEspecialidad(especialidad);
        return medicos.stream().map(ApiDtos::toDto).toList();
    }
}
