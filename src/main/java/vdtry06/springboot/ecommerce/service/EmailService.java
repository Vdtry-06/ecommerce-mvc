package vdtry06.springboot.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vdtry06.springboot.ecommerce.constant.NotificationType;

import java.math.BigDecimal;


@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        String htmlContent = templateEngine.process(NotificationType.EMAIL_VERIFICATION.getTemplate(), context);
        helper.setTo(to);
        helper.setSubject(NotificationType.EMAIL_VERIFICATION.getSubject());
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendPaymentSuccessEmail(
            String destinationEmail,
            String username,
            BigDecimal amount,
            String orderReference
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        Context context = new Context();
        context.setVariable("destinationEmail", destinationEmail);
        context.setVariable("username", username);
        context.setVariable("amount", amount);
        context.setVariable("orderReference", orderReference);

        String htmlContent = templateEngine.process(NotificationType.PAYMENT_CONFIRMATION.getTemplate(), context);
        helper.setTo(destinationEmail);
        helper.setSubject(NotificationType.PAYMENT_CONFIRMATION.getSubject());
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }
}
