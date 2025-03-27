package vdtry06.springboot.ecommerce.payment;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.order.OrderRepository;
import vdtry06.springboot.ecommerce.order.OrderService;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.payment.dto.PaymentResponse;
import vdtry06.springboot.ecommerce.payment.dto.PaymentRequest;
import vdtry06.springboot.ecommerce.payment.dto.VNPayResponse;
import vdtry06.springboot.ecommerce.product.Product;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/vn-pay-selected")
    public ApiResponse<VNPayResponse> paySelectedItems(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        Long userId = ((Number) body.get("userId")).longValue();
        List<Map<String, Object>> orderLinesData = (List<Map<String, Object>>) body.get("orderLines");

        if (orderLinesData == null || orderLinesData.isEmpty()) {
            return ApiResponse.<VNPayResponse>builder()
                    .code(400)
                    .message("No items selected for payment")
                    .build();
        }

        List<OrderLine> selectedOrderLines = orderLinesData.stream().map(data -> {
            OrderLine orderLine = new OrderLine();
            orderLine.setProduct(Product.builder().id(((Number) data.get("productId")).longValue()).build());
            orderLine.setQuantity(((Number) data.get("quantity")).intValue());
            orderLine.setPrice(new BigDecimal(data.get("price").toString()));
            return orderLine;
        }).collect(Collectors.toList());

        VNPayResponse vnPayResponse = paymentService.createVNPayPaymentForSelectedItems(request, selectedOrderLines, userId);
        return ApiResponse.<VNPayResponse>builder()
                .message("VN payment requested for selected items")
                .data(vnPayResponse)
                .build();
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<Void> payCallback(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        log.info("VN payment callback txnRef (orderId): " + txnRef);

        if (txnRef == null || txnRef.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Long orderId = Long.parseLong(txnRef);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if ("00".equals(status)) {
            paymentService.updateOrderToPaid(order);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/"))
                    .build();
        } else {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/checkout?error=payment_failed"))
                    .build();
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long orderId) {
        paymentService.cancelPayment(orderId);
        return ApiResponse.<Void>builder()
                .message("Payment canceled successfully")
                .build();
    }
}