package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.service.TutorAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tutores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TutorAdminController {

    private final TutorAdminService service;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativarTutor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<TutorResponseDTO> reativar(@PathVariable Long id) {
        TutorResponseDTO tutor = service.reativarTutor(id);
        return ResponseEntity.ok(tutor);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> buscar(@PathVariable Long id) {
        TutorResponseDTO tutor = service.buscarTutorPorId(id);
        return ResponseEntity.ok(tutor);
    }
}