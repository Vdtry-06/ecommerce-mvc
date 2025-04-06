package vdtry06.springboot.ecommerce.service;

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
import vdtry06.springboot.ecommerce.entity.Payment;
import vdtry06.springboot.ecommerce.mapper.PaymentMapper;
import vdtry06.springboot.ecommerce.dto.response.PaymentResponse;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.PaymentRepository;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.OrderLine;
import vdtry06.springboot.ecommerce.dto.request.PaymentRequest;
import vdtry06.springboot.ecommerce.dto.response.VNPayResponse;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.OrderLineRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.entity.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    PaymentRepository paymentRepository;
    OrderRepository orderRepository;
    OrderLineRepository orderLineRepository;
    PaymentMapper paymentMapper;
    ProductRepository productRepository;
    NotificationService notificationService;
    VNPAYConfig vnpayConfig;

    @Transactional
    public VNPayResponse createVNPayPaymentForSelectedItems(HttpServletRequest request, List<OrderLine> selectedOrderLines, Long userId) {
        if (selectedOrderLines == null || selectedOrderLines.isEmpty()) {
            throw new AppException(ErrorCode.NO_ITEMS_SELECTED);
        }

        BigDecimal totalPrice = selectedOrderLines.stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING);
        Order order;

        if (!pendingOrders.isEmpty()) {
            order = pendingOrders.get(0);
            log.info("Using existing pending order: {}", order.getId());

            if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
                List<OrderLine> existingLines = new ArrayList<>(order.getOrderLines());
                for (OrderLine line : existingLines) {
                    order.getOrderLines().remove(line);
                    orderLineRepository.delete(line);
                }
            }
        } else {
            order = new Order();
            order.setUser(User.builder().id(userId).build());
            order.setOrderLines(new ArrayList<>());
            order.setStatus(OrderStatus.PENDING);
            log.info("Creating new order for user: {}", userId);
        }

        order.setTotalPrice(totalPrice);

        orderRepository.save(order);

        for (OrderLine orderLine : selectedOrderLines) {
            Product product = productRepository.findById(orderLine.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            OrderLine newLine = OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(orderLine.getQuantity())
                    .price(orderLine.getPrice())
                    .build();

            orderLineRepository.save(newLine);
            order.getOrderLines().add(newLine);
        }

        orderRepository.save(order);
        log.info("Order prepared for payment: {}", order.getId());

        long amount = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();
        String bankCode = request.getParameter("bankCode");

        Map<String, String> vnpParamsMap = vnpayConfig.getVNPAYConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(order.getId()));

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

    @Transactional
    public void updateOrderToPaid(Order order) {
        if (OrderStatus.PAID.equals(order.getStatus())) {
            log.info("Order {} is already paid, no update needed", order.getId());
            return;
        }

        try {
            updateProductQuantity(order, false);
            Payment payment = Payment.builder()
                    .reference("VNPAY_" + order.getId())
                    .amount(order.getTotalPrice())
                    .paymentMethod(PaymentMethod.VNPAY)
                    .build();

            paymentRepository.save(payment);
            order.setStatus(OrderStatus.PAID);
            order.setPayment(payment);
            orderRepository.save(order);
            User user = order.getUser();
            if (user != null) {
                notificationService.createPaymentNotification(user, payment);
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

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        notificationService.cancelPaymentedNotification(order);
    }

    private void updateProductQuantity(Order order, boolean isRestoring) {
        List<OrderLine> orderLines = order.getOrderLines();
        if (orderLines == null || orderLines.isEmpty()) {
            log.warn("No order lines found for order {}", order.getId());
            return;
        }

        for (OrderLine orderLine : orderLines) {
            Product product = productRepository.findById(orderLine.getProduct().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

            if (isRestoring) {
                product.setAvailableQuantity(product.getAvailableQuantity() + orderLine.getQuantity());
                log.info("Restored {} units to product {}", orderLine.getQuantity(), product.getId());
            } else {
                int newQuantity = product.getAvailableQuantity() - orderLine.getQuantity();
                if (newQuantity < 0) {
                    throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
                }
                product.setAvailableQuantity(newQuantity);
                log.info("Reduced {} units from product {}", orderLine.getQuantity(), product.getId());
            }
            productRepository.save(product);
        }
    }
}

