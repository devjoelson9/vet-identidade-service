package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.RedfinirSenhaRequestDTO;
import br.com.vet.identidade.application.dto.request.SolicitarRecuperacaoSenhaRequestDTO;
import br.com.vet.identidade.application.dto.response.RecuperacaoSenhaResponseDTO;
import br.com.vet.identidade.application.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<RecuperacaoSenhaResponseDTO> solicitarRecuperacao(@Valid @RequestBody SolicitarRecuperacaoSenhaRequestDTO request) {
        RecuperacaoSenhaResponseDTO response = passwordResetService.solicitarRecuperacao(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<RecuperacaoSenhaResponseDTO> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO request) {
        RecuperacaoSenhaResponseDTO response = passwordResetService.redefinirSenha(request);
        return ResponseEntity.ok(response);
    }
}