package proyecto_laboS.labo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Estudio;
import java.util.List;

public interface EstudioRepository extends JpaRepository<Estudio, Long> {
    
    // Aquí puedes agregar métodos personalizados si es necesario
    // Por ejemplo, para buscar estudios por nombre o tipo, etc.
    List<Estudio> findByNombre(String nombre);
    List<Estudio> findByPacienteIdPaciente(Long idPaciente);
    
}
