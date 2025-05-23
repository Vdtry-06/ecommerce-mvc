package vdtry06.springboot.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment")
@EntityListeners(AuditingEntityListener.class)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String reference;
    BigDecimal amount;

    @Enumerated(STRING)
    PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    List<Notification> notifications = new ArrayList<>();

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

