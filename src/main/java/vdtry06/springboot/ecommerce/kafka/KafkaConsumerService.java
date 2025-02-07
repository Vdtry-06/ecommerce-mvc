package vdtry06.springboot.ecommerce.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.notification.Notification;
import vdtry06.springboot.ecommerce.payment.Payment;
import vdtry06.springboot.ecommerce.notification.NotificationRepository;
import vdtry06.springboot.ecommerce.notification.EmailService;
import vdtry06.springboot.ecommerce.payment.PaymentRepository;
import vdtry06.springboot.ecommerce.payment.dto.PaymentConfirmation;

import java.time.LocalDateTime;

import static vdtry06.springboot.ecommerce.core.constant.NotificationType.PAYMENT_CONFIRMATION;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class KafkaConsumerService {
    EmailService emailService;
    NotificationRepository notificationRepository;
    PaymentRepository paymentRepository;

    @KafkaListener(topics = "verification-codes")
    public void listen(ConsumerRecord<String, String> record) {
        String[] data = record.value().split(",");
        String email = data[0];
        String verificationCode = data[1];

        try {
            emailService.sendVerificationEmail(email, verificationCode);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "payment-topic")
    public void consumePaymentMessage(ConsumerRecord<String, String> record) {
        String message = record.value();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            PaymentConfirmation paymentConfirmation = objectMapper.readValue(message, PaymentConfirmation.class);

            if (paymentConfirmation.getAmount() == null) {
                log.error("ERROR - Received null amount in message: {}", message);
                return;
            }

            log.info("Processing payment: OrderReference={}, Amount={}, PaymentMethod={}, User={} {} ({})",
                    paymentConfirmation.getOrderReference(),
                    paymentConfirmation.getAmount(),
                    paymentConfirmation.getPaymentMethod(),
                    paymentConfirmation.getUsername(),
                    paymentConfirmation.getUserEmail());

            Payment paymentEntity = Payment.builder()
                    .amount(paymentConfirmation.getAmount())
                    .paymentMethod(paymentConfirmation.getPaymentMethod())
                    .createdDate(LocalDateTime.now())
                    .lastModifiedDate(LocalDateTime.now())
                    .build();

            paymentRepository.save(paymentEntity);

            notificationRepository.save(
                    Notification.builder()
                            .type(PAYMENT_CONFIRMATION)
                            .notificationDate(LocalDateTime.now())
                            .payment(paymentEntity)
                            .build()
            );

            String customerName = paymentConfirmation.getUsername();
            try {
                emailService.sendPaymentSuccessEmail(
                        paymentConfirmation.getUserEmail(),
                        customerName,
                        paymentConfirmation.getAmount(),
                        paymentConfirmation.getOrderReference()
                );
                log.info("Payment success email sent to {}", paymentConfirmation.getUserEmail());
            } catch (MessagingException e) {
                log.error("Failed to send email to {}: {}", paymentConfirmation.getUserEmail(), e.getMessage());
            }

        } catch (JsonProcessingException e) {
            log.error("ERROR - Invalid JSON format: {}", message, e);
        } catch (Exception e) {
            log.error("ERROR - Unexpected error: {}", e.getMessage(), e);
        }
    }

}
