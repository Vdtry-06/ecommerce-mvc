package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    Long id;
    String name;
    String description;
    Double availableQuantity;
    BigDecimal price;
    CategoryResponse category;
    String imageUrl;
    Set<ToppingResponse> toppings;
}
