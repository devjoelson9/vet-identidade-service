package br.com.vet.identidade.web.controller;

import br.com.vet.identidade.application.dto.request.AtualizarTutorRecepcionistaRequestDTO;
import br.com.vet.identidade.application.dto.request.RecepcionistaTutorRequestDTO;
import br.com.vet.identidade.application.dto.response.TutorResponseDTO;
import br.com.vet.identidade.application.service.RecepcionistaTutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recepcionista/tutores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RECEPCIONISTA')")
public class RecepcionistaTutorController {

    /**
     * Campos de ordenacao expostos na API (TutorResponseDTO) mapeados para as propriedades da entidade.
     * Sem esse mapeamento, sort=nomeCompleto falha porque nomeCompleto pertence a Usuario, nao a Tutor.
     */
    private static final Map<String, String> SORT_PROPERTIES = Map.of(
            "nomeCompleto", "usuario.nomeCompleto",
            "email", "usuario.email",
            "cpf", "usuario.cpf",
            "telefone", "usuario.telefone",
            "status", "usuario.status",
            "usuarioId", "usuario.id"
    );

    private final RecepcionistaTutorService service;

    @PostMapping
    public ResponseEntity<TutorResponseDTO> cadastrar(@Valid @RequestBody RecepcionistaTutorRequestDTO request) {
        TutorResponseDTO response = service.cadastrar(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMIN')")
    public ResponseEntity<Page<TutorResponseDTO>> listar(
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 20, sort = "nomeCompleto") Pageable pageable
    ) {
        Pageable page = mapPageable(pageable);
        if (termo != null && !termo.trim().isEmpty()) {
            return ResponseEntity.ok(service.buscarPorTermo(termo, page));
        }
        return ResponseEntity.ok(service.listar(page));
    }

    private static Pageable mapPageable(Pageable pageable) {
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> order.withProperty(SORT_PROPERTIES.getOrDefault(order.getProperty(), order.getProperty())))
                .toList();

        if (orders.isEmpty()) {
            orders = List.of(Sort.Order.asc("usuario.nomeCompleto"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ADMIN')")
    public ResponseEntity<TutorResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarTutorRecepcionistaRequestDTO request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}