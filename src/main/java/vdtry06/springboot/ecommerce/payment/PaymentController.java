package vdtry06.springboot.ecommerce.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.payment.dto.PaymentResponse;
import vdtry06.springboot.ecommerce.payment.dto.PaymentRequest;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
        return ApiResponse.<PaymentResponse>builder()
                .message("Payment created successfully")
                .data(paymentResponse)
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPayment(orderId);
        return ApiResponse.<Void>builder()
                .message("Payment canceled successfully")
                .build();
    }
}
