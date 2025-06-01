package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.entity.OrderLine;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.mapper.OrderLineMapper;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.OrderLineRepository;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.repository.ToppingRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    ToppingRepository toppingRepository;
    CartUtilityService cartUtilityService;

    @Transactional
    public OrderLineResponse addOrderLine(Long orderId, OrderLineRequest request) {
        Order order = validateOrder(orderId);
        Product product = validateProduct(request.getProductId(), request.getQuantity());
        Set<Topping> toppings = cartUtilityService.validateToppings(request.getToppingIds(), product);
        OrderLine orderLine = orderLineRepository.findByOrderIdAndProductId(orderId, request.getProductId())
                .map(existing -> updateExistingOrderLine(
                        existing,
                        request,
                        product,
                        toppings
                ))
                .orElseGet(() -> createNewOrderLine(
                        order,
                        product,
                        request,
                        toppings
                ));
        orderLineRepository.save(orderLine);
        updateOrderTotalPrice(order);

        log.info("Added/Updated order line for product: {}", product.getId());
        return orderLineMapper.toOrderLineResponse(orderLine);
    }

    @Transactional
    public OrderLineResponse updateOrderLine(Long orderId, Long orderLineId, OrderLineRequest request) {
        OrderLine orderLine = validateOrderLine(orderId, orderLineId);
        Product product = orderLine.getProduct();
        Integer quantity = orderLine.getQuantity();
        Set<Topping> toppings = orderLine.getSelectedToppings();

        if (quantity == null) {
            log.error("Order line {} has null quantity", orderLineId);
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        if (request.getProductId() != null) {
            product = validateProduct(request.getProductId(), request.getQuantity() != null ? request.getQuantity() : quantity);
            orderLine.setProduct(product);
            log.debug("Updated product to ID: {}", product.getId());
        }

        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                log.error("Invalid quantity provided: {}", request.getQuantity());
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }
            if (product.getAvailableQuantity() < request.getQuantity()) {
                log.error("Insufficient stock for product ID: {}, requested: {}, available: {}",
                        product.getId(), request.getQuantity(), product.getAvailableQuantity());
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            quantity = request.getQuantity();
            orderLine.setQuantity(quantity);
            log.debug("Updated quantity to: {}", quantity);
        }

        if (request.getToppingIds() != null) {
            try {
                if (request.getToppingIds().isEmpty()) {
                    orderLine.getSelectedToppings().clear(); // Clear existing toppings
                    toppings = Set.of();
                    log.info("Cleared all toppings for order line: {}", orderLineId);
                } else {
                    toppings = cartUtilityService.validateToppings(request.getToppingIds(), product);
                    orderLine.setSelectedToppings(toppings);
                    log.info("Updated toppings for order line: {} to: {}", orderLineId, toppings);
                }
            } catch (Exception e) {
                log.error("Failed to update toppings for order line: {}", orderLineId, e);
                throw new AppException(ErrorCode.TOPPING_UPDATE_FAILED);
            }
        }
        try {
            orderLine.setPrice(cartUtilityService.calculatePrice(product, quantity, toppings));
            log.debug("Recalculated price: {}", orderLine.getPrice());
        } catch (Exception e) {
            log.error("Failed to calculate price for order line: {}", orderLineId, e);
            throw new AppException(ErrorCode.PRICE_CALCULATION_FAILED);
        }

        try {
            orderLineRepository.save(orderLine);
            log.debug("Saved order line: {}", orderLineId);
        } catch (Exception e) {
            log.error("Failed to save order line: {}", orderLineId, e);
            throw new AppException(ErrorCode.ORDER_LINE_SAVE_FAILED);
        }

        updateOrderTotalPrice(orderLine.getOrder());

        log.info("Successfully updated order line: {}", orderLineId);
        return orderLineMapper.toOrderLineResponse(orderLine);
    }

    @Transactional
    public void removeOrderLine(Long orderId, Long orderLineId) {
        Order order = validateOrder(orderId);
        OrderLine orderLine = orderLineRepository.findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_FOUND));
        order.getOrderLines().remove(orderLine);
        orderLineRepository.delete(orderLine);
        if (order.getOrderLines().isEmpty()) {
            orderRepository.delete(order);
        } else {
            updateOrderTotalPrice(order);
        }
    }

    public List<OrderLineResponse> getOrderLines(Long orderId) {
        Order order = validateOrder(orderId);
        return orderLineRepository.findByOrder(order).stream()
                .map(orderLineMapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }

    private OrderLine validateOrderLine(Long orderId, Long orderLineId) {
        return orderLineRepository.findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_FOUND));
    }

    private Order validateOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    private Product validateProduct(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        return product;
    }


    private OrderLine updateExistingOrderLine(OrderLine existing, OrderLineRequest request, Product product, Set<Topping> toppings) {
        existing.setQuantity(existing.getQuantity() + request.getQuantity());
        existing.setSelectedToppings(toppings);
        existing.setPrice(cartUtilityService.calculatePrice(product, existing.getQuantity(), toppings));
        return existing;
    }

    private OrderLine createNewOrderLine(Order order, Product product, OrderLineRequest request, Set<Topping> toppings) {
        return OrderLine.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .price(cartUtilityService.calculatePrice(product, request.getQuantity(), toppings))
                .selectedToppings(toppings)
                .build();
    }


    private void updateOrderTotalPrice(Order order) {
        BigDecimal totalPrice = order.getOrderLines().stream()
                .map(orderLine -> orderLine.getPrice() != null ? orderLine.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
        log.debug("Updated order total price: {}", totalPrice);
    }
}
