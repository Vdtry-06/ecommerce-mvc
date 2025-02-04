package vdtry06.springboot.ecommerce.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.dto.response.user.UserResponse;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    BigDecimal amount;
    PaymentMethod paymentMethod;
    Long orderId;
    String orderReference;
    UserResponse user;
}
