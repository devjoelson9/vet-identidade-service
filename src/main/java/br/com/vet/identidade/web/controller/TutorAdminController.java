package br.com.vet.identidade.web.controller;

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

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponse> buscar(@PathVariable Long id) {
        var tutor = service.buscarTutorAtivo(id);
        return ResponseEntity.ok(new TutorResponse(
                tutor.getId(),
                tutor.getUsuario().getId(),
                tutor.getUsuario().getNomeCompleto(),
                tutor.getUsuario().getEmail(),
                tutor.getUsuario().getCpf(),
                tutor.getUsuario().getTelefone(),
                tutor.getEndereco(),
                tutor.getUsuario().getStatus().name()
        ));
    }

    public record TutorResponse(
            Long id,
            Long usuarioId,
            String nomeCompleto,
            String email,
            String cpf,
            String telefone,
            String endereco,
            String status
    ) {}
}