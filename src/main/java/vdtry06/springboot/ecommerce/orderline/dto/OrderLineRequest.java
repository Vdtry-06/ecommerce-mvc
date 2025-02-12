package vdtry06.springboot.ecommerce.orderline.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineRequest {
    Long productId;
    Integer quantity;
}
