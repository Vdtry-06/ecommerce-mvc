package vdtry06.springboot.ecommerce.order;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.core.constant.OrderStatus;
import vdtry06.springboot.ecommerce.order.dto.OrderRequest;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineResponse;
import vdtry06.springboot.ecommerce.order.dto.OrderResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.orderline.OrderLineMapper;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.product.ProductRepository;
import vdtry06.springboot.ecommerce.notification.NotificationService;
import vdtry06.springboot.ecommerce.orderline.OrderLineService;
import vdtry06.springboot.ecommerce.user.UserRepository;
import vdtry06.springboot.ecommerce.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
                .map(orderLineRequest -> orderLineService.addOrderLine(order.getId(), orderLineRequest))
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

    public List<OrderResponse> getAllOrders() {
        OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> deleteOrder(Long orderId) {
        OrderResponse orderResponse = getOrderById(orderId);
        if(orderResponse.getStatus() == OrderStatus.PENDING) {
            orderRepository.deleteById(orderId);
            return getAllOrders();
        } else {
            throw new AppException(ErrorCode.ORDER_STATE_PENDING);
        }
    }

//    @Transactional
//    public OrderResponse updateOrderStatusToPaid(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        if(order.getStatus() == OrderStatus.PAID) {
//            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
//        }
//        order.setStatus(OrderStatus.PAID);
//        Order updatedOrder = orderRepository.save(order);
//
//        return orderMapper.toOrderResponse(updatedOrder);
//    }
//
//    @Transactional
//    public OrderResponse updateOrderStatusToDelivered(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
//
//        if(order.getStatus() != OrderStatus.SHIPPED) {
//            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
//        }
//
//        order.setStatus(OrderStatus.DELIVERED);
//        Order updatedOrder = orderRepository.save(order);
//
//        notificationService.createDeliveredNotification(order);
//
//        return orderMapper.toOrderResponse(updatedOrder);
//    }
}
