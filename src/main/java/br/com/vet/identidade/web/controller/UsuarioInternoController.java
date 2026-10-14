package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.AtualizarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.request.RegistrarUsuarioInternoRequest;
import br.com.vet.identidade.application.dto.response.UsuarioInternoResponse;
import br.com.vet.identidade.application.service.UsuarioInternoService;
import br.com.vet.identidade.domain.enums.Role;
import br.com.vet.identidade.domain.enums.StatusUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioInternoController {

    private final UsuarioInternoService service;

    @PostMapping
    public ResponseEntity<UsuarioInternoResponse> cadastrar(@Valid @RequestBody RegistrarUsuarioInternoRequest request) {
        UsuarioInternoResponse response = service.cadastrar(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioInternoResponse>> listar(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) StatusUsuario status,
            @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable
    ) {
        Page<UsuarioInternoResponse> page = service.listar(role, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioInternoResponse> buscar(@PathVariable Long id) {
        UsuarioInternoResponse response = service.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioInternoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioInternoRequest request) {
        UsuarioInternoResponse response = service.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UsuarioInternoResponse> alterarStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        UsuarioInternoResponse response = service.alterarStatus(id, request.status());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    public record StatusRequest(StatusUsuario status) {}
}