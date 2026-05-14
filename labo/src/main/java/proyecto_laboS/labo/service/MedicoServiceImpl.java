package proyecto_laboS.labo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.MedicoRepository;


@Service
public class MedicoServiceImpl implements MedicoService {

    // Aquí puedes implementar los métodos definidos en la interfaz MedicoService
    // Por ejemplo, si tienes un método para listar médicos, lo implementarías aquí

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }   


    @Override
    public Optional<Medico> obtenerMedicoPorId(Long id) {
        return medicoRepository.findById(id);
    }   
    @Override
    @Transactional
    public Medico guardarMedico(Medico medico) {
        List<Disponibilidad> disponibilidades = medico.getDisponibilidades();
        Medico medicoGuardado = medicoRepository.save(medico);
        guardarDisponibilidades(medicoGuardado, disponibilidades);
        medicoGuardado.setDisponibilidades(null);
        return medicoGuardado;
    }

    @Override
    @Transactional
    public Medico actualizarMedico(Long id, Medico medico) {
        if (medicoRepository.existsById(id)) {
            List<Disponibilidad> disponibilidades = medico.getDisponibilidades();
            medico.setIdMedico(id);
            Medico medicoGuardado = medicoRepository.save(medico);
            if (disponibilidades != null) {
                disponibilidadRepository.deleteAll(disponibilidadRepository.findByMedico(medicoGuardado));
                guardarDisponibilidades(medicoGuardado, disponibilidades);
            }
            medicoGuardado.setDisponibilidades(null);
            return medicoGuardado;
        }
        return null;
    }   

    @Override
    public void eliminarMedico(Long id) {
        medicoRepository.deleteById(id);
    }   
    @Override
    public Optional<Medico> iniciarSesion(String email, String password) {
        return medicoRepository.findByEmailAndPassword(email, password);
    }   
    @Override
    public List<Medico> buscarPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidad(especialidad);
    }

    @Override
    public List<Medico> buscarPorNombre(String nombre) {
        return medicoRepository.findByNombre(nombre);
    }   

    private void guardarDisponibilidades(Medico medico, List<Disponibilidad> disponibilidades) {
        if (disponibilidades == null) {
            return;
        }

        for (Disponibilidad disponibilidad : disponibilidades) {
            disponibilidad.setMedico(medico);
            disponibilidadRepository.save(disponibilidad);
        }
    }
    
}
