package vdtry06.springboot.ecommerce.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.dto.request.product.ProductPurchaseRequest;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    String reference;

    @Positive(message = "Order amount should be positive")
    BigDecimal amount;

    @NotNull(message = "Payment method should be precised")
    PaymentMethod paymentMethod;

    @NotNull(message = "Customer should be present")
    @NotEmpty(message = "Customer should be present")
    @NotBlank(message = "Customer should be present")
    Long userId;

    @NotEmpty(message = "You should at least purchase one product")
    List<ProductPurchaseRequest> products;
}
