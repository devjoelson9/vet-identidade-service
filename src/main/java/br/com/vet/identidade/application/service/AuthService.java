package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.LoginRequest;
import br.com.vet.identidade.application.dto.response.LoginResponse;
import br.com.vet.identidade.application.dto.response.UserDto;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase().trim(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado após autenticação"));

        String accessToken = jwtTokenProvider.generateAccessToken(usuario.getEmail(), usuario.getRoles());
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario.getEmail());

        UserDto userDto = new UserDto(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                usuario.getRoles(),
                usuario.getStatus().name()
        );

        return new LoginResponse(accessToken, refreshToken, userDto);
    }
}