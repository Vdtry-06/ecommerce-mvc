package vdtry06.springboot.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    double amount;
    String status;

    @OneToOne(mappedBy = "payment")
    Order order;

    @OneToOne(mappedBy = "payment")
    Notification notification;

}
