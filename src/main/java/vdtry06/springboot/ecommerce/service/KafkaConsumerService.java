package vdtry06.springboot.ecommerce.service;

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
import vdtry06.springboot.ecommerce.dto.response.PaymentConfirmation;
import vdtry06.springboot.ecommerce.entity.Notification;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.NotificationRepository;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;

import static vdtry06.springboot.ecommerce.constant.NotificationType.PAYMENT_CONFIRMATION;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class KafkaConsumerService {
    EmailService emailService;
    NotificationRepository notificationRepository;
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    @KafkaListener(topics = "verification-codes")
    public void listen(ConsumerRecord<String, String> record) {
        String[] data = record.value().split(",");
        String email = data[0];
        String verificationCode = data[1];

        try {
            emailService.sendVerificationEmail(email, verificationCode);
            log.info("Verification email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
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

            Order order = orderRepository.findById(paymentConfirmation.getOrderId())
                    .orElseThrow(() -> {
                        log.error("Order not found for ID: {}", paymentConfirmation.getOrderId());
                        return new AppException(ErrorCode.ORDER_NOT_FOUND);
                    });

            log.info("Processing payment: OrderReference={}, Amount={}, PaymentMethod={}, Username={}, UserEmail={}",
                    paymentConfirmation.getOrderReference(),
                    paymentConfirmation.getAmount(),
                    paymentConfirmation.getPaymentMethod(),
                    paymentConfirmation.getUsername(),
                    paymentConfirmation.getUserEmail());

            Payment paymentEntity = Payment.builder()
                    .amount(paymentConfirmation.getAmount())
                    .paymentMethod(paymentConfirmation.getPaymentMethod())
                    .order(order)
                    .reference(paymentConfirmation.getOrderReference())
                    .createdDate(LocalDateTime.now())
                    .lastModifiedDate(LocalDateTime.now())
                    .build();

            paymentRepository.save(paymentEntity);

            Notification notification = Notification.builder()
                    .type(PAYMENT_CONFIRMATION)
                    .notificationDate(LocalDateTime.now())
                    .payment(paymentEntity)
                    .order(order)
                    .build();

            notificationRepository.save(notification);

            String customerName = paymentConfirmation.getUsername();
            List<PaymentConfirmation.OrderLineDetails> orderLineDetails = paymentConfirmation.getOrderLines();
            if (orderLineDetails == null || orderLineDetails.isEmpty()) {
                log.warn("No order line details found for payment confirmation: {}", paymentConfirmation.getOrderReference());
            }
            try {
                emailService.sendPaymentSuccessEmail(
                        paymentConfirmation.getUserEmail(),
                        customerName,
                        paymentConfirmation.getAmount(),
                        paymentConfirmation.getOrderReference(),
                        orderLineDetails
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