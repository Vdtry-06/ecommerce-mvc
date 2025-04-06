package vdtry06.springboot.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.NotificationType;

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

        @ManyToOne
        @JoinColumn(name = "order_id")
        Order order;

        @ManyToOne
        @JoinColumn(name = "payment_id")
        Payment payment;

        @PrePersist
        protected void onCreate() {
                this.notificationDate = LocalDateTime.now();
        }
}

