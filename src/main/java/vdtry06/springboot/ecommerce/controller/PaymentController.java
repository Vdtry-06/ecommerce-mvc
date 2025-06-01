package vdtry06.springboot.ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.constant.OrderStatus;
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
    RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/vn-pay-selected")
    public ApiResponse<VNPayResponse> paySelectedItems(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        try {
            Long userId = ((Number) body.get("userId")).longValue();
            List<Long> selectedProductIds = ((List<?>) body.get("selectedProductIds")).stream()
                    .map(id -> ((Number) id).longValue())
                    .collect(Collectors.toList());

            if (selectedProductIds == null || selectedProductIds.isEmpty()) {
                return ApiResponse.<VNPayResponse>builder()
                        .code(400)
                        .message("No items selected for payment")
                        .build();
            }

            log.info("Processing payment for userId={}, selectedProductIds={}", userId, selectedProductIds);

            VNPayResponse vnPayResponse = paymentService.createVNPayPaymentForSelectedItems(request, userId, selectedProductIds);

            return ApiResponse.<VNPayResponse>builder()
                    .code(1000)
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
                        .location(URI.create("http://localhost:3000/cart?payment=success"))
                        .build();
            } else {
                log.warn("Payment failed for order: {}, status: {}", orderId, status);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // Deselect and remove items from Redis on cancellation
                Long userId = order.getUser().getId();
                String cartKey = "cart:user:" + userId;
                for (OrderLine orderLine : order.getOrderLines()) {
                    Long productId = orderLine.getProduct().getId();
                    redisTemplate.opsForHash().delete(cartKey, productId.toString());
                    log.info("Removed product {} from cart:user:{}", productId, userId);
                }

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

    @GetMapping("/zalo-pay-callback")
    public ResponseEntity<Void> zaloPayCallback(HttpServletRequest request) {
        try {
            String status = request.getParameter("status"); // Adjust based on ZaloPay API
            String orderIdStr = request.getParameter("orderId"); // Adjust based on ZaloPay API

            log.info("ZaloPay callback received: status={}, orderId={}", status, orderIdStr);

            if (orderIdStr == null || orderIdStr.isEmpty()) {
                log.error("Invalid order ID in ZaloPay callback");
                return ResponseEntity.badRequest().build();
            }

            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found: {}", orderId);
                        return new AppException(ErrorCode.ORDER_NOT_FOUND);
                    });

            if ("1".equals(status)) { // Assume "1" is success for ZaloPay
                log.info("ZaloPay payment successful for order: {}", orderId);
                paymentService.updateOrderToPaid(order);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/cart?payment=success"))
                        .build();
            } else {
                log.warn("ZaloPay payment failed/canceled for order: {}, status: {}", orderId, status);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // Deselect and remove items from Redis on cancellation
                Long userId = order.getUser().getId();
                String cartKey = "cart:user:" + userId;
                for (OrderLine orderLine : order.getOrderLines()) {
                    Long productId = orderLine.getProduct().getId();
                    redisTemplate.opsForHash().delete(cartKey, productId.toString());
                    log.info("Removed product {} from cart:user:{}", productId, userId);
                }

                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/checkout?error=payment_failed"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Error processing ZaloPay callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3000/checkout?error=system_error"))
                    .build();
        }
    }
}

