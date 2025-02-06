package vdtry06.springboot.ecommerce.kafka;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.payment.dto.PaymentConfirmation;
import vdtry06.springboot.ecommerce.notification.Notification;
import vdtry06.springboot.ecommerce.payment.Payment;
import vdtry06.springboot.ecommerce.notification.NotificationRepository;
import vdtry06.springboot.ecommerce.notification.EmailService;

import java.time.LocalDateTime;

import static vdtry06.springboot.ecommerce.core.constant.NotificationType.PAYMENT_CONFIRMATION;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class KafkaConsumerService {
    EmailService emailService;
    NotificationRepository notificationRepository;

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
    public void consumePaymentSuccessNotifications(PaymentConfirmation paymentConfirmation) throws MessagingException {
        log.info("Consuming the message from payment-topic Topic:: {}", paymentConfirmation);

        Payment paymentEntity = Payment.builder()
                .amount(paymentConfirmation.getAmount())
                .paymentMethod(paymentConfirmation.getPaymentMethod())
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        notificationRepository.save(
                Notification.builder()
                        .type(PAYMENT_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .payment(paymentEntity)
                        .build()
        );

        var customerName = paymentConfirmation.getUserFirstName() + " " + paymentConfirmation.getUserLastName();
        emailService.sendPaymentSuccessEmail(
                paymentConfirmation.getUserEmail(),
                customerName,
                paymentConfirmation.getAmount(),
                paymentConfirmation.getOrderReference()
        );
    }

}
