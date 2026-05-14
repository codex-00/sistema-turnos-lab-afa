package proyecto_laboS.labo.service;

import proyecto_laboS.labo.model.Paciente;
import java.util.List;

public interface PacienteService {
    
    List<Paciente> listarPacientes();
    Paciente guardarPaciente(Paciente paciente);
    
    
}
