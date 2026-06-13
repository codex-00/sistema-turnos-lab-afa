package proyecto_laboS.labo.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByEmail(String email);
    Optional<Paciente> findByDni(String dni);
    Optional<Paciente> findByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);
}
