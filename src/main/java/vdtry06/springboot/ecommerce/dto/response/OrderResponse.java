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
    OrderStatus status;
    BigDecimal totalPrice;
    Long userId;
    List<OrderLineResponse> orderLines;
    List<PaymentResponse> payments;
    List<NotificationResponse> notifications;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
}