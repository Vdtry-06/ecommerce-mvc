package vdtry06.springboot.ecommerce.dto.response.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderLineResponse {
    Long id;
    Double quantity;
}
