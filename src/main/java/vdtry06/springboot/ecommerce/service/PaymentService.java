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
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.entity.User;

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
    PaymentMapper paymentMapper;
    ProductRepository productRepository;
    NotificationService notificationService;
    VNPAYConfig vnpayConfig;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (OrderStatus.PAID.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        PaymentMethod paymentMethod = request.getPaymentMethod();

        Payment payment = Payment.builder()
                .reference(request.getOrderReference())
                .amount(order.getTotalPrice())
                .paymentMethod(paymentMethod)
                .order(order)
                .build();

        paymentRepository.save(payment);
        order.getPayments().add(payment);
        orderRepository.save(order);

        updateProductQuantity(order, false);

        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            order.setStatus(OrderStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.PAID);
            User user = order.getUser();
            if (user != null) {
                notificationService.createPaymentNotification(user, payment);
            } else {
                log.warn("Order {} has no associated user, skipping notification.", order.getId());
            }
        }

        orderRepository.save(order);

        return paymentMapper.toPaymentResponse(payment);
    }

    @Transactional
    public VNPayResponse createVNPayPaymentForSelectedItems(HttpServletRequest request, List<OrderLine> selectedOrderLines, Long userId) {
        if (selectedOrderLines == null || selectedOrderLines.isEmpty()) {
            throw new AppException(ErrorCode.NO_ITEMS_SELECTED);
        }

        Order existingOrder = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    log.warn("No PENDING order found for userId: {}", userId);
                    return new AppException(ErrorCode.ORDER_NOT_FOUND);
                });

        List<OrderLine> updatedOrderLines = existingOrder.getOrderLines().stream()
                .filter(orderLine -> selectedOrderLines.stream()
                        .anyMatch(selected -> selected.getProduct().getId().equals(orderLine.getProduct().getId())))
                .map(orderLine -> {
                    OrderLine selected = selectedOrderLines.stream()
                            .filter(s -> s.getProduct().getId().equals(orderLine.getProduct().getId()))
                            .findFirst()
                            .get();
                    orderLine.setQuantity(selected.getQuantity());
                    orderLine.setPrice(selected.getPrice());
                    return orderLine;
                })
                .collect(Collectors.toList());

        BigDecimal totalPrice = updatedOrderLines.stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        existingOrder.setOrderLines(updatedOrderLines);
        existingOrder.setTotalPrice(totalPrice);
        orderRepository.save(existingOrder);

        long amount = totalPrice.multiply(BigDecimal.valueOf(100)).longValue();
        String bankCode = request.getParameter("bankCode");

        Map<String, String> vnpParamsMap = vnpayConfig.getVNPAYConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(existingOrder.getId()));

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
        if (!OrderStatus.PAID.equals(order.getStatus())) {
            log.info("Updating order {} to PAID status", order.getId());
            order.setStatus(OrderStatus.PAID);
            updateProductQuantity(order, false);

            boolean paymentExists = order.getPayments().stream()
                    .anyMatch(p -> p.getPaymentMethod() == PaymentMethod.VNPAY && p.getReference().equals("VNPAY_" + order.getId()));

            if (!paymentExists) {
                Payment payment = Payment.builder()
                        .reference("VNPAY_" + order.getId())
                        .amount(order.getTotalPrice())
                        .paymentMethod(PaymentMethod.VNPAY)
                        .order(order)
                        .notifications(new ArrayList<>())
                        .build();
                paymentRepository.save(payment);
                order.getPayments().add(payment);
                log.info("Created new payment with ID {} for order {}", payment.getId(), order.getId());
            }

            order.getOrderLines().clear();
            orderRepository.save(order);

            if (order.getUser() != null) {
                Payment latestPayment = order.getPayments().get(order.getPayments().size() - 1);
                notificationService.createPaymentNotification(order.getUser(), latestPayment);
            }
        } else {
            log.warn("Order {} is already in PAID status", order.getId());
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
        for (OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            if (isRestoring) {
                product.setAvailableQuantity(product.getAvailableQuantity() + orderLine.getQuantity());
            } else {
                product.setAvailableQuantity(product.getAvailableQuantity() - orderLine.getQuantity());
            }
            productRepository.save(product);
        }
    }
}