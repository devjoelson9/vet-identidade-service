package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.Animal;
import br.com.vet.identidade.domain.model.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByTutorAndDeletadoEmIsNull(Tutor tutor);
    Page<Animal> findByTutorAndDeletadoEmIsNull(Tutor tutor, Pageable pageable);
    Optional<Animal> findByIdAndTutorAndDeletadoEmIsNull(Long id, Tutor tutor);
    Optional<Animal> findByIdAndDeletadoEmIsNull(Long id);
}