package br.com.vet.identidade.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "veterinarios")
@Getter
@Setter
@NoArgsConstructor
public class Veterinario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "crmv", nullable = false, length = 20, unique = true)
    private String crmv;

    @Column(name = "especialidade", nullable = false, length = 100)
    private String especialidade;

    @Column(name = "deletado_em")
    private java.time.LocalDateTime deletadoEm;

    public Veterinario(Usuario usuario, String crmv, String especialidade) {
        this.usuario = usuario;
        this.crmv = crmv != null ? crmv.trim().toUpperCase() : null;
        this.especialidade = especialidade != null ? especialidade.trim() : null;
    }
}