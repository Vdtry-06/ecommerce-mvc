package vdtry06.springboot.ecommerce.order.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineRequest;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    Long userId;
    List<OrderLineRequest> orderLines;
    PaymentMethod paymentMethod;
}
