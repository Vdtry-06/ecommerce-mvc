package vdtry06.springboot.ecommerce.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.dto.response.product.ProductPurchaseResponse;
import vdtry06.springboot.ecommerce.dto.response.user.UserResponse;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderConfirmation {
    String orderReference;
    BigDecimal totalAmount;
    PaymentMethod paymentMethod;
    UserResponse user;
    List<ProductPurchaseResponse> products;
}
