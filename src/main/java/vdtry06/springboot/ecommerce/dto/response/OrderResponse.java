package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.constant.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Long userId;
    OrderStatus status;
    BigDecimal totalPrice;
    List<OrderLineResponse> orderLines;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
}
