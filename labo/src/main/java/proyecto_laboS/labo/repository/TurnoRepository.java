package proyecto_laboS.labo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Turno;
import java.util.List;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByMedicoIdMedico(Long idMedico);
    List<Turno> findByPacienteIdPaciente(Long idPaciente);

}