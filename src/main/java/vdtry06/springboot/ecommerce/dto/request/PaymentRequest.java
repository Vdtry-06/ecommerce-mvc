package vdtry06.springboot.ecommerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.dto.response.UserResponse;

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
