package proyecto_laboS.labo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Estudio;
import proyecto_laboS.labo.repository.EstudioRepository;


@Service
public class EstudioServiceImpl implements EstudioService {

    // Aquí puedes implementar los métodos definidos en la interfaz EstudioService
    // Por ejemplo, si tienes un repositorio de estudios, puedes inyectarlo aquí

    @Autowired
    private EstudioRepository estudioRepository;

    @Override
    public Estudio crearEstudio(Estudio estudio) {
        return estudioRepository.save(estudio);
    }

    @Override
    public Estudio obtenerEstudioPorId(Long id) {
        return estudioRepository.findById(id).orElse(null);
    }

    @Override
    public void cancelarEstudio(Long id) {
        estudioRepository.deleteById(id);
    }

    @Override
    public List<Estudio> listarEstudios() {
        return estudioRepository.findAll();
    }
    
    @Override
    public List<Estudio> listarEstudiosPorPaciente(Long Paciente) {
        return estudioRepository.findByPacienteIdPaciente(Paciente);
    }
    
    
}
