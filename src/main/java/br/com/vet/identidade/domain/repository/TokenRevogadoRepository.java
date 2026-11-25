package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.TokenRevogado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenRevogadoRepository extends JpaRepository<TokenRevogado, Long> {

    boolean existsByJti(String jti);

    void deleteByExpiraEmBefore(LocalDateTime dataHora);
}
