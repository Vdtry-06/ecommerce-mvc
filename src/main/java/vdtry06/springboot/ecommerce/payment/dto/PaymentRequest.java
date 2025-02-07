package vdtry06.springboot.ecommerce.payment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.user.dto.UserResponse;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    PaymentMethod paymentMethod;
    Long orderId;
    String orderReference;
    UserResponse user;
}
