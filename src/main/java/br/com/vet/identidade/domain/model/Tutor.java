package br.com.vet.identidade.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tutores")
@Getter
@Setter
@NoArgsConstructor
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "endereco", columnDefinition = "TEXT")
    private String endereco;

    @Column(name = "deletado_em")
    private LocalDateTime deletadoEm;

    public Tutor(Usuario usuario, String endereco) {
        this.usuario = usuario;
        this.endereco = endereco;
    }
}