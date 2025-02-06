package vdtry06.springboot.ecommerce.order.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.product.dto.ProductPurchaseResponse;
import vdtry06.springboot.ecommerce.user.dto.UserResponse;

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
