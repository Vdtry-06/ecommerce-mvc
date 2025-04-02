package vdtry06.springboot.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    Long id;
    String reference;
    BigDecimal amount;
    PaymentMethod paymentMethod;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;
}