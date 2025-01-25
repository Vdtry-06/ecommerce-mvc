package vdtry06.springboot.ecommerce.service.kafka;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.service.email.EmailService;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumerService {
    EmailService emailService;

    @KafkaListener(topics = "verification-codes")
    public void listen(ConsumerRecord<String, String> record) {
        String[] data = record.value().split(",");
        String email = data[0];
        String verificationCode = data[1];

        String subject = "Account Verification";

        try {
            emailService.sendVerificationEmail(email, subject, verificationCode);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
