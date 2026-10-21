package br.com.vet.identidade.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnimalResponseDTO(
        Long id,
        Long tutorId,
        String tutorNome,
        String nome,
        String especie,
        String raca,
        String sexo,
        LocalDate dataNascimento,
        BigDecimal peso,
        String observacoes,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDateTime deletadoEm
) {
}