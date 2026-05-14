package proyecto_laboS.labo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import proyecto_laboS.labo.model.Agenda;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    List<Agenda> findByMedico_IdMedicoAndFecha(
            Long medicoId,
            LocalDate fecha
    );

    List<Agenda> findByMedico_IdMedicoAndFechaAndDisponibleTrue(
            Long medicoId,
            LocalDate fecha
    );

    boolean existsByMedico_IdMedicoAndFechaAndHora(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora
    );
}
