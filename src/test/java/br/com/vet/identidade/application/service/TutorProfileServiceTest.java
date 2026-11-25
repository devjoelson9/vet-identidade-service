package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.CpfAlreadyExistsException;
import br.com.vet.identidade.application.exception.EmailAlreadyExistsException;
import br.com.vet.identidade.application.mapper.TutorMapper;
import br.com.vet.identidade.domain.model.Tutor;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.TutorRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorProfileServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TutorMapper tutorMapper;

    private TutorProfileService service;
    private Usuario usuario;
    private Tutor tutor;

    @BeforeEach
    void setUp() {
        service = new TutorProfileService(tutorRepository, usuarioRepository, tutorMapper);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("Maria Silva");
        usuario.setEmail("maria@teste.com");
        usuario.setCpf("12345678901");

        tutor = new Tutor(usuario, "Rua Antiga, 10");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria@teste.com", null)
        );

        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(tutorRepository.findByUsuarioIdAndDeletadoEmIsNull(1L)).thenReturn(Optional.of(tutor));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAtualizarEmailDoTutorNormalizado() {
        stubTutorResponse();
        when(usuarioRepository.findByEmail("novo@teste.com")).thenReturn(Optional.empty());

        TutorResponseDTO response = service.atualizarMeuPerfil(
                new AtualizarTutorRequestDTO(null, null, " NOVO@TESTE.COM ", null, null)
        );

        assertThat(usuario.getEmail()).isEqualTo("novo@teste.com");
        assertThat(response.email()).isEqualTo("novo@teste.com");
        verify(usuarioRepository).save(usuario);
        verify(tutorRepository).save(tutor);
    }

    @Test
    void deveRecusarEmailPertenceAOutroUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("novo@teste.com");

        when(usuarioRepository.findByEmail("novo@teste.com"))
                .thenReturn(Optional.of(outroUsuario));

        assertThatThrownBy(() -> service.atualizarMeuPerfil(
                new AtualizarTutorRequestDTO(null, null, "novo@teste.com", null, null)
        ))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("novo@teste.com");

        assertThat(usuario.getEmail()).isEqualTo("maria@teste.com");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void devePermitirManterOProprioEmailComCaixaDiferente() {
        stubTutorResponse();
        TutorResponseDTO response = service.atualizarMeuPerfil(
                new AtualizarTutorRequestDTO(null, null, " MARIA@TESTE.COM ", null, null)
        );

        assertThat(usuario.getEmail()).isEqualTo("maria@teste.com");
        assertThat(response.email()).isEqualTo("maria@teste.com");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveRecusarCpfPertenceAOutroUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setCpf("98765432100");

        when(usuarioRepository.findByCpf("98765432100"))
                .thenReturn(Optional.of(outroUsuario));

        assertThatThrownBy(() -> service.atualizarMeuPerfil(
                new AtualizarTutorRequestDTO(
                        null, "98765432100", null, null, null
                )
        ))
                .isInstanceOf(CpfAlreadyExistsException.class)
                .hasMessageContaining("98765432100");

        assertThat(usuario.getCpf()).isEqualTo("12345678901");
        verify(usuarioRepository, never()).save(any());
    }

    private void stubTutorResponse() {
        when(tutorMapper.toDTO(tutor)).thenAnswer(invocation -> new TutorResponseDTO(
                tutor.getId(),
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getTelefone(),
                tutor.getEndereco(),
                "ATIVO",
                null
        ));
    }
}
