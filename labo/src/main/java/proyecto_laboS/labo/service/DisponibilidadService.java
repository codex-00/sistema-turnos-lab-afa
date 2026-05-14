package proyecto_laboS.labo.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Medico;
import proyecto_laboS.labo.repository.DisponibilidadRepository;
import proyecto_laboS.labo.repository.MedicoRepository;


@Service
public class DisponibilidadService {

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    public Disponibilidad crearDisponibilidad(Long medicoId, Disponibilidad disponibilidad) {

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Medico no encontrado"));

        disponibilidad.setMedico(medico);

        return disponibilidadRepository.save(disponibilidad);
    }

    public List<Disponibilidad> obtenerPorMedico(Long medicoId) {
        Medico medico = medicoRepository.findById(medicoId).get();
        return disponibilidadRepository.findByMedico(medico);
    }
}
