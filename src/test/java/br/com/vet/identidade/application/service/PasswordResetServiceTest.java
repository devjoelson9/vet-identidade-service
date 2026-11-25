package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.RedefinirSenhaRequestDTO;
import br.com.vet.identidade.application.dto.request.SolicitarRecuperacaoSenhaRequestDTO;
import br.com.vet.identidade.application.dto.response.RecuperacaoSenhaResponseDTO;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.PasswordResetToken;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.PasswordResetTokenRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.infrastructure.messaging.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                usuarioRepository,
                tokenRepository,
                passwordEncoder,
                emailService
        );
        ReflectionTestUtils.setField(service, "tokenExpirationHours", 2);
        ReflectionTestUtils.setField(service, "tokenLength", 16);
    }

    @Test
    void deveRetornarMensagemGenericaQuandoEmailNaoEstiverCadastrado() {
        when(usuarioRepository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        RecuperacaoSenhaResponseDTO response =
                service.solicitarRecuperacao(new SolicitarRecuperacaoSenhaRequestDTO("naoexiste@teste.com"));

        assertThat(response.mensagem()).contains("Se o e-mail estiver cadastrado");
        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void naoDeveEnviarEmailParaUsuarioDesativado() {
        Usuario usuario = usuario();
        usuario.setStatus(StatusUsuario.DESATIVADO);
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        RecuperacaoSenhaResponseDTO response =
                service.solicitarRecuperacao(new SolicitarRecuperacaoSenhaRequestDTO("maria@teste.com"));

        assertThat(response.mensagem()).contains("Se o e-mail estiver cadastrado");
        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void deveInvalidarTokensAnterioresGerarNovoTokenEEnviarEmail() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        RecuperacaoSenhaResponseDTO response =
                service.solicitarRecuperacao(new SolicitarRecuperacaoSenhaRequestDTO("  Maria@Teste.COM "));

        assertThat(response.mensagem()).contains("Se o e-mail estiver cadastrado");
        verify(tokenRepository).deleteByUsuarioId(1L);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        PasswordResetToken salvo = captor.getValue();

        assertThat(salvo.getToken()).isNotBlank();
        assertThat(salvo.getUsuario()).isSameAs(usuario);
        assertThat(salvo.getUsado()).isFalse();
        assertThat(salvo.getDataExpiracao())
                .isAfter(LocalDateTime.now().plusMinutes(119))
                .isBefore(LocalDateTime.now().plusMinutes(121));

        verify(emailService).enviarLinkRedefinicaoSenha("maria@teste.com", salvo.getToken());
    }

    @Test
    void deveRecusarRedefinicaoQuandoSenhasNaoConferem() {
        RedefinirSenhaRequestDTO request =
                new RedefinirSenhaRequestDTO("token-valido", "novaSenha123", "outraSenha123");

        assertThatThrownBy(() -> service.redefinirSenha(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("As senhas não conferem");

        verifyNoInteractions(tokenRepository, usuarioRepository, passwordEncoder);
    }

    @Test
    void deveRecusarTokenInexistente() {
        when(tokenRepository.findByToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.redefinirSenha(
                new RedefinirSenhaRequestDTO("token-inexistente", "novaSenha123", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token inválido ou expirado");
    }

    @Test
    void deveRecusarTokenJaUtilizado() {
        PasswordResetToken token = token(usuario(), LocalDateTime.now().plusHours(1));
        token.setUsado(true);
        when(tokenRepository.findByToken("token-usado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.redefinirSenha(
                new RedefinirSenhaRequestDTO("token-usado", "novaSenha123", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token já foi utilizado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRecusarTokenExpirado() {
        PasswordResetToken token = token(usuario(), LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.redefinirSenha(
                new RedefinirSenhaRequestDTO("token-expirado", "novaSenha123", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Token expirado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRecusarRedefinicaoParaContaDesativada() {
        Usuario usuario = usuario();
        usuario.setDeletadoEm(LocalDateTime.now());
        PasswordResetToken token = token(usuario, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.redefinirSenha(
                new RedefinirSenhaRequestDTO("token-valido", "novaSenha123", "novaSenha123")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conta desativada");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveRedefinirSenhaEMarcarTokenComoUsado() {
        Usuario usuario = usuario();
        PasswordResetToken token = token(usuario, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken("token-valido")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");

        RecuperacaoSenhaResponseDTO response = service.redefinirSenha(
                new RedefinirSenhaRequestDTO("  token-valido  ", "novaSenha123", "novaSenha123"));

        assertThat(response.mensagem()).contains("Senha redefinida com sucesso");
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        assertThat(token.getUsado()).isTrue();
        verify(usuarioRepository).save(usuario);
        verify(tokenRepository).save(token);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNomeCompleto("Maria Silva");
        usuario.setCpf("12345678901");
        usuario.setEmail("maria@teste.com");
        usuario.setSenhaHash("hash-antigo");
        usuario.setStatus(StatusUsuario.ATIVO);
        return usuario;
    }

    private PasswordResetToken token(Usuario usuario, LocalDateTime expiracao) {
        return new PasswordResetToken("token-valido", usuario, expiracao);
    }
}
