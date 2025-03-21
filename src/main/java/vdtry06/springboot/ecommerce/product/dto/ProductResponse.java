package vdtry06.springboot.ecommerce.product.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.category.dto.CategoryResponse;
import vdtry06.springboot.ecommerce.topping.dto.ToppingResponse;

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
    Set<CategoryResponse> categories;
    String imageUrl;
    Set<ToppingResponse> toppings;
}
