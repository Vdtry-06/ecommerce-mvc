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
    public void createPaymentNotification(User user, Payment payment, Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .payment(payment)
                .order(order)
                .build();

        notificationRepository.save(notification);

        if (payment.getNotifications() == null) {
            payment.setNotifications(new java.util.ArrayList<>());
        }
        payment.getNotifications().add(notification);

        if (order.getNotifications() == null) {
            order.setNotifications(new java.util.ArrayList<>());
        }
        order.getNotifications().add(notification);

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

