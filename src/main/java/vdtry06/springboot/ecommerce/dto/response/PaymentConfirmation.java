package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmation {
    Long orderId;
    String orderReference;
    BigDecimal amount;
    PaymentMethod paymentMethod;
    String username;
    String userEmail;
    List<OrderLineDetails> orderLines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderLineDetails {
        Long productId;
        Integer quantity;
        String productImageUrl;
        String productName;
        BigDecimal price;
    }
}
