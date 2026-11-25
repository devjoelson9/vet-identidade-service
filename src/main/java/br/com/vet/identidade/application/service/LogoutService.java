package br.com.vet.identidade.application.service;

import br.com.vet.identidade.domain.model.TokenRevogado;
import br.com.vet.identidade.domain.repository.TokenRevogadoRepository;
import br.com.vet.identidade.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final TokenRevogadoRepository tokenRevogadoRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void revogarToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Tentativa de logout com token invalido ou expirado");
            return;
        }

        String jti = jwtTokenProvider.extractJti(token);
        if (jti == null || tokenRevogadoRepository.existsByJti(jti)) {
            return;
        }

        tokenRevogadoRepository.deleteByExpiraEmBefore(LocalDateTime.now());

        LocalDateTime expiraEm = LocalDateTime.ofInstant(
                jwtTokenProvider.extractExpiration(token).toInstant(),
                ZoneId.systemDefault()
        );
        tokenRevogadoRepository.save(new TokenRevogado(jti, expiraEm));
        log.info("Token revogado com sucesso. jti={}", jti);
    }

    public boolean isRevogado(String jti) {
        return jti != null && tokenRevogadoRepository.existsByJti(jti);
    }
}
