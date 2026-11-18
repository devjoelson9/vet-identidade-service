package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.response.VeterinarioResumoResponse;
import br.com.vet.identidade.application.service.VeterinarioConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/veterinarios")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class VeterinarioController {

    private final VeterinarioConsultaService service;

    @GetMapping
    public ResponseEntity<List<VeterinarioResumoResponse>> listar() {
        return ResponseEntity.ok(service.listarAtivos());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<VeterinarioResumoResponse> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.of(service.buscarPorUsuario(usuarioId));
    }
}
