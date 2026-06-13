package proyecto_laboS.labo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Medico;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByEmail(String email);
    Optional<Medico> findByDni(String dni);
    Optional<Medico> findByNombreIgnoreCaseAndApellidoIgnoreCase(String nombre, String apellido);
    List<Medico> findByNombre(String nombre);
    List<Medico> findByEspecialidad(String especialidad);
}
