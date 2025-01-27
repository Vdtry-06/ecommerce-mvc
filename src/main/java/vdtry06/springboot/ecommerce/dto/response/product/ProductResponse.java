package vdtry06.springboot.ecommerce.dto.response.product;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.dto.response.category.CategoryResponse;

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
    Integer availableQuantity;
    Double price;
    Set<CategoryResponse> categories;
}
