package br.com.vet.identidade.infrastructure.messaging;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.from:noreply@vet.local}")
    private String from;

    @Async
    public void enviarLinkRedefinicaoSenha(String email, String token) {
        String link = frontendUrl + "/redefinir-senha?token=" + token;
        String assunto = "Redefinição de senha - Vet Identidade";
        String corpo = construirCorpoRedefinicaoSenha(email, link, token);

        enviarEmail(email, assunto, corpo);
    }

    @Async
    public void enviarConfirmacaoCadastro(String email, String nome) {
        String assunto = "Bem-vindo ao Vet Identidade";
        String corpo = construirCorpoConfirmacaoCadastro(nome);

        enviarEmail(email, assunto, corpo);
    }

    private void enviarEmail(String to, String assunto, String corpoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);

            mailSender.send(message);
            log.info("E-mail enviado com sucesso para: {}", to);
        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail para {}: {}", to, e.getMessage(), e);
        }
    }

    private String construirCorpoRedefinicaoSenha(String email, String link, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Redefinição de Senha</h2>
                    <p>Olá,</p>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Vet Identidade</strong>.</p>
                    <p>Clique no botão abaixo para definir uma nova senha:</p>
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block;">Redefinir Senha</a>
                    </p>
                    <p>Ou copie e cole o link abaixo no navegador:</p>
                    <p style="word-break: break-all; color: #3498db;">%s</p>
                    <p><strong>Token:</strong> %s</p>
                    <p><strong>Expira em:</strong> 1 hora</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #999;">Se você não solicitou esta redefinição, ignore este e-mail.</p>
                </div>
            </body>
            </html>
            """.formatted(link, link, token);
    }

    private String construirCorpoConfirmacaoCadastro(String nome) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Bem-vindo ao Vet Identidade!</h2>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu cadastro foi realizado com sucesso. Agora você pode acessar o sistema com suas credenciais.</p>
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #27ae60; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; display: inline-block;">Acessar Sistema</a>
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #999;">Equipe Vet Identidade</p>
                </div>
            </body>
            </html>
            """.formatted(nome, frontendUrl);
    }
}