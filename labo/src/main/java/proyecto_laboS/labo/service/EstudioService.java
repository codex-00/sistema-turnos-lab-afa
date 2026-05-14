package proyecto_laboS.labo.service;

import java.util.List;
import proyecto_laboS.labo.model.Paciente;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.model.Turno;

public interface EstudioService {

    List<Estudio> listarEstudios();
    Estudio crearEstudio(Estudio estudio);
    Estudio obtenerEstudioPorId(Long id);
    void cancelarEstudio(Long id);
    List<Estudio> listarEstudiosPorPaciente(Long idPaciente);
    
}
