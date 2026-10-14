package br.com.vet.identidade.application.dto.response;

import java.time.LocalDateTime;

public record TutorResponseDTO(
        Long id,
        Long usuarioId,
        String nomeCompleto,
        String email,
        String cpf,
        String telefone,
        String endereco,
        String status,
        LocalDateTime deletadoEm
) {
}