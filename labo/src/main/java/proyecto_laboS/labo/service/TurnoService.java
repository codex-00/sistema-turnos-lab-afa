package proyecto_laboS.labo.service;

import java.util.List;

import proyecto_laboS.labo.model.Turno ;

public interface TurnoService {
    
    Turno crearTurno(Turno turno);
    Turno obtenerTurnoPorId(Long id);
    Turno actualizarTurno(Long id, Turno turno);
    Turno cambiarEstado(Long id, String estado);
    Turno cambiarEstado(Long id, String estado, String rol);
    void cancelarTurno(Long id);
    void cancelarTurno(Long id, String rol);
    List<Turno> listarTurnos();
    List<Turno> listarTurnosPorMedico(Long idMedico);
    List<Turno> listarTurnosPorPaciente(Long idPaciente);
}
