package proyecto_laboS.labo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import proyecto_laboS.labo.model.Agenda;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {

    List<Agenda> findByMedico_IdMedicoAndFecha(
            Long medicoId,
            LocalDate fecha
    );

    List<Agenda> findByMedico_IdMedicoAndFechaOrderByHoraAsc(
            Long medicoId,
            LocalDate fecha
    );

    List<Agenda> findByMedico_IdMedicoAndFechaAndDisponibleTrue(
            Long medicoId,
            LocalDate fecha
    );

    List<Agenda> findByMedico_IdMedicoAndFechaAndDisponibleTrueOrderByHoraAsc(
            Long medicoId,
            LocalDate fecha
    );

    List<Agenda> findByMedico_IdMedicoAndFechaBetweenOrderByFechaAscHoraAsc(
            Long medicoId,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    boolean existsByMedico_IdMedicoAndFechaAndHora(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora
    );

    java.util.Optional<Agenda> findByMedico_IdMedicoAndFechaAndHora(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora
    );

    java.util.Optional<Agenda> findFirstByMedico_IdMedicoAndFechaAndHoraOrderByIdAsc(
            Long medicoId,
            LocalDate fecha,
            LocalTime hora
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agenda from Agenda agenda where agenda.id = :id")
    java.util.Optional<Agenda> findByIdForUpdate(@Param("id") Long id);
}
