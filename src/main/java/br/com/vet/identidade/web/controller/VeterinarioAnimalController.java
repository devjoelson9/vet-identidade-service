package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.response.AnimalResponseDTO;
import br.com.vet.identidade.application.service.VeterinarioAnimalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/veterinario/animais")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VETERINARIO')")
public class VeterinarioAnimalController {

    private final VeterinarioAnimalService service;

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> listar(
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable
    ) {
        if (termo != null && !termo.trim().isEmpty()) {
            return ResponseEntity.ok(service.buscarPorTermo(termo, pageable));
        }
        return ResponseEntity.ok(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }
}