package proyecto_laboS.labo.repository;
import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import proyecto_laboS.labo.model.Disponibilidad;
import proyecto_laboS.labo.model.Medico;



@Repository
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {

    List<Disponibilidad> findByMedico(Medico medico);

    List<Disponibilidad> findByMedicoAndDia(Medico medico, DayOfWeek dia);
}