package br.com.vet.identidade.domain.repository;

import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);

    @Query("SELECT u FROM Usuario u WHERE :role MEMBER OF u.roles AND u.deletadoEm IS NULL " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findByRoleAndDeletadoEmIsNull(
            @Param("role") Role role,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE u.status = :status AND u.deletadoEm IS NULL " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findByStatusAndDeletadoEmIsNull(
            @Param("status") StatusUsuario status,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE :role MEMBER OF u.roles AND u.status = :status " +
           "AND u.deletadoEm IS NULL AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findByRoleAndStatusAndDeletadoEmIsNull(
            @Param("role") Role role,
            @Param("status") StatusUsuario status,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE u.deletadoEm IS NULL " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findByDeletadoEmIsNull(
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );
}
