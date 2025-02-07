package vdtry06.springboot.ecommerce.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.core.constant.OrderStatus;
import vdtry06.springboot.ecommerce.core.constant.PaymentMethod;
import vdtry06.springboot.ecommerce.payment.dto.PaymentResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.notification.NotificationService;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.payment.dto.PaymentRequest;
import vdtry06.springboot.ecommerce.product.Product;
import vdtry06.springboot.ecommerce.order.OrderRepository;
import vdtry06.springboot.ecommerce.product.ProductRepository;
import vdtry06.springboot.ecommerce.user.User;

import java.util.List;

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

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(OrderStatus.PAID.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

//        if(OrderStatus.CANCELLED.equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
//        }

        PaymentMethod paymentMethod = request.getPaymentMethod();

        Payment payment = Payment.builder()
                .reference(request.getOrderReference())
                .amount(order.getTotalPrice())
                .paymentMethod(paymentMethod)
                .build();

        paymentRepository.save(payment);
        order.setPayment(payment);

        updateProductQuantity(order, false);

        if(paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            order.setStatus(OrderStatus.PENDING);
        } else {
            order.setStatus(OrderStatus.PAID);
            User user = order.getUser();
            if(user != null) {
                notificationService.createPaymentNotification(user, payment);
            } else {
                log.warn("Order {} has no associated user, skipping notification.", order.getId());
            }
        }

        orderRepository.save(order);

        return paymentMapper.toPaymentResponse(payment);
    }



    @Transactional
    public void cancelPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }

        if(!OrderStatus.PAID.equals(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_NOT_PAID);
        }



        updateProductQuantity(order, true);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        notificationService.cancelPaymentedNotification(order);
    }

    private void updateProductQuantity(Order order, boolean isRestoring) {
        List<OrderLine> orderLines = order.getOrderLines();

        for(OrderLine orderLine : orderLines) {
            Product product = orderLine.getProduct();
            if(isRestoring) {
                product.setAvailableQuantity(product.getAvailableQuantity() + orderLine.getQuantity());
            } else {
                product.setAvailableQuantity(product.getAvailableQuantity() - orderLine.getQuantity());
            }
            productRepository.save(product);
        }
    }
}
