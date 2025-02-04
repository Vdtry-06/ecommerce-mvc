package vdtry06.springboot.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.payment.PaymentRequest;
import vdtry06.springboot.ecommerce.service.PaymentService;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping
    public ApiResponse<Long> createPayment(@RequestBody @Valid PaymentRequest payment) {
        return ApiResponse.<Long>builder()
                .data(paymentService.createPayment(payment))
                .build();
    }
}
