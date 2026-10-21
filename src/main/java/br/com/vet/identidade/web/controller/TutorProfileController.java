package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.service.TutorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutores/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TUTOR')")
public class TutorProfileController {

    private final TutorProfileService service;

    @GetMapping
    public ResponseEntity<TutorResponseDTO> buscarMeuPerfil() {
        return ResponseEntity.ok(service.buscarMeuPerfil());
    }

    @PatchMapping
    public ResponseEntity<TutorResponseDTO> atualizarMeuPerfil(@Valid @RequestBody AtualizarTutorRequestDTO request) {
        return ResponseEntity.ok(service.atualizarMeuPerfil(request));
    }
}