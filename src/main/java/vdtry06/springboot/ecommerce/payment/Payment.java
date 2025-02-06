package vdtry06.springboot.ecommerce.payment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.notification.Notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String reference;
    BigDecimal amount;

    @Enumerated(STRING)
    PaymentMethod paymentMethod;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
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
