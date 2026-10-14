package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {
    Optional<Veterinario> findByUsuarioId(Long usuarioId);
    boolean existsByCrmv(String crmv);
    Optional<Veterinario> findByCrmv(String crmv);
}