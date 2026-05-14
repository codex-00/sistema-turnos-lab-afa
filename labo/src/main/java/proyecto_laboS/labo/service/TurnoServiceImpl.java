package proyecto_laboS.labo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import proyecto_laboS.labo.model.Turno;
import proyecto_laboS.labo.repository.TurnoRepository;

@Service
public class TurnoServiceImpl implements TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    
    @Override
    public Turno crearTurno(Turno turno) {
         return turnoRepository.save(turno);
    }

    @Override
    public Turno obtenerTurnoPorId(Long id) {
        return turnoRepository.findById(id).orElse(null);
    }

    @Override
    public void cancelarTurno(Long id) {
        turnoRepository.deleteById(id);
    }

    @Override
    public List<Turno> listarTurnos() {
        return turnoRepository.findAll();
    }

    @Override
    public List<Turno> listarTurnosPorMedico(Long medico) {
        return turnoRepository.findByMedicoIdMedico(medico);
    }   
    
    @Override
    public List<Turno> listarTurnosPorPaciente(Long paciente) {
        return turnoRepository.findByPacienteIdPaciente(paciente);
    }
}
