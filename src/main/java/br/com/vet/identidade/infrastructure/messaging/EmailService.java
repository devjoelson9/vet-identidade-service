package br.com.vet.identidade.infrastructure.messaging;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String COR_PRIMARIA = "#0f766e";
    private static final String COR_SUCESSO = "#0f766e";

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.from:noreply@vet.local}")
    private String from;

    @Async
    public void enviarLinkRedefinicaoSenha(String email, String token) {
        String tokenCodificado = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String link = frontendUrl + "/redefinir-senha?token=" + tokenCodificado;
        String assunto = "Redefinição de senha - Vet Identidade";
        String corpo = construirCorpoRedefinicaoSenha(email, link);

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
        } catch (MessagingException | MailException e) {
            log.error("Falha ao enviar e-mail para {}: {}", to, e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Conteúdo dos e-mails
    // ------------------------------------------------------------------

    private String construirCorpoRedefinicaoSenha(String email, String link) {
        String linkSeguro = HtmlUtils.htmlEscape(link);
        String emailSeguro = HtmlUtils.htmlEscape(email);

        String conteudo = """
            <h1 style="margin:0 0 16px;font-size:22px;line-height:28px;color:#0f172a;">Redefinição de senha</h1>
            <p style="margin:0 0 12px;font-size:15px;line-height:24px;color:#334155;">Olá,</p>
            <p style="margin:0 0 12px;font-size:15px;line-height:24px;color:#334155;">
                Recebemos uma solicitação para redefinir a senha da conta <strong>%s</strong> no <strong>Vet Identidade</strong>.
            </p>
            <p style="margin:0 0 24px;font-size:15px;line-height:24px;color:#334155;">
                Clique no botão abaixo para criar uma nova senha:
            </p>
            %s
            <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0"
                   style="margin:24px 0;background-color:#f8fafc;border:1px solid #e2e8f0;border-radius:6px;">
                <tr>
                    <td style="padding:12px 16px;font-size:13px;line-height:20px;color:#475569;">
                        ⏱ Este link expira em <strong>1 hora</strong>.
                    </td>
                </tr>
            </table>
            <p style="margin:0 0 6px;font-size:13px;line-height:20px;color:#64748b;">
                Se o botão não funcionar, copie e cole este endereço no navegador:
            </p>
            <p style="margin:0;font-size:13px;line-height:20px;word-break:break-all;">
                <a href="%s" style="color:%s;text-decoration:underline;">%s</a>
            </p>
            <hr style="border:none;border-top:1px solid #e2e8f0;margin:28px 0 16px;">
            <p style="margin:0;font-size:13px;line-height:20px;color:#64748b;">
                Se você não fez essa solicitação, pode ignorar este e-mail com segurança. Sua senha atual continuará a mesma.
            </p>
            """.formatted(
                emailSeguro,
                botao(linkSeguro, "Redefinir senha", COR_PRIMARIA),
                linkSeguro, COR_PRIMARIA, linkSeguro);

        return montarLayout("Use o link para criar uma nova senha. Ele expira em 1 hora.", conteudo);
    }

    private String construirCorpoConfirmacaoCadastro(String nome) {
        String nomeSeguro = HtmlUtils.htmlEscape(nome);
        String urlSegura = HtmlUtils.htmlEscape(frontendUrl);

        String conteudo = """
            <h1 style="margin:0 0 16px;font-size:22px;line-height:28px;color:#0f172a;">Bem-vindo ao Vet Identidade! 🐾</h1>
            <p style="margin:0 0 12px;font-size:15px;line-height:24px;color:#334155;">
                Olá, <strong>%s</strong>!
            </p>
            <p style="margin:0 0 24px;font-size:15px;line-height:24px;color:#334155;">
                Seu cadastro foi concluído com sucesso. Você já pode acessar o sistema com suas credenciais.
            </p>
            %s
            <hr style="border:none;border-top:1px solid #e2e8f0;margin:28px 0 16px;">
            <p style="margin:0;font-size:13px;line-height:20px;color:#64748b;">
                Se você não criou esta conta, entre em contato com a nossa equipe.
            </p>
            """.formatted(nomeSeguro, botao(urlSegura, "Acessar o sistema", COR_SUCESSO));

        return montarLayout("Seu cadastro foi concluído. Acesse o sistema agora.", conteudo);
    }

    // ------------------------------------------------------------------
    // Componentes reutilizáveis
    // ------------------------------------------------------------------

    /** Botão "à prova de e-mail": funciona em Outlook, Gmail e apps mobile. */
    private String botao(String href, String texto, String cor) {
        return """
            <table role="presentation" cellpadding="0" cellspacing="0" border="0" align="center" style="margin:0 auto;">
                <tr>
                    <td align="center" bgcolor="%s" style="border-radius:6px;">
                        <a href="%s" target="_blank"
                           style="display:inline-block;padding:14px 32px;font-family:Arial,Helvetica,sans-serif;font-size:16px;font-weight:bold;color:#ffffff;text-decoration:none;border-radius:6px;background-color:%s;">
                            %s
                        </a>
                    </td>
                </tr>
            </table>
            """.formatted(cor, href, cor, texto);
    }

    /**
     * Layout base: cabeçalho, cartão central e rodapé.
     * Usa replace() em vez de formatted() para que o "%" do CSS não precise ser escapado.
     */
    private String montarLayout(String preheader, String conteudo) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Vet Identidade</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:Arial,Helvetica,sans-serif;">
                <!-- Texto de pré-visualização na caixa de entrada -->
                <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:#f1f5f9;">{{PREHEADER}}</div>

                <table role="presentation" width="100%" cellpadding="0" cellspacing="0" border="0" bgcolor="#f1f5f9">
                    <tr>
                        <td align="center" style="padding:32px 16px;">
                            <table role="presentation" width="600" cellpadding="0" cellspacing="0" border="0"
                                   style="width:100%;max-width:600px;background-color:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #e2e8f0;">
                                <tr>
                                    <td align="center" bgcolor="#0f766e" style="padding:24px;">
                                        <span style="font-size:20px;font-weight:bold;letter-spacing:0.5px;color:#ffffff;">🐾 Vet Identidade</span>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding:32px 32px 24px;">
                                        {{CONTEUDO}}
                                    </td>
                                </tr>
                            </table>
                            <p style="margin:16px 0 0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © Vet Identidade · Este é um e-mail automático, por favor não responda.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.replace("{{PREHEADER}}", HtmlUtils.htmlEscape(preheader))
                .replace("{{CONTEUDO}}", conteudo);
    }
}