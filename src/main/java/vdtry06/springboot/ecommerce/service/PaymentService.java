package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.config.payment.VNPAYConfig;
import vdtry06.springboot.ecommerce.constant.OrderStatus;
import vdtry06.springboot.ecommerce.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.config.vnPay.VNPayUtil;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.entity.*;
import vdtry06.springboot.ecommerce.dto.response.VNPayResponse;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.OrderLineRepository;
import vdtry06.springboot.ecommerce.repository.PaymentRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    OrderLineRepository orderLineRepository;
    ProductRepository productRepository;
    NotificationService notificationService;
    VNPAYConfig vnpayConfig;
    KafkaProducerService kafkaProducerService;
    ObjectMapper objectMapper;

    @Transactional
    public VNPayResponse createVNPayPaymentForSelectedItems(HttpServletRequest request, List<OrderLine> selectedOrderLines, Long userId) {
        if (selectedOrderLines == null || selectedOrderLines.isEmpty()) {
            throw new AppException(ErrorCode.NO_ITEMS_SELECTED);
        }

        BigDecimal totalPrice = selectedOrderLines.stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User user = User.builder().id(userId).build();

        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING);
        Order existingPendingOrder = pendingOrders.isEmpty() ? null : pendingOrders.get(0);

        Order paymentOrder;
        if (existingPendingOrder != null) {
            paymentOrder = createNewOrderForPayment(user, selectedOrderLines, totalPrice);
            removeSelectedItemsFromOrder(existingPendingOrder, selectedOrderLines);
        } else {
            paymentOrder = createNewOrderForPayment(user, selectedOrderLines, totalPrice);
        }

        selectedOrderLines.forEach(line -> {
            Map<String, Object> event = Map.of(
                    "userId", userId,
                    "productId", line.getProduct().getId(),
                    "action", "REMOVE"
            );
            try {
                kafkaProducerService.sendMessage("cart-topic", objectMapper.writeValueAsString(event));
            } catch (Exception e) {
                log.error("Error sending Kafka event for product {}: {}", line.getProduct().getId(), e.getMessage(), e);
            }
        });

        long amount = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();
        String bankCode = request.getParameter("bankCode");

        Map<String, String> vnpParamsMap = vnpayConfig.getVNPAYConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(paymentOrder.getId()));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnpayConfig.getSecretKey(), hashData);

        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl)
                .build();
    }

    private Order createNewOrderForPayment(User user, List<OrderLine> selectedOrderLines, BigDecimal totalPrice) {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .orderLines(new ArrayList<>(selectedOrderLines))
                .payments(new ArrayList<>())
                .notifications(new ArrayList<>())
                .build();
        return orderRepository.save(order);
    }

    private void removeSelectedItemsFromOrder(Order existingOrder, List<OrderLine> selectedOrderLines) {
        List<OrderLine> remainingOrderLines = existingOrder.getOrderLines().stream()
                .filter(orderLine -> selectedOrderLines.stream()
                        .noneMatch(selected -> selected.getProduct().getId().equals(orderLine.getProduct().getId())))
                .collect(Collectors.toList());
        existingOrder.setOrderLines(remainingOrderLines);
        orderRepository.save(existingOrder);
    }

    @Transactional
    public void updateOrderToPaid(Order order) {
        if (OrderStatus.PAID.equals(order.getStatus())) {
            log.info("Order {} is already paid, no update needed", order.getId());
            return;
        }

        try {
            updateProductQuantity(order, false);

            Payment payment = order.getPayments() != null && !order.getPayments().isEmpty()
                    ? order.getPayments().get(0)
                    : null;
            if (payment == null) {
                payment = Payment.builder()
                        .reference("VNPAY_" + order.getId())
                        .amount(order.getTotalPrice())
                        .paymentMethod(PaymentMethod.VNPAY)
                        .order(order)
                        .notifications(new ArrayList<>())
                        .build();

                paymentRepository.save(payment);

                if (order.getPayments() == null) {
                    order.setPayments(new ArrayList<>());
                }
                order.getPayments().add(payment);
                log.info("Created new payment for order: {}", order.getId());
            } else {
                payment.setAmount(order.getTotalPrice());
                payment.setPaymentMethod(PaymentMethod.VNPAY);
                paymentRepository.save(payment);
                log.info("Updated existing payment for order: {}", order.getId());
            }

            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            User user = order.getUser();
            if (user != null) {
                notificationService.createPaymentNotification(user, payment, order);
            } else {
                log.warn("Order {} has no associated user, skipping notification.", order.getId());
            }

            log.info("Order {} updated to PAID status", order.getId());
        } catch (Exception e) {
            log.error("Error updating order to paid: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void cancelPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        if (!OrderStatus.PAID.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }

        updateProductQuantity(order, true);

        Long userId = order.getUser().getId();
        order.getOrderLines().forEach(orderLine -> {
            Map<String, Object> event = Map.of(
                    "userId", userId,
                    "productId", orderLine.getProduct().getId(),
                    "quantity", orderLine.getQuantity(),
                    "toppingIds", orderLine.getSelectedToppings().stream()
                            .map(Topping::getId)
                            .collect(Collectors.toList()),
                    "action", "ADD"
            );
            try {
                kafkaProducerService.sendMessage("cart-topic", objectMapper.writeValueAsString(event));
            } catch (Exception e) {
                log.error("Error sending Kafka event for product {}: {}", orderLine.getProduct().getId(), e.getMessage(), e);
            }
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        notificationService.cancelPaymentedNotification(order);
    }

    private void updateProductQuantity(Order order, boolean isCancel) {
        order.getOrderLines().forEach(orderLine -> {
            Product product = productRepository.findById(orderLine.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            int newQuantity = isCancel
                    ? product.getAvailableQuantity() + orderLine.getQuantity()
                    : product.getAvailableQuantity() - orderLine.getQuantity();
            if (newQuantity < 0) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            product.setAvailableQuantity(newQuantity);
            productRepository.save(product);
        });
    }
}