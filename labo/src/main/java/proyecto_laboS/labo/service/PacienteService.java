package proyecto_laboS.labo.service;

import proyecto_laboS.labo.model.Paciente;
import java.util.List;

public interface PacienteService {
    
    List<Paciente> listarPacientes();
    Paciente obtenerPacientePorId(Long id);
    Paciente guardarPaciente(Paciente paciente);
    Paciente actualizarPaciente(Long id, Paciente paciente);
    void eliminarPaciente(Long id);
    
    
}
