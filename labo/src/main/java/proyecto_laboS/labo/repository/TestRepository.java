package proyecto_laboS.labo.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import proyecto_laboS.labo.model.Test;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {}