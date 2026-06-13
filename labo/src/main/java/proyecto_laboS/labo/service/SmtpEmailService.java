package proyecto_laboS.labo.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import proyecto_laboS.labo.config.MailProperties;

@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private static final int EXPIRACION_MINUTOS = 30;

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final MailProperties mailProperties;

    public SmtpEmailService(ObjectProvider<JavaMailSender> mailSenderProvider, MailProperties mailProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailProperties = mailProperties;
    }

    @Override
    public boolean enviarRecuperacionPassword(String destinatario, String token) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("No hay JavaMailSender disponible; se omite el envío del mail de recuperación");
            return false;
        }

        if (!mailProperties.isEnabled()) {
            log.info("Envio de mail deshabilitado (labo.mail.enabled=false); se omite el envio");
            return false;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, false, StandardCharsets.UTF_8.name());
            helper.setFrom(resolverFrom());
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña - Clínica AFA");
            helper.setText(cuerpoTexto(destinatario, token), false);
            mailSender.send(mensaje);
            return true;
        } catch (MessagingException ex) {
            log.error("No se pudo enviar el mail de recuperación a {}", destinatario, ex);
            return false;
        } catch (Exception ex) {
            log.error("Fallo inesperado al enviar el mail de recuperación a {}", destinatario, ex);
            return false;
        }
    }

    private String resolverFrom() {
        String from = mailProperties.getFrom();
        return (from == null || from.isBlank()) ? "no-reply@afa.local" : from;
    }

    private String cuerpoTexto(String destinatario, String token) {
        return "Hola,"
                + "\n\nRecibimos una solicitud para restablecer la contraseña de la cuenta asociada a "
                + destinatario + "."
                + "\n\nCódigo de recuperación: " + token
                + "\n\nEste código vence en " + EXPIRACION_MINUTOS + " minutos."
                + "\n\nSi no realizaste esta solicitud, podés ignorar este mensaje."
                + "\n\nClínica AFA";
    }
}
