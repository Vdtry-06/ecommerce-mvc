package vdtry06.springboot.ecommerce.orderline.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
