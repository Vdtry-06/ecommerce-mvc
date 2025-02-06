package vdtry06.springboot.ecommerce.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.dto.response.product.ProductResponse;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineResponse {
    Long id;
    Long productId;
    Integer quantity;
    BigDecimal price;
}
