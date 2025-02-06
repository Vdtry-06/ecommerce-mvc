package vdtry06.springboot.ecommerce.order.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.OrderStatus;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineResponse;

import java.math.BigDecimal;
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
}
