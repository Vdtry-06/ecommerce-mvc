package vdtry06.springboot.ecommerce.topping.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ToppingResponse {
    Long id;
    String name;
    BigDecimal price;
}
