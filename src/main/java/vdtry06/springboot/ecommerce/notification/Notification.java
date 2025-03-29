package vdtry06.springboot.ecommerce.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.NotificationType;
import vdtry06.springboot.ecommerce.payment.Payment;
import vdtry06.springboot.ecommerce.order.Order;

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

        @Enumerated(EnumType.STRING)
        NotificationType type;

        LocalDateTime notificationDate;

        @OneToOne
        @JoinColumn(name = "order_id", referencedColumnName = "id")
        Order order;

        @OneToOne(mappedBy = "notification")
        Payment payment;
}
