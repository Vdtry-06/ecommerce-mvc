package vdtry06.springboot.ecommerce.dto.request.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineRequest {
    Long id;
    Long orderId;
    Long productId;
    Double quantity;
}
