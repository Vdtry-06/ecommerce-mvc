package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.OrderStatus;
import vdtry06.springboot.ecommerce.dto.response.CartItem;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.mapper.OrderMapper;
import vdtry06.springboot.ecommerce.dto.request.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderResponse;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    OrderMapper orderMapper;
    OrderLineService orderLineService;
    CartService cartService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .orderLines(new ArrayList<>())
                .payments(new ArrayList<>())
                .notifications(new ArrayList<>())
                .build();

        orderRepository.save(order);
        log.info("Saved order: {}", order.getId());

        request.getOrderLines().forEach(orderLineRequest ->
                orderLineService.addOrderLine(order.getId(), orderLineRequest));

        return orderMapper.toOrderResponse(orderRepository.findById(order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND)));

    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getAllOrdersOfUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return orderRepository.findByUserId(userId).stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_STATE_PENDING);
        }
        orderRepository.deleteById(orderId);
    }

    public void addToCart(Long userId, OrderLineRequest request) {
        cartService.addCartItem(userId, request);
    }

    public void updateCartItem(Long userId, Long productId, OrderLineRequest request) {
        cartService.updateCartItem(userId, productId, request.getQuantity(), request.getToppingIds());
    }

    public void removeCartItem(Long userId, Long productId) {
        cartService.removeCartItem(userId, productId);
    }

    public Map<Long, CartItem> getCart(Long userId) {
        return cartService.getCart(userId);
    }

    public void toggleSelectItem(Long userId, Long productId, Boolean selected) {
        cartService.toggleSelectItem(userId, productId, selected);
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
