package proyecto_laboS.labo.service;

import java.util.List;

import proyecto_laboS.labo.model.Turno ;

public interface TurnoService {
    
    Turno crearTurno(Turno turno);
    Turno obtenerTurnoPorId(Long id);
    void cancelarTurno(Long id);
    List<Turno> listarTurnos();
    List<Turno> listarTurnosPorMedico(Long idMedico);
    List<Turno> listarTurnosPorPaciente(Long idPaciente);
}
