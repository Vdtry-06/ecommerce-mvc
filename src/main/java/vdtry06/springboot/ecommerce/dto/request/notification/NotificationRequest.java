package vdtry06.springboot.ecommerce.dto.request.notification;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

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
