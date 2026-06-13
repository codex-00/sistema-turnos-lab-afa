package proyecto_laboS.labo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import proyecto_laboS.labo.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByEmailAndRolAndTokenHashAndUsadoFalseOrderByFechaCreacionDesc(
            String email,
            String rol,
            String tokenHash);
}
