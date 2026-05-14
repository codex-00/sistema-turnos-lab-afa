package proyecto_laboS.labo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Medico;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByEmailAndPassword(String email, String password);
    List<Medico> findByNombre(String nombre);
    List<Medico> findByEspecialidad(String especialidad);
}