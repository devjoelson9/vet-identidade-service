package br.com.vet.identidade.application.service;

import br.com.vet.identidade.application.dto.request.RedefinirSenhaRequestDTO;
import br.com.vet.identidade.application.dto.request.SolicitarRecuperacaoSenhaRequestDTO;
import br.com.vet.identidade.application.dto.response.RecuperacaoSenhaResponseDTO;
import br.com.vet.identidade.application.exception.BusinessException;
import br.com.vet.identidade.domain.model.PasswordResetToken;
import br.com.vet.identidade.domain.model.Usuario;
import br.com.vet.identidade.domain.repository.PasswordResetTokenRepository;
import br.com.vet.identidade.domain.repository.UsuarioRepository;
import br.com.vet.identidade.infrastructure.messaging.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String MENSAGEM_RECUPERACAO =
            "Se o e-mail estiver cadastrado, você receberá um link para redefinir sua senha em poucos minutos.";

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${password-reset.token-expiration-hours:1}")
    private int tokenExpirationHours;

    @Value("${password-reset.token-length:32}")
    private int tokenLength;

    @Transactional
    public RecuperacaoSenhaResponseDTO solicitarRecuperacao(SolicitarRecuperacaoSenhaRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null
                || usuario.getDeletadoEm() != null
                || usuario.getStatus() != br.com.vet.identidade.domain.enums.StatusUsuario.ATIVO) {
            return new RecuperacaoSenhaResponseDTO(MENSAGEM_RECUPERACAO);
        }

        // Invalidar tokens anteriores não usados
        tokenRepository.deleteByUsuarioId(usuario.getId());

        // Gerar novo token
        String token = gerarTokenSeguro();
        LocalDateTime expiracao = LocalDateTime.now().plusHours(tokenExpirationHours);

        PasswordResetToken resetToken = new PasswordResetToken(token, usuario, expiracao);
        tokenRepository.save(resetToken);

        // Enviar e-mail
        emailService.enviarLinkRedefinicaoSenha(usuario.getEmail(), token);

        return new RecuperacaoSenhaResponseDTO(MENSAGEM_RECUPERACAO);
    }

    @Transactional
    public RecuperacaoSenhaResponseDTO redefinirSenha(RedefinirSenhaRequestDTO request) {
        if (!request.senhasConferem()) {
            throw new BusinessException("As senhas não conferem");
        }

        String token = request.token().trim();
        
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));

        if (resetToken.getUsado()) {
            throw new BusinessException("Token já foi utilizado");
        }

        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token expirado");
        }

        Usuario usuario = resetToken.getUsuario();

        if (usuario.getDeletadoEm() != null) {
            throw new BusinessException("Conta desativada");
        }

        // Atualizar senha
        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);

        // Marcar token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        return new RecuperacaoSenhaResponseDTO("Senha redefinida com sucesso. Você já pode fazer login com a nova senha.");
    }

    private String gerarTokenSeguro() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[tokenLength];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}