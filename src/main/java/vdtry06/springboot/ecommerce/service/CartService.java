package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.constant.OrderStatus;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.CartItem;
import vdtry06.springboot.ecommerce.entity.*;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.OrderLineRepository;
import vdtry06.springboot.ecommerce.repository.OrderRepository;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.repository.ToppingRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    private static final String CART_USER_PREFIX = "cart:user:";
    private static final String CART_GUEST_PREFIX = "cart:guest:";
    private static final long CART_EXPIRATION_SECONDS = 60; // 10 giây để test
    private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

    RedisTemplate<String, Object> redisTemplate;
    OrderRepository orderRepository;
    OrderLineRepository orderLineRepository;
    ProductRepository productRepository;
    ToppingRepository toppingRepository;
    ObjectMapper objectMapper;

    public void addCartItem(Long userId, OrderLineRequest request) {
        String cartKey = "cart:user:" + userId;
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found: {}", request.getProductId());
                    return new IllegalArgumentException("Product not found: " + request.getProductId());
                });

        CartItem cartItem = new CartItem();
        cartItem.setProductId(request.getProductId());
        cartItem.setProductName(product.getName());
        cartItem.setPrice(product.getPrice());
        cartItem.setProductImageUrl(product.getImageUrls().getFirst());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSelected(false);
        cartItem.setToppingIds(new HashSet<>(request.getToppingIds()));

        try {
            String cartItemJson = objectMapper.writeValueAsString(cartItem);
            redisTemplate.opsForHash().put(cartKey, String.valueOf(request.getProductId()), cartItemJson);
            redisTemplate.expire(cartKey, CART_EXPIRATION_SECONDS, TimeUnit.SECONDS);
            log.info("Added product: {} to cart for user: {}", request.getProductId(), userId);
        } catch (Exception e) {
            log.error("Failed to add cart item to Redis for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to add cart item to Redis", e);
        }
    }

    public void addToGuestCart(String sessionId, Long productId, Integer quantity, Set<Long> toppingIds) {
        String cartKey = CART_GUEST_PREFIX + sessionId;
        addToCartInternal(cartKey, productId, quantity, toppingIds);
    }

    private void addToCartInternal(String cartKey, Long productId, Integer quantity, Set<Long> toppingIds) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        Set<Topping> toppings = validateToppings(toppingIds, product);

        CartItem cartItem = CartItem.builder()
                .quantity(quantity)
                .selected(false)
                .toppingIds(toppings.stream().map(Topping::getId).collect(Collectors.toSet()))
                .build();

        redisTemplate.opsForHash().put(cartKey, productId.toString(), cartItem);
        redisTemplate.expire(cartKey, CART_EXPIRATION_SECONDS, TIME_UNIT);
        log.info("Added product {} to cart {} with TTL {} seconds", productId, cartKey, CART_EXPIRATION_SECONDS);
    }

    public void updateCartItem(Long userId, Long productId, Integer quantity, Set<Long> toppingIds) {
        String cartKey = CART_USER_PREFIX + userId;
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(cartKey, productId.toString());
        if (cartItem == null) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (quantity != null && product.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        if (quantity != null) {
            cartItem.setQuantity(quantity);
        }
        if (toppingIds != null) {
            Set<Topping> toppings = validateToppings(toppingIds, product);
            cartItem.setToppingIds(toppings.stream().map(Topping::getId).collect(Collectors.toSet()));
        }

        redisTemplate.opsForHash().put(cartKey, productId.toString(), cartItem);
        redisTemplate.expire(cartKey, CART_EXPIRATION_SECONDS, TIME_UNIT); // Đặt lại TTL
        log.info("Updated cart item {} for user {} with TTL {} seconds", productId, userId, CART_EXPIRATION_SECONDS);
    }

    public void removeCartItem(Long userId, Long productId) {
        String cartKey = CART_USER_PREFIX + userId;
        redisTemplate.opsForHash().delete(cartKey, productId.toString());
        log.info("Removed product {} from cart for user {}", productId, userId);
    }

    public Map<Long, CartItem> getCart(Long userId) {
        String cartKey = CART_USER_PREFIX + userId;
        return getCartInternal(cartKey);
    }

    public Map<Long, CartItem> getGuestCart(String sessionId) {
        String cartKey = CART_GUEST_PREFIX + sessionId;
        return getCartInternal(cartKey);
    }

    private Map<Long, CartItem> getCartInternal(String cartKey) {
        Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);
        return cartEntries.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> Long.valueOf((String) entry.getKey()),
                        entry -> (CartItem) entry.getValue()
                ));
    }

    public void toggleSelectItem(Long userId, Long productId, Boolean selected) {
        String cartKey = CART_USER_PREFIX + userId;
        CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(cartKey, productId.toString());
        if (cartItem == null) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItem.setSelected(selected);
        redisTemplate.opsForHash().put(cartKey, productId.toString(), cartItem);
        redisTemplate.expire(cartKey, CART_EXPIRATION_SECONDS, TIME_UNIT); // Đặt lại TTL
        log.info("Toggled select for product {} in cart for user {}: {} with TTL {} seconds", productId, userId, selected, CART_EXPIRATION_SECONDS);
    }

    public void mergeGuestCartToUserCart(String sessionId, Long userId) {
        String guestCartKey = CART_GUEST_PREFIX + sessionId;
        String userCartKey = CART_USER_PREFIX + userId;
        Map<Object, Object> guestCart = redisTemplate.opsForHash().entries(guestCartKey);
        if (guestCart.isEmpty()) {
            log.info("No guest cart found for session {}", sessionId);
            return;
        }

        for (Map.Entry<Object, Object> entry : guestCart.entrySet()) {
            redisTemplate.opsForHash().put(userCartKey, entry.getKey(), entry.getValue());
        }
        redisTemplate.delete(guestCartKey);
        redisTemplate.expire(userCartKey, CART_EXPIRATION_SECONDS, TIME_UNIT);
        log.info("Merged guest cart {} to user cart {} with TTL {} seconds", sessionId, userId, CART_EXPIRATION_SECONDS);
    }

    @Transactional
    public void syncCartToDatabase(Long userId) {
        String cartKey = CART_USER_PREFIX + userId;
        Map<Long, CartItem> cart = getCart(userId);
        if (cart.isEmpty()) {
            return;
        }

        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING);
        Order order = pendingOrders.isEmpty() ? createNewPendingOrder(userId) : pendingOrders.getFirst();

        orderLineRepository.deleteByOrder(order);
        order.getOrderLines().clear();

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Map.Entry<Long, CartItem> entry : cart.entrySet()) {
            Long productId = entry.getKey();
            CartItem cartItem = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
            Set<Topping> toppings = validateToppings(cartItem.getToppingIds(), product);

            OrderLine orderLine = OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(calculatePrice(product, cartItem.getQuantity(), toppings))
                    .selectedToppings(toppings)
                    .build();

            orderLineRepository.save(orderLine);
            order.getOrderLines().add(orderLine);
            totalPrice = totalPrice.add(orderLine.getPrice());
        }

        order.setTotalPrice(totalPrice);
        orderRepository.save(order);
        log.info("Synced cart to database for user {}", userId);
    }

    private Order createNewPendingOrder(Long userId) {
        User user = User.builder().id(userId).build();
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .orderLines(new ArrayList<>())
                .payments(new ArrayList<>())
                .notifications(new ArrayList<>())
                .build();
        return orderRepository.save(order);
    }

    @Transactional
    public Order checkoutSelectedItems(Long userId, List<Long> selectedProductIds) {
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            throw new AppException(ErrorCode.NO_ITEMS_SELECTED);
        }

        Map<Long, CartItem> cart = getCart(userId);
        Order order = new Order();
        order.setUser(User.builder().id(userId).build());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(BigDecimal.ZERO);
        List<OrderLine> orderLines = new ArrayList<>();

        for (Long productId : selectedProductIds) {
            CartItem cartItem = cart.get(productId);
            if (cartItem != null) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
                OrderLine orderLine = new OrderLine();
                orderLine.setProduct(product);
                orderLine.setQuantity(cartItem.getQuantity());
                orderLine.setPrice(product.getPrice());
                orderLine.setOrder(order);
                orderLines.add(orderLine);
                order.setTotalPrice(order.getTotalPrice().add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))));
            }
        }

        if (orderLines.isEmpty()) {
            throw new AppException(ErrorCode.NO_ITEMS_SELECTED);
        }

        order.setOrderLines(orderLines);
        Order savedOrder = orderRepository.save(order);
        orderLineRepository.saveAll(orderLines);

        log.info("Created order {} for user {} with selected products {}", savedOrder.getId(), userId, selectedProductIds);
        return savedOrder;
    }

    protected Set<Topping> validateToppings(Set<Long> toppingIds, Product product) {
        if (toppingIds == null || toppingIds.isEmpty()) {
            return Set.of();
        }
        Set<Topping> toppings = toppingRepository.findAllById(toppingIds).stream()
                .filter(topping -> product.getToppings().contains(topping))
                .collect(Collectors.toSet());
        if (toppings.size() != toppingIds.size()) {
            throw new AppException(ErrorCode.INVALID_TOPPING);
        }
        return toppings;
    }

    protected BigDecimal calculatePrice(Product product, Integer quantity, Set<Topping> toppings) {
        if (product.getPrice() == null || quantity == null) {
            throw new AppException(ErrorCode.PRICE_CALCULATION_FAILED);
        }
        BigDecimal basePrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal toppingsPrice = toppings.stream()
                .map(topping -> topping.getPrice() != null ? topping.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));
        return basePrice.add(toppingsPrice);
    }
}