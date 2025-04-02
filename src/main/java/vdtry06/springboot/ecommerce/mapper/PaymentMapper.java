package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.dto.response.PaymentResponse;
import vdtry06.springboot.ecommerce.dto.request.PaymentRequest;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentRequest request);

    PaymentResponse toPaymentResponse(Payment request);
}
