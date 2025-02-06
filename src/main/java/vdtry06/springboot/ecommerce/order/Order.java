package vdtry06.springboot.ecommerce.order;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import vdtry06.springboot.ecommerce.core.constant.OrderStatus;
import vdtry06.springboot.ecommerce.notification.Notification;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.payment.Payment;
import vdtry06.springboot.ecommerce.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String username;
    String email;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderLine> orderLines;

    @OneToOne
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    Notification notification;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    LocalDateTime lastModifiedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }
}
