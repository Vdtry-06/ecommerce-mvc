package vdtry06.springboot.ecommerce.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long orderId;
    String reference;
    BigDecimal amount;
    PaymentMethod paymentMethod;
    Long userId;
}
