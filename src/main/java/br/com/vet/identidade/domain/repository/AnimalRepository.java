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

    @Query("SELECT a FROM Animal a JOIN a.tutor t JOIN t.usuario u WHERE a.deletadoEm IS NULL AND (" +
           "LOWER(a.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(a.especie) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(a.raca) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(u.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "u.cpf = :termo)")
    Page<Animal> buscarPorTermo(String termo, Pageable pageable);

    Page<Animal> findByDeletadoEmIsNull(Pageable pageable);
}