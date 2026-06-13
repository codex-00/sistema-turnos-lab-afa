package proyecto_laboS.labo.service;

import java.util.List;
import java.util.Optional;

import proyecto_laboS.labo.model.Medico;

public interface MedicoService {

    List<Medico> listarMedicos();
    Optional<Medico> obtenerMedicoPorId(Long id);
    Medico guardarMedico(Medico medico);
    Medico actualizarMedico(Long id, Medico medico);
    void eliminarMedico(Long id);
    List<Medico> buscarPorEspecialidad(String especialidad);
    List<Medico> buscarPorNombre(String nombre);
}
