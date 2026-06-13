package proyecto_laboS.labo.repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Turno;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByMedicoIdMedico(Long idMedico);
    List<Turno> findByPacienteIdPaciente(Long idPaciente);
    List<Turno> findByMedico_IdMedicoAndFechaDeTurnoBetween(Long idMedico, LocalDateTime inicio, LocalDateTime fin);
    List<Turno> findByMedico_IdMedicoAndFechaDeTurnoAndEstadoNotIn(Long idMedico, LocalDateTime fechaDeTurno, Collection<String> estados);

}
