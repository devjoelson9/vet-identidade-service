package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.LoginRequest;
import br.com.vet.identidade.application.dto.request.RefreshTokenRequestDTO;
import br.com.vet.identidade.application.dto.request.RegistrarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.LoginResponse;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.application.service.AuthService;
import br.com.vet.identidade.application.service.LogoutService;
import br.com.vet.identidade.application.service.TutorCadastroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TutorCadastroService tutorCadastroService;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @Valid @RequestBody(required = false) RefreshTokenRequestDTO request) {
        logoutService.revogarToken(extrairToken(authorization));
        if (request != null) {
            logoutService.revogarToken(request.refreshToken());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register-tutor")
    public ResponseEntity<TutorResponseDTO> registrarTutor(@Valid @RequestBody RegistrarTutorRequestDTO request) {
        TutorResponseDTO response = tutorCadastroService.cadastrar(request);
        return ResponseEntity.status(201).body(response);
    }

    private String extrairToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException("Token de acesso nao informado");
        }
        return authorization.substring(7);
    }
}