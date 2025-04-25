package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.dto.response.PaymentConfirmation;
import vdtry06.springboot.ecommerce.entity.Notification;
import vdtry06.springboot.ecommerce.dto.request.NotificationRequest;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.repository.NotificationRepository;
import vdtry06.springboot.ecommerce.entity.User;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    KafkaTemplate<String, String> kafkaTemplate;
    NotificationRepository notificationRepository;
    ObjectMapper objectMapper;

    public void createPaymentNotification(User user, Payment payment, Order order) {
        try {
            PaymentConfirmation paymentConfirmation = PaymentConfirmation.builder()
                    .orderReference(payment.getReference())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .username(user.getUsername())
                    .userEmail(user.getEmail())
                    .orderLines(order.getOrderLines().stream()
                            .map(orderLine -> PaymentConfirmation.OrderLineDetails.builder()
                                    .productId(orderLine.getProduct().getId())
                                    .quantity(orderLine.getQuantity())
                                    .productImageUrl(orderLine.getProduct().getImageUrl())
                                    .productName(orderLine.getProduct().getName())
                                    .price(orderLine.getPrice())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            String message = objectMapper.writeValueAsString(paymentConfirmation);
            kafkaTemplate.send("payment-topic", message);
            log.info("Sent payment confirmation to Kafka for order: {}", payment.getReference());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize payment confirmation: {}", e.getMessage());
        }
    }

    @Transactional
    public void cancelPaymentedNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CANCELLATION)
                .notificationDate(LocalDateTime.now())
                .order(order)
                .build();

        notificationRepository.save(notification);

        if (order.getNotifications() == null) {
            order.setNotifications(new java.util.ArrayList<>());
        }
        order.getNotifications().add(notification);
    }

    @Transactional
    public void createDeliveredNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.DELIVERY_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .order(order)
                .build();

        notificationRepository.save(notification);

        if (order.getNotifications() == null) {
            order.setNotifications(new java.util.ArrayList<>());
        }
        order.getNotifications().add(notification);
    }
}

