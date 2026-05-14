package proyecto_laboS.labo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.service.MedicoService;

@RestController
@RequestMapping("/medicos")
@CrossOrigin(origins = "*") //permite que el navegador se pueda comunicar con el backend desde cualquier navegador, va en todos los controllers
public class MedicoController {
    
    @Autowired
    private MedicoService medicoService;

    @GetMapping
    public List<Medico> listarMedicos() {
        return medicoService.listarMedicos();
    }

    @PostMapping 
    public Medico crearMedico(@RequestBody Medico medico) {
        return medicoService.guardarMedico(medico);
    }

    @PutMapping("/{id}")
    public Medico actualizarMedico(@PathVariable Long id, @RequestBody Medico medico) {
        return medicoService.actualizarMedico(id, medico);
    }
    
    @GetMapping("/especialidades")
    public List<Medico> listarEspecialidades(@RequestParam(required = false) String especialidad) {
       if (especialidad == null) {
           return medicoService.listarMedicos(); // o devolver una lista vacía
       }
       return medicoService.buscarPorEspecialidad(especialidad);
    }

    // @GetMapping("/especialidades/{especialidad}")
    // public List<Medico> listarEspecialidades(@PathVariable String especialidad) {
    //     return medicoService.buscarPorEspecialidad(especialidad);
    // }


    //@GetMapping("/nombre")
    //public List<String> listarNombres() {
    //    return medicoService.buscarPorNombre();
    //}   

    //@GetMapping("/id")
    //public List<Medico> obtenerMedicoPorId(@RequestBody Long id> ) {
     //   Long id = body.get(key:"id");
      //  return medicoService.obtenerMedicoPorId(id);    
    //}   



    //@GetMapping("/id/{id}")
    //public Medico obtenerMedicoPorId(@PathVariable Long id) {
    //    return medicoService.obtenerMedicoPorId(id);
    //}
}

