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

    /*
     * Usuários internos são ativados/desativados por "status". O campo
     * "deletadoEm" é reservado para exclusão lógica real e não deve esconder
     * contas apenas desativadas, pois o administrador precisa consultá-las e
     * reativá-las.
     */
    @Query("SELECT u FROM Usuario u WHERE :role MEMBER OF u.roles " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findInternosByRole(
            @Param("role") Role role,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE u.status = :status " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findInternosByStatus(
            @Param("status") StatusUsuario status,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE :role MEMBER OF u.roles AND u.status = :status " +
           "AND :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findInternosByRoleAndStatus(
            @Param("role") Role role,
            @Param("status") StatusUsuario status,
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );

    @Query("SELECT u FROM Usuario u WHERE :roleTutor NOT MEMBER OF u.roles")
    Page<Usuario> findAllInternos(
            @Param("roleTutor") Role roleTutor,
            Pageable pageable
    );
}
