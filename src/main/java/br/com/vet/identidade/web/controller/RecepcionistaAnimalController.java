package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.AnimalRequestDTO;
import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.application.service.RecepcionistaAnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recepcionista/tutores/{tutorId}/animais")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPCIONISTA')")
public class RecepcionistaAnimalController {

    private final RecepcionistaAnimalService service;

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> cadastrar(
            @PathVariable Long tutorId,
            @Valid @RequestBody AnimalRequestDTO request
    ) {
        AnimalResponseDTO response = service.cadastrar(tutorId, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> listar(
            @PathVariable Long tutorId,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarPorTutor(tutorId, pageable));
    }

    @GetMapping("/{animalId}")
    public ResponseEntity<AnimalResponseDTO> buscar(
            @PathVariable Long tutorId,
            @PathVariable Long animalId
    ) {
        return ResponseEntity.ok(service.buscarPorId(tutorId, animalId));
    }

    @PutMapping("/{animalId}")
    public ResponseEntity<AnimalResponseDTO> atualizar(
            @PathVariable Long tutorId,
            @PathVariable Long animalId,
            @Valid @RequestBody AnimalRequestDTO request
    ) {
        return ResponseEntity.ok(service.atualizar(tutorId, animalId, request));
    }

    @DeleteMapping("/{animalId}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long tutorId,
            @PathVariable Long animalId
    ) {
        service.desativar(tutorId, animalId);
        return ResponseEntity.noContent().build();
    }
}