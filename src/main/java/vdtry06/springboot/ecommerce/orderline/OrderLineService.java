package vdtry06.springboot.ecommerce.orderline;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineRequest;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineResponse;
import vdtry06.springboot.ecommerce.order.Order;
import vdtry06.springboot.ecommerce.product.Product;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.order.OrderRepository;
import vdtry06.springboot.ecommerce.product.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderLineService {
    OrderLineRepository orderLineRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository;
    OrderLineMapper orderLineMapper;

    @Transactional
    public OrderLineResponse addOrderLine(Long orderId, OrderLineRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if(product.getAvailableQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        Optional<OrderLine> existingOrderLine = orderLineRepository.findByOrderIdAndProductId(orderId, request.getProductId());

        OrderLine orderLine;
        if(existingOrderLine.isPresent()) {
            orderLine = existingOrderLine.get();
            orderLine.setQuantity(orderLine.getQuantity() + request.getQuantity());
            orderLine.setPrice(orderLine.getPrice().add(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))));
        } else {
            BigDecimal price = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            orderLine = OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(price)
                    .build();
            order.getOrderLines().add(orderLine);
        }

        orderLineRepository.save(orderLine);
        log.info("Add order line: {}", orderLine.getProduct().getId());

        order.setTotalPrice(order.getOrderLines().stream()
                .map(OrderLine::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        orderRepository.save(order);

        return orderLineMapper.toOrderLineResponse(orderLine);
    }

    @Transactional
    public OrderLineResponse updateOrderLine(Long orderId, Long orderLineId, OrderLineRequest request) {
        OrderLine orderLine = orderLineRepository.findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_FOUND));

        if(!orderLine.getOrder().getId().equals(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        if(orderLine.getProduct().getAvailableQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        orderLine.setQuantity(request.getQuantity());
        orderLine.setPrice(orderLine.getProduct().getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        orderLineRepository.save(orderLine);

        return orderLineMapper.toOrderLineResponse(orderLine);
    }

    @Transactional
    public void removeOrderLine(Long orderLineId) {
        OrderLine orderLine = orderLineRepository.findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_FOUND));

        orderLineRepository.delete(orderLine);
    }

    public List<OrderLineResponse> getOrderLines(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderLine> orderLines = orderLineRepository.findByOrder(order);

        return orderLines.stream()
                .map(orderLineMapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }
}
