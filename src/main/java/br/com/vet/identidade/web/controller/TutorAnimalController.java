package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.AnimalRequestDTO;
import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.application.service.TutorAnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tutores/me/animais")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TUTOR')")
public class TutorAnimalController {

    private final TutorAnimalService service;

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> cadastrar(@Valid @RequestBody AnimalRequestDTO request) {
        AnimalResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody AnimalRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}