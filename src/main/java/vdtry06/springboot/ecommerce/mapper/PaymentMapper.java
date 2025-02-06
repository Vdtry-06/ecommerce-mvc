package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vdtry06.springboot.ecommerce.dto.request.payment.PaymentRequest;
import vdtry06.springboot.ecommerce.dto.response.payment.PaymentResponse;
import vdtry06.springboot.ecommerce.entity.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toPayment(PaymentRequest request);

    PaymentResponse toPaymentResponse(Payment request);
}
