package proyecto_laboS.labo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.service.EstudioService;

@RestController
@RequestMapping("/estudios")
@CrossOrigin(origins = "*") 
public class EstudioController {
    
    @Autowired
    private EstudioService estudioService;

    @GetMapping
    public List<Estudio> listarEstudios() {
        return estudioService.listarEstudios();
    }

}
