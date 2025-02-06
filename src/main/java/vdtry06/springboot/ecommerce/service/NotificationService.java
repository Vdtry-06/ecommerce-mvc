package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.entity.Notification;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.repository.NotificationRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;

    @Transactional
    public void createPaymentNotification(Payment payment) {
        Notification notification = Notification.builder()
                .type(NotificationType.PAYMENT_CONFIRMATION)
                .notificationDate(LocalDateTime.now())
                .payment(payment)
                .build();

        notificationRepository.save(notification);
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
