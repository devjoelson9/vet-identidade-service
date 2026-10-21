package br.com.vet.identidade.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "animais")
@Getter
@Setter
@NoArgsConstructor
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Espécie é obrigatória")
    @Size(max = 50, message = "Espécie deve ter no máximo 50 caracteres")
    @Column(nullable = false, length = 50)
    private String especie;

    @Size(max = 50, message = "Raça deve ter no máximo 50 caracteres")
    @Column(length = 50)
    private String raca;

    @Size(max = 10, message = "Sexo deve ter no máximo 10 caracteres")
    @Column(length = 10)
    private String sexo;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @DecimalMin(value = "0.0", message = "Peso não pode ser negativo")
    @Column(name = "peso", precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}