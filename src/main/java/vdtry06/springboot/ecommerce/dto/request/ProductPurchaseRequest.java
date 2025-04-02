package vdtry06.springboot.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductPurchaseRequest {
    @NotNull(message = "Product is mandatory")
    Long productId;

    @Positive(message = "Quantity is mandatory")
    Double quantity;

}
