package com.br.ssmup.auth.usuario.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String remetente;

    @Async("emailExecutor")
    public void enviarEmailAtivacao(String destinatario, String nomeUsuario, String token) {
        log.info("Iniciando envio de e-mail de ativação para: {}", destinatario);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("SSMUP - Ative sua conta");

            String linkAtivacao = frontendUrl + "/ativar-conta?token=" + token;

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background-color: #1a73e8; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;">
                        <h1 style="color: white; margin: 0;">SSMUP</h1>
                        <p style="color: #e0e0e0; margin: 5px 0 0;">Sistema Sanitário Municipal Unificado de Processos</p>
                    </div>
                    <div style="background-color: #f9f9f9; padding: 30px; border: 1px solid #e0e0e0;">
                        <h2 style="color: #333;">Olá, %s!</h2>
                        <p style="color: #555; font-size: 16px;">
                            Sua conta foi criada no SSMUP. Para começar a usar o sistema, você precisa ativar sua conta clicando no botão abaixo:
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #1a73e8; color: white; padding: 14px 28px; 
                                      text-decoration: none; border-radius: 6px; font-size: 16px; 
                                      font-weight: bold; display: inline-block;">
                                Ativar Minha Conta
                            </a>
                        </div>
                        
                        <p style="color: #777; font-size: 14px;">
                            Se o botão não funcionar, copie e cole o link abaixo no seu navegador:
                        </p>
                        <p style="color: #1a73e8; font-size: 13px; word-break: break-all;">
                            %s
                        </p>
                        
                        <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;">
                        
                        <p style="color: #999; font-size: 12px;">
                            ⚠️ Este link expira em <strong>24 horas</strong>. Caso expire, solicite um novo ao seu coordenador.<br>
                            Se você não solicitou esta conta, ignore este e-mail.
                        </p>
                    </div>
                    
                    <div style="text-align: center; padding: 15px; color: #999; font-size: 11px;">
                        © 2026 SSMUP — Todos os direitos reservados.
                    </div>
                </div>
                """.formatted(nomeUsuario, linkAtivacao, linkAtivacao);

            helper.setText(html, true);
            mailSender.send(mimeMessage);

            log.info("E-mail de ativação enviado com sucesso para: {}", destinatario);

        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail de ativação para: {} - {}", destinatario, e.getMessage());
        }
    }
}
