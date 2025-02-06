package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.OrderStatus;
import vdtry06.springboot.ecommerce.dto.request.order.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderLineResponse;
import vdtry06.springboot.ecommerce.dto.response.order.OrderResponse;
import vdtry06.springboot.ecommerce.entity.*;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.OrderLineMapper;
import vdtry06.springboot.ecommerce.mapper.OrderMapper;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    OrderMapper orderMapper;
    ProductRepository productRepository;
    NotificationService notificationService;
    OrderLineService orderLineService;
    OrderLineMapper orderLineMapper;


    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderLineResponse> orderLineResponses = request.getOrderLines().stream()
                .map(orderLineService::addOrderLine)
                .toList();

        savedOrder.setOrderLines(orderLineMapper.toOrderLines(orderLineResponses));
        savedOrder = orderRepository.save(savedOrder);

        OrderResponse orderResponse = orderMapper.toOrderResponse(savedOrder);
        orderResponse.setOrderLines(orderLineResponses);

        return orderResponse;
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        BigDecimal totalPrice = order.getOrderLines().stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderResponse orderResponse = orderMapper.toOrderResponse(order);
        orderResponse.setTotalPrice(totalPrice);

        return orderResponse;
    }

    @Transactional
    public OrderResponse updateOrderStatusToPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() == OrderStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }
        order.setStatus(OrderStatus.PAID);
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatusToDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() != OrderStatus.SHIPPED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.setStatus(OrderStatus.DELIVERED);
        Order updatedOrder = orderRepository.save(order);

        notificationService.createDeliveredNotification(order);

        return orderMapper.toOrderResponse(updatedOrder);
    }
}
