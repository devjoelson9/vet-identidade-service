package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorAdminServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private TutorAdminService service;

    @BeforeEach
    void setUp() {
        service = new TutorAdminService(
                tutorRepository,
                usuarioRepository,
                new TutorMapper()
        );
    }

    @Test
    void deveDesativarTutorComExclusaoLogica() {
        Usuario usuario = usuario(StatusUsuario.ATIVO);
        Tutor tutor = tutor(usuario, null);
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));

        service.desativarTutor(20L);

        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.DESATIVADO);
        assertThat(usuario.getDeletadoEm()).isNotNull();
        assertThat(tutor.getDeletadoEm()).isNotNull();
        verify(usuarioRepository).save(usuario);
        verify(tutorRepository).save(tutor);
    }

    @Test
    void deveFalharAoDesativarTutorInexistente() {
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.desativarTutor(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveFalharAoDesativarTutorJaExcluido() {
        Usuario usuario = usuario(StatusUsuario.DESATIVADO);
        Tutor tutor = tutor(usuario, LocalDateTime.now());
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));

        assertThatThrownBy(() -> service.desativarTutor(20L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveReativarTutorDesativado() {
        Usuario usuario = usuario(StatusUsuario.DESATIVADO);
        usuario.setDeletadoEm(LocalDateTime.now());
        Tutor tutor = tutor(usuario, LocalDateTime.now());
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(tutorRepository.save(tutor)).thenReturn(tutor);

        TutorResponseDTO response = service.reativarTutor(20L);

        assertThat(response.status()).isEqualTo("ATIVO");
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(usuario.getDeletadoEm()).isNull();
        assertThat(tutor.getDeletadoEm()).isNull();
    }

    @Test
    void deveFalharAoReativarTutorQueNaoEstaDesativado() {
        Usuario usuario = usuario(StatusUsuario.ATIVO);
        Tutor tutor = tutor(usuario, null);
        when(tutorRepository.findById(20L)).thenReturn(Optional.of(tutor));

        assertThatThrownBy(() -> service.reativarTutor(20L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveBuscarApenasTutorAtivo() {
        Usuario usuario = usuario(StatusUsuario.ATIVO);
        Tutor tutor = tutor(usuario, null);
        when(tutorRepository.findByIdAndDeletadoEmIsNull(20L)).thenReturn(Optional.of(tutor));

        TutorResponseDTO response = service.buscarTutorAtivo(20L);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.nomeCompleto()).isEqualTo("Maria Silva");
        assertThat(response.status()).isEqualTo("ATIVO");
    }

    @Test
    void deveFalharAoBuscarTutorAtivoInexistente() {
        when(tutorRepository.findByIdAndDeletadoEmIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarTutorAtivo(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Tutor tutor(Usuario usuario, LocalDateTime deletadoEm) {
        Tutor tutor = new Tutor(usuario, "Rua das Flores, 10");
        tutor.setId(20L);
        tutor.setDeletadoEm(deletadoEm);
        return tutor;
    }

    private Usuario usuario(StatusUsuario status) {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNomeCompleto("Maria Silva");
        usuario.setCpf("12345678901");
        usuario.setEmail("maria@teste.com");
        usuario.setSenhaHash("hash");
        usuario.setStatus(status);
        usuario.getRoles().add(Role.TUTOR);
        return usuario;
    }
}
