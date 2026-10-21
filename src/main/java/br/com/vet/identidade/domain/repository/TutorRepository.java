package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.model.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByUsuarioId(Long usuarioId);
    Optional<Tutor> findByUsuarioIdAndDeletadoEmIsNull(Long usuarioId);
    Optional<Tutor> findByIdAndDeletadoEmIsNull(Long id);
    Optional<Tutor> findByUsuario_EmailAndDeletadoEmIsNull(String email);
    Page<Tutor> findByDeletadoEmIsNull(Pageable pageable);

    @Query("SELECT t FROM Tutor t JOIN t.usuario u WHERE t.deletadoEm IS NULL AND (" +
           "LOWER(u.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "u.cpf = :termo OR " +
           "u.telefone = :termo)")
    Page<Tutor> buscarPorTermo(String termo, Pageable pageable);

    @Query("SELECT t FROM Tutor t JOIN t.usuario u WHERE t.deletadoEm IS NULL AND " +
           "LOWER(u.nomeCompleto) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Tutor> buscarPorNome(String nome, Pageable pageable);

    @Query("SELECT t FROM Tutor t JOIN t.usuario u WHERE t.deletadoEm IS NULL AND u.cpf = :cpf")
    Page<Tutor> buscarPorCpf(String cpf, Pageable pageable);

    @Query("SELECT t FROM Tutor t JOIN t.usuario u WHERE t.deletadoEm IS NULL AND u.telefone = :telefone")
    Page<Tutor> buscarPorTelefone(String telefone, Pageable pageable);
}