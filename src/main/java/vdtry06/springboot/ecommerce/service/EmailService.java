package vdtry06.springboot.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.dto.response.PaymentConfirmation;

import java.math.BigDecimal;
import java.util.List;

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

    @Retryable(value = MessagingException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendPaymentSuccessEmail(
            String destinationEmail,
            String username,
            BigDecimal amount,
            String orderReference,
            List<PaymentConfirmation.OrderLineDetails> orderLines
    ) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        Context context = new Context();
        context.setVariable("destinationEmail", destinationEmail);
        context.setVariable("username", username);
        context.setVariable("amount", amount);
        context.setVariable("orderReference", orderReference);
        context.setVariable("orderLines", orderLines);

        log.info(destinationEmail);

        String htmlContent = templateEngine.process(NotificationType.PAYMENT_CONFIRMATION.getTemplate(), context);
        helper.setTo(destinationEmail);
        helper.setSubject(NotificationType.PAYMENT_CONFIRMATION.getSubject());
        helper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }
}