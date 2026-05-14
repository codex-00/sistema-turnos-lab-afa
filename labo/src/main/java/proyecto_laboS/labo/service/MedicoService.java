package proyecto_laboS.labo.service;

import java.util.List;
import java.util.Optional;
import proyecto_laboS.labo.model.Medico;

public interface MedicoService {
    
    // Aquí puedes definir los métodos que necesites para el servicio de médico
    // Por ejemplo:
    // List<Medico> listarMedicos();
    // Medico obtenerMedicoPorId(Long id);
    // Medico guardarMedico(Medico medico);
    // void eliminarMedico(Long id);

    List<Medico> listarMedicos();
    // Método para obtener todos los médicos
    // Método para obtener un médico por ID
    Optional<Medico> obtenerMedicoPorId(Long id);
    // Método para guardar un médico
    Medico guardarMedico(Medico medico);
    // Método para actualizar un médico
    Medico actualizarMedico(Long id, Medico medico);
    // Método para eliminar un médico por ID
    void eliminarMedico(Long id);
    // Método para iniciar sesión de un médico
    Optional<Medico> iniciarSesion(String email, String password);
    // Método para buscar médicos por especialidad
    List<Medico> buscarPorEspecialidad(String especialidad);
    // Método para buscar médicos por nombre
    List<Medico> buscarPorNombre(String nombre);
}