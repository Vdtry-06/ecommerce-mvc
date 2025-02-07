package vdtry06.springboot.ecommerce.notification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.core.constant.NotificationType;
import vdtry06.springboot.ecommerce.kafka.KafkaProducerService;
import vdtry06.springboot.ecommerce.notification.dto.NotificationRequest;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.payment.Payment;
import vdtry06.springboot.ecommerce.user.User;

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
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .payment(payment)
                .build();

        notificationRepository.save(notification);


        // Gửi thông báo qua Kafka
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .orderReference(payment.getReference())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .username(user.getUsername())
                .userEmail(user.getEmail())
                .build();

        kafkaProducerService.sendNotification(notificationRequest);
    }

    public void cancelPaymentedNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CANCELLATION)
                .notificationDate(order.getLastModifiedDate())
                .order(order)
                .build();

        notificationRepository.save(notification);
    }

    public void createDeliveredNotification(Order order) {
        Notification notification = Notification.builder()
                .type(NotificationType.DELIVERY_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .order(order)
                .build();

        notificationRepository.save(notification);
    }
}
