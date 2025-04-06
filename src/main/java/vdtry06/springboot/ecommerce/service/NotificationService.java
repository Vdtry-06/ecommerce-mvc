package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.entity.Notification;
import vdtry06.springboot.ecommerce.dto.request.NotificationRequest;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.repository.NotificationRepository;
import vdtry06.springboot.ecommerce.entity.User;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    KafkaProducerService kafkaProducerService;

    @Transactional
    public void createPaymentNotification(User user, Payment payment) {
        log.info("Creating payment notification for payment: {}", payment.getId());

        boolean notificationExists = payment.getNotifications().stream()
                .anyMatch(n -> n.getType() == NotificationType.PAYMENT_CONFIRMATION);

        if (!notificationExists) {
            Notification notification = Notification.builder()
                    .type(NotificationType.PAYMENT_CONFIRMATION)
                    .notificationDate(LocalDateTime.now())
                    .payment(payment)
                    .order(payment.getOrder())
                    .build();

            notificationRepository.save(notification);
            payment.getNotifications().add(notification);
            payment.getOrder().getNotifications().add(notification);
            log.info("Created new notification with ID {} for payment {}", notification.getId(), payment.getId());
        } else {
            log.warn("Notification PAYMENT_CONFIRMATION already exists for payment {}", payment.getId());
        }

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .orderReference(payment.getReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .build();

        kafkaProducerService.sendNotification(notificationRequest);
    }

    @Transactional
    public void cancelPaymentedNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CANCELLATION)
                .notificationDate(order.getLastModifiedDate())
                .order(order)
                .payment(null)
                .build();

        notificationRepository.save(notification);
        order.getNotifications().add(notification);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createDeliveredNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.DELIVERY_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .order(order)
                .payment(null)
                .build();

        notificationRepository.save(notification);
        order.getNotifications().add(notification);
        notificationRepository.save(notification);
    }
}