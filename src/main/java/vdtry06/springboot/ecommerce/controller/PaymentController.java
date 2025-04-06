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
import vdtry06.springboot.ecommerce.dto.response.PaymentResponse;
import vdtry06.springboot.ecommerce.dto.request.PaymentRequest;
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

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody PaymentRequest paymentRequest) {
        log.info("Creating payment for orderId: {}", paymentRequest.getOrderId());
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);
        return ApiResponse.<PaymentResponse>builder()
                .message("Payment created successfully")
                .data(paymentResponse)
                .build();
    }

    @PostMapping("/vn-pay-selected")
    public ApiResponse<VNPayResponse> paySelectedItems(HttpServletRequest request, @RequestBody Map<String, Object> body) {
        log.info("Requesting VNPay payment for selected items");

        Long userId = ((Number) body.get("userId")).longValue();
        List<Map<String, Object>> orderLinesData = (List<Map<String, Object>>) body.get("orderLines");

        if (orderLinesData == null || orderLinesData.isEmpty()) {
            log.warn("No items selected for payment");
            return ApiResponse.<VNPayResponse>builder()
                    .code(ErrorCode.NO_ITEMS_SELECTED.getCode())
                    .message(ErrorCode.NO_ITEMS_SELECTED.getMessage())
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

        log.info("VN payment callback received with txnRef (orderId): {}", txnRef);

        if (txnRef == null || txnRef.isEmpty()) {
            log.error("txnRef is null or empty");
            return ResponseEntity.badRequest().build();
        }

        try {
            Long orderId = Long.parseLong(txnRef);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found for orderId: {}", orderId);
                        return new AppException(ErrorCode.ORDER_NOT_FOUND);
                    });

            if ("00".equals(status)) {
                log.info("Payment successful for orderId: {}", orderId);
                paymentService.updateOrderToPaid(order);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/?payment=success")) // Redirect với thông báo thành công
                        .build();
            } else {
                log.warn("Payment failed for orderId: {} with response code: {}", orderId, status);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create("http://localhost:3000/checkout?error=payment_failed")) // Redirect với thông báo lỗi
                        .build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid txnRef format: {}", txnRef, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing VNPay callback for txnRef: {}", txnRef, e);
            throw new AppException(ErrorCode.UNEXPECTED_ERROR);
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long orderId) {
        log.info("Canceling payment for orderId: {}", orderId);
        paymentService.cancelPayment(orderId);
        return ApiResponse.<Void>builder()
                .message("Payment canceled successfully")
                .build();
    }
}