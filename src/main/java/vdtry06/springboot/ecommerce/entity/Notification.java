package vdtry06.springboot.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.NotificationType;
import vdtry06.springboot.ecommerce.kafka.OrderConfirmation;
import vdtry06.springboot.ecommerce.kafka.PaymentConfirmation;


import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    NotificationType type;
    LocalDateTime notificationDate;
    OrderConfirmation orderConfirmation;
    PaymentConfirmation paymentConfirmation;
}
