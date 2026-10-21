package br.com.vet.identidade.application.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Espécie é obrigatória")
        @Size(max = 50, message = "Espécie deve ter no máximo 50 caracteres")
        String especie,

        @Size(max = 50, message = "Raça deve ter no máximo 50 caracteres")
        String raca,

        @Size(max = 10, message = "Sexo deve ter no máximo 10 caracteres")
        @Pattern(regexp = "^(MACHO|FEMEA)$", message = "Sexo deve ser MACHO ou FEMEA")
        String sexo,

        @PastOrPresent(message = "Data de nascimento não pode ser futura")
        LocalDate dataNascimento,

        @DecimalMin(value = "0.0", message = "Peso não pode ser negativo")
        @Digits(integer = 3, fraction = 2, message = "Peso deve ter no máximo 3 dígitos inteiros e 2 decimais")
        BigDecimal peso,

        @Size(max = 1000, message = "Observações deve ter no máximo 1000 caracteres")
        String observacoes
) {
}