package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByUsuarioId(Long usuarioId);
    Optional<Tutor> findByIdAndDeletadoEmIsNull(Long id);
}