package proyecto_laboS.labo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import proyecto_laboS.labo.model.Test;
import proyecto_laboS.labo.service.TestServiceImpl;

// Este controlador maneja la página de inicio de la aplicación
    // Aquí puedes agregar métodos para manejar las solicitudes HTTP relacionadas con la página de inicio

    // Por ejemplo, un método para mostrar la página de inicio
    // @GetMapping("/")
    // public String home(Model model) {
    //     return "home"; // Retorna el nombre de la vista que se debe renderizar
    // }



@RestController("/")
public class HomeController{

    private final TestServiceImpl testService;

    public HomeController(TestServiceImpl testService) {
        this.testService = testService;
    }
    
    @GetMapping("/")
    public String home() {
        return "Bienvenido a la aplicación de gestión de laboratorio";
    }

    @GetMapping("/tests")
    public String home2() {
        return "Bienvenido a la aplicación de gestión de laboratorio";
    }

    @PostMapping("/tests")
    public Test guardar(@RequestBody Test test) {
        return testService.guardar(test);
    }
}
