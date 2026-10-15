package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.LoginRequest;
import br.com.vet.identidade.application.dto.request.RegistrarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.LoginResponse;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.service.AuthService;
import br.com.vet.identidade.application.service.TutorCadastroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TutorCadastroService tutorCadastroService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-tutor")
    public ResponseEntity<TutorResponseDTO> registrarTutor(@Valid @RequestBody RegistrarTutorRequestDTO request) {
        TutorResponseDTO response = tutorCadastroService.cadastrar(request);
        return ResponseEntity.status(201).body(response);
    }
}