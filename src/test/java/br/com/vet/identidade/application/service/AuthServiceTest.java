package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.LoginRequest;
import br.com.vet.identidade.application.dto.response.LoginResponse;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.model.Veterinario;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.domain.repository.VeterinarioRepository;
import br.com.vet.identidade.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LogoutService logoutService;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                authenticationManager,
                usuarioRepository,
                veterinarioRepository,
                jwtTokenProvider,
                logoutService
        );
    }

    @Test
    void deveNormalizarEmailNoLoginEDevolverDadosDoUsuario() {
        Usuario usuario = usuario(1L, Role.TUTOR);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateAccessToken("maria@teste.com", 1L, usuario.getRoles()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("maria@teste.com"))
                .thenReturn("refresh-token");

        LoginResponse response = service.login(new LoginRequest("  Maria@Teste.COM ", "senha123"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("maria@teste.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("senha123");

        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.user().nomeCompleto()).isEqualTo("Maria Silva");
        assertThat(response.user().email()).isEqualTo("maria@teste.com");
        assertThat(response.user().status()).isEqualTo("ATIVO");
        assertThat(response.user().telefone()).isEqualTo("(84) 99999-0000");
        assertThat(response.user().roles()).containsExactly(Role.TUTOR);
        assertThat(response.user().veterinarioId()).isNull();
        assertThat(response.user().crmv()).isNull();
        assertThat(response.user().especialidade()).isNull();
    }

    @Test
    void deveIncluirDadosDoVeterinarioNaRespostaDoLogin() {
        Usuario usuario = usuario(2L, Role.VETERINARIO);
        Veterinario veterinario = new Veterinario(usuario, " crmv-123 ", " Clínica Geral ");
        veterinario.setId(7L);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(veterinarioRepository.findByUsuarioId(2L)).thenReturn(Optional.of(veterinario));

        LoginResponse response = service.login(new LoginRequest("maria@teste.com", "senha123"));

        assertThat(response.user().veterinarioId()).isEqualTo(7L);
        assertThat(response.user().crmv()).isEqualTo("CRMV-123");
        assertThat(response.user().especialidade()).isEqualTo("Clínica Geral");
    }

    @Test
    void deveFalharQuandoUsuarioNaoForEncontradoAposAutenticacao() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("maria@teste.com", "senha123")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void deveRevogarRefreshTokenAntigoERenovarSessao() {
        String refreshToken = "refresh-antigo";
        Usuario usuario = usuario(1L, Role.TUTOR);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractJti(refreshToken)).thenReturn("jti-1");
        when(logoutService.isRevogado("jti-1")).thenReturn(false);
        when(jwtTokenProvider.extractEmail(refreshToken)).thenReturn("maria@teste.com");
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));
        when(jwtTokenProvider.generateAccessToken("maria@teste.com", 1L, usuario.getRoles()))
                .thenReturn("novo-access");
        when(jwtTokenProvider.generateRefreshToken("maria@teste.com"))
                .thenReturn("novo-refresh");

        LoginResponse response = service.refresh(refreshToken);

        assertThat(response.token()).isEqualTo("novo-access");
        assertThat(response.refreshToken()).isEqualTo("novo-refresh");
        assertThat(response.user().email()).isEqualTo("maria@teste.com");
        verify(logoutService).revogarToken(refreshToken);
    }

    @Test
    void deveRecusarRefreshTokenInvalidoSemConsultarUsuario() {
        String refreshToken = "refresh-invalido";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        assertThatThrownBy(() -> service.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token inválido ou expirado");

        verify(usuarioRepository, never()).findByEmail(any());
        verify(logoutService, never()).revogarToken(any());
    }

    @Test
    void deveRecusarRefreshTokenJaRevogado() {
        String refreshToken = "refresh-revogado";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractJti(refreshToken)).thenReturn("jti-revogado");
        when(logoutService.isRevogado("jti-revogado")).thenReturn(true);

        assertThatThrownBy(() -> service.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token inválido ou expirado");

        verify(usuarioRepository, never()).findByEmail(any());
        verify(logoutService, never()).revogarToken(any());
    }

    @Test
    void deveRecusarRenovacaoDeUsuarioDesativado() {
        String refreshToken = "refresh-valido";
        Usuario usuario = usuario(1L, Role.TUTOR);
        usuario.setStatus(StatusUsuario.DESATIVADO);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractJti(refreshToken)).thenReturn("jti-2");
        when(logoutService.isRevogado("jti-2")).thenReturn(false);
        when(jwtTokenProvider.extractEmail(refreshToken)).thenReturn("maria@teste.com");
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conta indisponível");

        verify(logoutService, never()).revogarToken(any());
    }

    @Test
    void deveRecusarRenovacaoDeContaExcluida() {
        String refreshToken = "refresh-valido";
        Usuario usuario = usuario(1L, Role.TUTOR);
        usuario.setDeletadoEm(LocalDateTime.now());

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.extractJti(refreshToken)).thenReturn("jti-3");
        when(logoutService.isRevogado("jti-3")).thenReturn(false);
        when(jwtTokenProvider.extractEmail(refreshToken)).thenReturn("maria@teste.com");
        when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Conta indisponível");
    }

    private Usuario usuario(Long id, Role... roles) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto("Maria Silva");
        usuario.setCpf("12345678901");
        usuario.setEmail("maria@teste.com");
        usuario.setSenhaHash("hash-antigo");
        usuario.setTelefone("(84) 99999-0000");
        usuario.setStatus(StatusUsuario.ATIVO);
        usuario.getRoles().addAll(Set.of(roles));
        return usuario;
    }
}
