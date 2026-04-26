package com.gestion.chat_app.Services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envoie un email HTML à l'adresse destinataire indiquée.
     *
     * @param to      adresse email du destinataire
     * @param subject sujet de l'email
     * @param body    corps de l'email (HTML accepté)
     */
    public void send(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email à : " + to, e);
        }
    }
}
