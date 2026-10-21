package br.com.vet.identidade.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void enviarLinkRedefinicaoSenha(String email, String token) {
        String link = frontendUrl + "/redefinir-senha?token=" + token;
        
        log.info("============================================");
        log.info("EMAIL DE RECUPERAÇÃO DE SENHA (MOCK)");
        log.info("Para: {}", email);
        log.info("Assunto: Redefinição de senha - Vet Identidade");
        log.info("Link: {}", link);
        log.info("Token: {}", token);
        log.info("Expira em: 1 hora");
        log.info("============================================");
        
        // TODO: Implementar envio real de e-mail (Spring Mail, SendGrid, etc.)
    }

    public void enviarConfirmacaoCadastro(String email, String nome) {
        log.info("============================================");
        log.info("EMAIL DE CONFIRMAÇÃO DE CADASTRO (MOCK)");
        log.info("Para: {}", email);
        log.info("Assunto: Bem-vindo ao Vet Identidade");
        log.info("Nome: {}", nome);
        log.info("============================================");
    }
}