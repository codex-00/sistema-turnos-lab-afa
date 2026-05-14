package proyecto_laboS.labo.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
}
