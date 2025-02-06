package vdtry06.springboot.ecommerce.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String orderReference;
    BigDecimal amount;
    PaymentMethod paymentMethod;
    String userFirstName;
    String userLastName;
    String userEmail;
}
