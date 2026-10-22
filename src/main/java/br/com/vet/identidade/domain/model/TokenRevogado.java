package br.com.vet.identidade.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_revogados")
@Getter
@Setter
@NoArgsConstructor
public class TokenRevogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jti", nullable = false, unique = true, length = 36)
    private String jti;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public TokenRevogado(String jti, LocalDateTime expiraEm) {
        this.jti = jti;
        this.expiraEm = expiraEm;
        this.criadoEm = LocalDateTime.now();
    }
}
