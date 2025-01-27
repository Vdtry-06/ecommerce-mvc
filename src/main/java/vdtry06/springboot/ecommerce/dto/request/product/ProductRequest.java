package vdtry06.springboot.ecommerce.dto.request.product;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.entity.Category;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String name;
    String description;
    Integer availableQuantity;
    Double price;
    Set<String> categoryNames;
}
