package vdtry06.springboot.ecommerce.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.order.OrderRepository;
import vdtry06.springboot.ecommerce.order.OrderService;
import vdtry06.springboot.ecommerce.payment.dto.PaymentResponse;
import vdtry06.springboot.ecommerce.payment.dto.PaymentRequest;
import vdtry06.springboot.ecommerce.payment.dto.VNPayResponse;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class PaymentController {
    PaymentService paymentService;
    OrderRepository orderRepository;
    OrderService orderService;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest) {
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
        return ApiResponse.<PaymentResponse>builder()
                .message("Payment created successfully")
                .data(paymentResponse)
                .build();
    }

    @GetMapping("/vn-pay")
    public ApiResponse<VNPayResponse> pay(HttpServletRequest request, @RequestParam(required = false) Long orderId) {
        if(orderId == null) {
            return ApiResponse.<VNPayResponse>builder()
                    .code(400)
                    .message("Order ID is required")
                    .build();
        }

        VNPayResponse vnPayResponse = paymentService.createVNPayPayment(request, orderId);
        return ApiResponse.<VNPayResponse>builder()
                .message("VN payment requested")
                .data(vnPayResponse)
                .build();
    }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<VNPayResponse> payCallback(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        log.info("VN payment callback txnRef (orderId): " + txnRef);

        if (txnRef == null || txnRef.isEmpty()) {
            return ApiResponse.<VNPayResponse>builder()
                    .code(400)
                    .message("Invalid transaction reference")
                    .build();
        }
        Long orderId = Long.parseLong(txnRef);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(status.equals("00")) {
            paymentService.updateOrderToPaid(order);
            return ApiResponse.<VNPayResponse>builder()
                    .message("VN payment successful")
                    .data(new VNPayResponse())
                    .build();
        } else {
            return ApiResponse.<VNPayResponse>builder()
                    .code(400)
                    .message("VN payment failed")
                    .build();
        }
    }

//    @GetMapping("/vn-pay-callback")
//    public ApiResponse<VNPayResponse> payCallbackHandler(HttpServletRequest request) {
//        String status = request.getParameter("vnp_ResponseCode");
//
//        if(status.equals("00")) {
//            return ApiResponse.<VNPayResponse>builder()
//                    .code(200)
//                    .message("VN payment callback handled")
//                    .data(new VNPayResponse())
//                    .build();
//        } else {
//            return ApiResponse.<VNPayResponse>builder()
//                    .code(400)
//                    .message("VN payment callback handled")
//                    .data(null)
//                    .build();
//        }
//    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPayment(orderId);
        return ApiResponse.<Void>builder()
                .message("Payment canceled successfully")
                .build();
    }
}
