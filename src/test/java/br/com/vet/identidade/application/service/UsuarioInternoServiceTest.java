package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.RegistrarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.response.UsuarioInternoResponse;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.CrmvAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.exception.InvalidRoleException;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.UsuarioInternoMapper;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.model.Veterinario;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.domain.repository.VeterinarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioInternoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioInternoService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioInternoService(
                usuarioRepository,
                veterinarioRepository,
                passwordEncoder,
                new UsuarioInternoMapper()
        );
    }

    @Test
    void deveCadastrarVeterinarioComSenhaCriptografadaECrmvNormalizado() {
        RegistrarUsuarioInternoRequest request = requestVeterinario();

        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(veterinarioRepository.existsByCrmv("CRMV-123")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(5L);
            return salvo;
        });
        when(veterinarioRepository.save(any(Veterinario.class))).thenAnswer(invocation -> {
            Veterinario salvo = invocation.getArgument(0);
            salvo.setId(8L);
            return salvo;
        });

        UsuarioInternoResponse response = service.cadastrar(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.nomeCompleto()).isEqualTo("Dra. Ana");
        assertThat(response.status()).isEqualTo("ATIVO");
        assertThat(response.roles()).containsExactly(Role.VETERINARIO);
        assertThat(response.crmv()).isEqualTo("CRMV-123");
        assertThat(response.especialidade()).isEqualTo("Clínica Geral");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getSenhaHash()).isEqualTo("hash-seguro");
        assertThat(usuarioCaptor.getValue().getStatus()).isEqualTo(StatusUsuario.ATIVO);

        ArgumentCaptor<Veterinario> veterinarioCaptor = ArgumentCaptor.forClass(Veterinario.class);
        verify(veterinarioRepository).save(veterinarioCaptor.capture());
        assertThat(veterinarioCaptor.getValue().getUsuario()).isSameAs(usuarioCaptor.getValue());
        assertThat(veterinarioCaptor.getValue().getCrmv()).isEqualTo("CRMV-123");
    }

    @Test
    void deveCadastrarRecepcionistaSemCriarVeterinario() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(6L);
            return salvo;
        });

        UsuarioInternoResponse response = service.cadastrar(requestRecepcionista());

        assertThat(response.id()).isEqualTo(6L);
        assertThat(response.roles()).containsExactly(Role.RECEPCIONISTA);
        assertThat(response.crmv()).isNull();
        assertThat(response.especialidade()).isNull();
        verify(veterinarioRepository, never()).save(any());
    }

    @Test
    void deveRecusarPerfilNaoPermitidoParaUsuarioInterno() {
        RegistrarUsuarioInternoRequest request = new RegistrarUsuarioInternoRequest(
                "João",
                "12345678901",
                LocalDate.of(1990, 1, 1),
                "joao@teste.com",
                "senha123",
                null,
                Role.TUTOR,
                null,
                null
        );

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(InvalidRoleException.class)
                .hasMessageContaining("Perfil inválido");

        verifyNoInteractions(usuarioRepository, veterinarioRepository, passwordEncoder);
    }

    @Test
    void deveRecusarEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestRecepcionista()))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ana@teste.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRecusarCpfJaCadastrado() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestRecepcionista()))
                .isInstanceOf(CpfAlreadyExistsException.class)
                .hasMessageContaining("12345678901");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRecusarCrmvJaCadastradoParaVeterinario() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(veterinarioRepository.existsByCrmv("CRMV-123")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestVeterinario()))
                .isInstanceOf(CrmvAlreadyExistsException.class)
                .hasMessageContaining("CRMV-123");

        verify(usuarioRepository, never()).save(any());
        verify(veterinarioRepository, never()).save(any());
    }

    @Test
    void deveDesativarUsuarioInternoSemExclusaoLogica() {
        Usuario veterinario = usuario(5L, Role.VETERINARIO);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(veterinario));
        when(usuarioRepository.save(veterinario)).thenReturn(veterinario);
        when(veterinarioRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

        UsuarioInternoResponse response = service.alterarStatus(5L, StatusUsuario.DESATIVADO);

        assertThat(response.status()).isEqualTo("DESATIVADO");
        assertThat(veterinario.getStatus()).isEqualTo(StatusUsuario.DESATIVADO);
        /*
         * Desativar não é exclusão lógica: a conta precisa continuar listável
         * para que o administrador possa reativá-la.
         */
        assertThat(veterinario.getDeletadoEm()).isNull();
        verify(usuarioRepository).save(veterinario);
    }

    @Test
    void deveReativarUsuarioInternoDesativadoSemExclusaoLogica() {
        Usuario veterinario = usuario(5L, Role.VETERINARIO);
        veterinario.setStatus(StatusUsuario.DESATIVADO);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(veterinario));
        when(usuarioRepository.save(veterinario)).thenReturn(veterinario);
        when(veterinarioRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

        UsuarioInternoResponse response = service.alterarStatus(5L, StatusUsuario.ATIVO);

        assertThat(response.status()).isEqualTo("ATIVO");
        assertThat(veterinario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(veterinario.getDeletadoEm()).isNull();
    }

    @Test
    void deveReativarUsuarioComExclusaoLogicaLegada() {
        Usuario veterinario = usuario(5L, Role.VETERINARIO);
        veterinario.setStatus(StatusUsuario.DESATIVADO);
        veterinario.setDeletadoEm(LocalDateTime.now());

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(veterinario));
        when(usuarioRepository.save(veterinario)).thenReturn(veterinario);
        when(veterinarioRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

        UsuarioInternoResponse response = service.alterarStatus(5L, StatusUsuario.ATIVO);

        assertThat(response.status()).isEqualTo("ATIVO");
        assertThat(veterinario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(veterinario.getDeletadoEm()).isNull();
        verify(usuarioRepository).save(veterinario);
    }

    @Test
    void deveListarUsuariosInternosDesativados() {
        Usuario desativado = usuario(5L, Role.VETERINARIO);
        desativado.setStatus(StatusUsuario.DESATIVADO);
        Pageable pageable = PageRequest.of(0, 10);

        when(usuarioRepository.findInternosByStatus(StatusUsuario.DESATIVADO, Role.TUTOR, pageable))
                .thenReturn(new PageImpl<>(List.of(desativado), pageable, 1));
        when(veterinarioRepository.findByUsuarioId(5L)).thenReturn(Optional.empty());

        Page<UsuarioInternoResponse> pagina = service.listar(null, StatusUsuario.DESATIVADO, pageable);

        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getContent()).extracting(UsuarioInternoResponse::id).containsExactly(5L);
        assertThat(pagina.getContent()).extracting(UsuarioInternoResponse::status)
                .containsExactly("DESATIVADO");
    }

    @Test
    void deveExigirStatusAoAlterarSituacao() {
        assertThatThrownBy(() -> service.alterarStatus(5L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Situação");

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void deveRecusarAlteracaoDeStatusDeTutorOuAdmin() {
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario(5L, Role.TUTOR)));
        when(usuarioRepository.findById(6L)).thenReturn(Optional.of(usuario(6L, Role.ADMIN)));

        assertThatThrownBy(() -> service.alterarStatus(5L, StatusUsuario.DESATIVADO))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.alterarStatus(6L, StatusUsuario.DESATIVADO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveFalharAoAlterarStatusDeUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarStatus(99L, StatusUsuario.DESATIVADO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private RegistrarUsuarioInternoRequest requestVeterinario() {
        return new RegistrarUsuarioInternoRequest(
                "Dra. Ana",
                " 12345678901 ",
                LocalDate.of(1990, 5, 10),
                " Ana@Teste.COM ",
                "senha123",
                "(84) 99999-0000",
                Role.VETERINARIO,
                " crmv-123 ",
                " Clínica Geral "
        );
    }

    private RegistrarUsuarioInternoRequest requestRecepcionista() {
        return new RegistrarUsuarioInternoRequest(
                "Ana",
                "12345678901",
                LocalDate.of(1995, 3, 20),
                "ana@teste.com",
                "senha123",
                "(84) 99999-0000",
                Role.RECEPCIONISTA,
                null,
                null
        );
    }

    private Usuario usuario(Long id, Role role) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto("Usuário Teste");
        usuario.setCpf("12345678901");
        usuario.setEmail("usuario" + id + "@teste.com");
        usuario.setSenhaHash("hash");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.getRoles().add(role);
        return usuario;
    }
}
