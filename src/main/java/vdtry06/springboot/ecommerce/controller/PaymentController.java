package vdtry06.springboot.ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.service.PaymentService;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.service.OrderService;
import vdtry06.springboot.ecommerce.entity.OrderLine;
import vdtry06.springboot.ecommerce.dto.response.VNPayResponse;
import vdtry06.springboot.ecommerce.entity.Product;

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

    @PostMapping("/vn-pay-selected")
    public ApiResponse<VNPayResponse> paySelectedItems(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
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
                Long productId = ((Number) data.get("productId")).longValue();
                Integer quantity = ((Number) data.get("quantity")).intValue();
                BigDecimal price = new BigDecimal(data.get("price").toString());

                orderLine.setProduct(Product.builder().id(productId).build());
                orderLine.setQuantity(quantity);
                orderLine.setPrice(price);

                log.info("Processing order line: productId={}, quantity={}, price={}", productId, quantity, price);
                return orderLine;
            }).collect(Collectors.toList());

            VNPayResponse vnPayResponse = paymentService.createVNPayPaymentForSelectedItems(request, selectedOrderLines, userId);

            return ApiResponse.<VNPayResponse>builder()
                    .message("VN payment requested for selected items")
                    .data(vnPayResponse)
                    .build();
        } catch (Exception e) {
            log.error("Error processing payment request: {}", e.getMessage(), e);
            return ApiResponse.<VNPayResponse>builder()
                    .code(500)
                    .message("Error processing payment: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/vn-pay-callback")
    public ResponseEntity<Void> payCallback(HttpServletRequest request) {
        try {
            String status = request.getParameter("vnp_ResponseCode");
            String txnRef = request.getParameter("vnp_TxnRef");

            log.info("VN payment callback received: status={}, txnRef={}", status, txnRef);

            if (txnRef == null || txnRef.isEmpty()) {
                log.error("Invalid transaction reference in callback");
                return ResponseEntity.badRequest().build();
            }

            Long orderId = Long.parseLong(txnRef);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new AppException(ErrorCode.ORDER_NOT_FOUND);
                    });

            if ("00".equals(status)) {
                log.info("Payment successful for order: {}", orderId);
                paymentService.updateOrderToPaid(order);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/"))
                        .build();
            } else {
                log.warn("Payment failed for order: {}, status: {}", orderId, status);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/checkout?error=payment_failed"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error processing payment callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/checkout?error=system_error"))
                    .build();
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long orderId) {
        try {
            log.info("Cancelling payment for order: {}", orderId);
            paymentService.cancelPayment(orderId);
            return ApiResponse.<Void>builder()
                    .message("Payment canceled successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error cancelling payment: {}", e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Error cancelling payment: " + e.getMessage())
                    .build();
        }
    }
}

