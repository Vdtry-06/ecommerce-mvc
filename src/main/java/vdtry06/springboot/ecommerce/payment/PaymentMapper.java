package vdtry06.springboot.ecommerce.payment;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.payment.dto.PaymentResponse;
import vdtry06.springboot.ecommerce.payment.dto.PaymentRequest;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentRequest request);

    PaymentResponse toPaymentResponse(Payment request);
}
