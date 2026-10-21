package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUsuarioIdAndUsadoFalse(Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}