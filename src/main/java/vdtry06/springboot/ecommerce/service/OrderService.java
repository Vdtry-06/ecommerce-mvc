package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.dto.request.order.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.request.order.OrderRequest;
import vdtry06.springboot.ecommerce.dto.request.payment.PaymentRequest;
import vdtry06.springboot.ecommerce.dto.request.product.ProductPurchaseRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderConfirmation;
import vdtry06.springboot.ecommerce.dto.response.order.OrderResponse;
import vdtry06.springboot.ecommerce.dto.response.product.ProductPurchaseResponse;
import vdtry06.springboot.ecommerce.dto.response.user.UserResponse;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.OrderMapper;
import vdtry06.springboot.ecommerce.mapper.PaymentMapper;
import vdtry06.springboot.ecommerce.mapper.UserMapper;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;
import vdtry06.springboot.ecommerce.service.kafka.OrderProducer;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    OrderMapper orderMapper;
    OrderLineService orderLineService;
    OrderProducer orderProducer;
    UserRepository userRepository;
    ProductService productService;
    PaymentService paymentService;
    PaymentMapper paymentMapper;
    UserMapper userMapper;


    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        var user = userRepository.findUserById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Gọi dịch vụ sản phẩm để xử lý mua sản phẩm
        log.info("Create Order Request: {}", request);
        List<ProductPurchaseResponse> purchaseProducts = productService.purchaseProducts(request.getProducts());

        // Tạo đơn hàng
//        Order order = orderRepository.save(orderMapper.toOrder(request));
        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setTotalAmount(request.getAmount());
        order = orderRepository.save(order);


        log.info("Create Order Response: {}", order);
        // Lưu các dòng sản phẩm vào đơn hàng
        for(ProductPurchaseRequest productPurchaseRequest : request.getProducts()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            productPurchaseRequest.getProductId(),
                            productPurchaseRequest.getQuantity()
                    )
            );
        }

        // Tạo yêu cầu thanh toán
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .orderId(order.getId())
                .user(userMapper.toUserResponse(user))
                .build();
        paymentService.createPayment(paymentRequest);
        log.info("Create Payment Response: {}", paymentRequest);

        // Gửi thông báo xác nhận đơn hàng qua Kafka
        OrderConfirmation orderConfirmation = OrderConfirmation.builder()
                .orderReference(request.getReference())
                .totalAmount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .user(userMapper.toUserResponse(user))
                .products(purchaseProducts)
                .build();
        log.info("Create Order Response: {}", orderConfirmation);
        orderProducer.sendOrderConfirmation(orderConfirmation);

        // Trả về phản hồi đơn hàng
        OrderResponse orderResponse = OrderResponse.builder()
                .orderId(order.getId())
                .reference(order.getReference())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .userId(request.getUserId())
                .build();
        log.info("Create Order Response: {}", orderResponse);

        return orderResponse;
    }

    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse findOrderById(Long id) {
        return orderRepository.findById(id)
                .map(orderMapper::toOrderResponse)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
