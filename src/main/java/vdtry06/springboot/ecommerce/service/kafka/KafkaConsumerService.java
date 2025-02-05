package vdtry06.springboot.ecommerce.service.kafka;

import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.response.order.OrderConfirmation;
import vdtry06.springboot.ecommerce.dto.response.payment.PaymentConfirmation;
import vdtry06.springboot.ecommerce.entity.Notification;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.repository.NotificationRepository;
import vdtry06.springboot.ecommerce.service.email.EmailService;

import java.time.LocalDateTime;

import static vdtry06.springboot.ecommerce.constant.NotificationType.ORDER_CONFIRMATION;
import static vdtry06.springboot.ecommerce.constant.NotificationType.PAYMENT_CONFIRMATION;

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

    @KafkaListener(topics = "order-topic")
    public void consumeOrderConfirmationNotifications(OrderConfirmation orderConfirmation) throws MessagingException {
        log.info("Consuming the message from order-topic Topic:: {}", orderConfirmation);

        Order orderEntity = Order.builder()
                .reference(orderConfirmation.getOrderReference())
                .totalAmount(orderConfirmation.getTotalAmount())
                .paymentMethod(orderConfirmation.getPaymentMethod())
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        notificationRepository.save(
                Notification.builder()
                        .type(ORDER_CONFIRMATION)
                        .notificationDate(LocalDateTime.now())
                        .order(orderEntity)
                        .build()
        );

        var customerName = orderConfirmation.getUser().getFirstName() + " " + orderConfirmation.getUser().getLastName();
        emailService.sendOrderConfirmationEmail(
                orderConfirmation.getUser().getEmail(),
                customerName,
                orderConfirmation.getTotalAmount(),
                orderConfirmation.getOrderReference(),
                orderConfirmation.getProducts()
        );
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
