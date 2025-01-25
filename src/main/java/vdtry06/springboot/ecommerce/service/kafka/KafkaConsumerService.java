package vdtry06.springboot.ecommerce.service.kafka;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import vdtry06.springboot.ecommerce.service.email.EmailService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumerService {
    EmailService emailService;

    public String readHtmlTemplate(String filePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(filePath);
        byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @KafkaListener(topics = "verification-codes")
    public void listen(ConsumerRecord<String, String> record) throws IOException {
        String[] data = record.value().split(",");
        String email = data[0];
        String verificationCode = data[1];

        String subject = "Account Verification";
        String htmlTemplate = readHtmlTemplate("templates/confirm-email.html");
        String htmlMessage = htmlTemplate.replace("{{VERIFICATION_CODE}}", verificationCode);

        try {
            emailService.sendVerificationEmail(email, subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
