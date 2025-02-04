package vdtry06.springboot.ecommerce.dto.response.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmation {
    String orderReference;
    BigDecimal amount;
    PaymentMethod paymentMethod;
    String userFirstName;
    String userLastName;
    String userEmail;
}
