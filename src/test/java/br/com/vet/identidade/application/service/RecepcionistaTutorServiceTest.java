package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRecepcionistaRequestDTO;
import br.com.vet.identidade.application.dto.request.RecepcionistaTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.exception.ResourceNotFoundException;
import br.com.vet.identidade.application.mapper.TutorMapper;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecepcionistaTutorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RecepcionistaTutorService service;

    @BeforeEach
    void setUp() {
        service = new RecepcionistaTutorService(
                usuarioRepository,
                tutorRepository,
                passwordEncoder,
                new TutorMapper()
        );
    }

    @Test
    void deveCadastrarTutorComSenhaCriptografadaEPerfilTutor() {
        RecepcionistaTutorRequestDTO request = new RecepcionistaTutorRequestDTO(
                "João Pereira",
                " 12345678901 ",
                "  Joao@Teste.COM ",
                "senha123",
                "(84) 98888-7777",
                "Rua das Flores, 10"
        );

        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-seguro");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });
        when(tutorRepository.save(any(Tutor.class))).thenAnswer(invocation -> {
            Tutor salvo = invocation.getArgument(0);
            salvo.setId(20L);
            return salvo;
        });

        TutorResponseDTO response = service.cadastrar(request);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.usuarioId()).isEqualTo(10L);
        assertThat(response.nomeCompleto()).isEqualTo("João Pereira");
        assertThat(response.email()).isEqualTo("joao@teste.com");
        assertThat(response.cpf()).isEqualTo("12345678901");
        assertThat(response.endereco()).isEqualTo("Rua das Flores, 10");
        assertThat(response.status()).isEqualTo("ATIVO");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getSenhaHash()).isEqualTo("hash-seguro");
        assertThat(usuarioSalvo.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(usuarioSalvo.getRoles()).containsExactly(Role.TUTOR);

        ArgumentCaptor<Tutor> tutorCaptor = ArgumentCaptor.forClass(Tutor.class);
        verify(tutorRepository).save(tutorCaptor.capture());
        assertThat(tutorCaptor.getValue().getUsuario()).isSameAs(usuarioSalvo);
        assertThat(tutorCaptor.getValue().getEndereco()).isEqualTo("Rua das Flores, 10");
    }

    @Test
    void deveRecusarCadastroComEmailJaCadastrado() {
        RecepcionistaTutorRequestDTO request = novoRequest();
        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("joao@teste.com");

        verify(usuarioRepository, never()).save(any());
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deveRecusarCadastroComCpfJaCadastrado() {
        RecepcionistaTutorRequestDTO request = novoRequest();
        when(usuarioRepository.existsByEmail("joao@teste.com")).thenReturn(false);
        when(usuarioRepository.existsByCpf("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(CpfAlreadyExistsException.class)
                .hasMessageContaining("12345678901");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveAtualizarDadosDoTutorSemVerificarDuplicidadeQuandoEmailECpfNaoMudam() {
        Usuario usuario = usuario(10L);
        Tutor tutor = new Tutor(usuario, "Rua Antiga");
        tutor.setId(20L);

        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(tutorRepository.save(tutor)).thenReturn(tutor);

        TutorResponseDTO response = service.atualizar(20L, new AtualizarTutorRecepcionistaRequestDTO(
                "Maria Souza",
                "12345678901",
                " Maria@Teste.COM ",
                "(84) 97777-6666",
                "Rua Nova, 50"
        ));

        assertThat(response.nomeCompleto()).isEqualTo("Maria Souza");
        assertThat(response.email()).isEqualTo("maria@teste.com");
        assertThat(response.telefone()).isEqualTo("(84) 97777-6666");
        assertThat(response.endereco()).isEqualTo("Rua Nova, 50");
        verify(usuarioRepository, never()).existsByEmail(any());
        verify(usuarioRepository, never()).existsByCpf(any());
    }

    @Test
    void deveRecusarAtualizacaoParaEmailDeOutroUsuario() {
        Usuario usuario = usuario(10L);
        Tutor tutor = new Tutor(usuario, "Rua Antiga");

        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));
        when(usuarioRepository.existsByEmail("outro@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(20L, new AtualizarTutorRecepcionistaRequestDTO(
                "Maria Souza",
                "12345678901",
                "outro@teste.com",
                "(84) 97777-6666",
                "Rua Nova, 50"
        )))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("outro@teste.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveDesativarTutorComExclusaoLogica() {
        Usuario usuario = usuario(10L);
        Tutor tutor = new Tutor(usuario, "Rua Antiga");
        tutor.setId(20L);
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));

        service.desativar(20L);

        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.DESATIVADO);
        assertThat(usuario.getDeletadoEm()).isNotNull();
        assertThat(tutor.getDeletadoEm()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(tutorRepository).save(tutor);
    }

    @Test
    void deveFalharAoDesativarTutorInexistente() {
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativar(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(usuarioRepository, never()).save(any());
    }

    private RecepcionistaTutorRequestDTO novoRequest() {
        return new RecepcionistaTutorRequestDTO(
                "João Pereira",
                "12345678901",
                "joao@teste.com",
                "senha123",
                "(84) 98888-7777",
                "Rua das Flores, 10"
        );
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto("Maria Silva");
        usuario.setCpf("12345678901");
        usuario.setEmail("maria@teste.com");
        usuario.setSenhaHash("hash");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.getRoles().add(Role.TUTOR);
        return usuario;
    }
}
