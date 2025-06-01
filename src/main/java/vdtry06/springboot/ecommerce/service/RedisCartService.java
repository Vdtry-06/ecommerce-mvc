package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.request.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;
import vdtry06.springboot.ecommerce.dto.response.ToppingResponse;
import vdtry06.springboot.ecommerce.entity.CartItem;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.CartItemRepository;
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
public class RedisCartService {
    RedisTemplate<String, Object> redisTemplate;
    ProductRepository productRepository;
    ToppingRepository toppingRepository;
    CartItemRepository cartItemRepository;
    ObjectMapper objectMapper;
    KafkaProducerService kafkaProducerService;
    CartUtilityService cartUtilityService;
    OrderRepository orderRepository;

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final long CART_TTL_DAYS = 60;

    public List<OrderLineResponse> getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Map<Object, Object> cartData = redisTemplate.opsForHash().entries(key);
        if (cartData.isEmpty()) {
            return restoreCartFromDatabase(userId);
        }
        try {
            return cartData.values().stream()
                    .map(data -> objectMapper.convertValue(data, OrderLineRequest.class))
                    .map(this::toOrderLineResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error reading cart from Redis for key {}: {}", key, e.getMessage(), e);
            throw new AppException(ErrorCode.CART_READ_FAILED);
        }
    }

    public OrderLineResponse addOrUpdateCartLine(Long userId, OrderLineRequest request) {
        String key = CART_KEY_PREFIX + userId;
        ArrayList<OrderLineRequest> cart = getCartAsList(userId); // Returns empty ArrayList for new user
        Product product = validateProduct(request.getProductId(), request.getQuantity());
        Set<Topping> toppings = cartUtilityService.validateToppings(request.getToppingIds(), product);

        Optional<OrderLineRequest> existing = cart.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
            existing.get().setToppingIds(request.getToppingIds());
        } else {
            cart.add(request); // Add the new request to the empty cart
        }

        saveCart(key, cart); // This will save the new item
        sendKafkaEvent(userId, request, "ADD");
        return toOrderLineResponse(request);
    }

    public OrderLineResponse updateCartLine(Long userId, Long productId, OrderLineRequest request) {
        String key = CART_KEY_PREFIX + userId;
        ArrayList<OrderLineRequest> cart = getCartAsList(userId);

        OrderLineRequest line = cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Product product = validateProduct(productId, request.getQuantity());
        Set<Topping> toppings = cartUtilityService.validateToppings(request.getToppingIds(), product);

        line.setQuantity(request.getQuantity());
        line.setToppingIds(request.getToppingIds());

        saveCart(key, cart);
        sendKafkaEvent(userId, line, "UPDATE");
        return toOrderLineResponse(line);
    }

    public void removeCartLine(Long userId, Long productId) {
        String key = CART_KEY_PREFIX + userId;
        redisTemplate.opsForHash().delete(key, productId.toString());
        sendKafkaEvent(userId, OrderLineRequest.builder().productId(productId).build(), "REMOVE");
    }

    public void clearCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        List<OrderLineRequest> cart = getCartAsList(userId);
        redisTemplate.delete(key);
        cart.forEach(line -> sendKafkaEvent(userId, line, "REMOVE"));
        cartItemRepository.deleteByUserId(userId);
    }

    public ArrayList<OrderLineRequest> getCartAsList(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Map<Object, Object> cartData;
        try {
            cartData = redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Failed to retrieve cart data from Redis for key {}: {}", key, e.getMessage(), e);
            return new ArrayList<>(restoreCartFromDatabase(userId).stream()
                    .map(this::toOrderLineRequest)
                    .collect(Collectors.toCollection(ArrayList::new)));
        }
        if (cartData == null || cartData.isEmpty()) {
            // For new users, return an empty list but allow the calling method to add data
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(cartData.values().stream()
                    .filter(data -> data instanceof String)
                    .map(data -> {
                        try {
                            return objectMapper.convertValue(data, OrderLineRequest.class);
                        } catch (IllegalArgumentException e) {
                            log.error("Failed to convert data {} to OrderLineRequest for key {}: {}", data, key, e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new)));
        } catch (Exception e) {
            log.error("Error converting cart data from Redis for key {}: {}", key, e.getMessage(), e);
            throw new AppException(ErrorCode.CART_READ_FAILED);
        }
    }

    private ArrayList<OrderLineResponse> restoreCartFromDatabase(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            log.info("No cart items found in database for user: {}", userId);
            return new ArrayList<>();
        }
        ArrayList<OrderLineRequest> cart = new ArrayList<>(cartItems.stream()
                .map(item -> {
                    Set<Long> toppingIds = item.getToppingIds() != null
                            ? objectMapper.convertValue(item.getToppingIds(), objectMapper.getTypeFactory()
                            .constructCollectionType(Set.class, Long.class))
                            : new HashSet<>();
                    return OrderLineRequest.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .toppingIds(toppingIds)
                            .build();
                })
                .collect(Collectors.toCollection(ArrayList::new)));
        if (!cart.isEmpty()) {
            String key = CART_KEY_PREFIX + userId;
            saveCart(key, cart);
        }
        return new ArrayList<>(cart.stream()
                .map(this::toOrderLineResponse)
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    private OrderLineRequest toOrderLineRequest(OrderLineResponse response) {
        return OrderLineRequest.builder()
                .productId(response.getProductId())
                .quantity(response.getQuantity())
                .toppingIds(response.getSelectedToppings().stream()
                        .map(topping -> topping.getId())
                        .collect(Collectors.toSet()))
                .build();
    }

    public void saveCart(String key, ArrayList<OrderLineRequest> cart) {
        try {
            if (cart == null || cart.isEmpty()) {
                log.warn("Cart is empty for key {}. Skipping save to Redis.", key);
                redisTemplate.delete(key); // Clear the key if cart is empty
                return;
            }
            Map<String, String> cartMap = cart.stream()
                    .collect(Collectors.toMap(
                            item -> item.getProductId().toString(),
                            item -> {
                                try {
                                    return objectMapper.writeValueAsString(item);
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }));
            redisTemplate.opsForHash().putAll(key, cartMap);
            redisTemplate.expire(key, CART_TTL_DAYS, TimeUnit.SECONDS);
            log.debug("Cart saved to Redis for key: {}. Entries: {}", key, cartMap);
        } catch (Exception e) {
            log.error("Error saving cart to Redis for key {}: {}", key, e.getMessage(), e);
            throw new AppException(ErrorCode.CART_WRITE_FAILED);
        }
    }

    private void sendKafkaEvent(Long userId, OrderLineRequest request, String action) {
        try {
            Map<String, Object> event = Map.of(
                    "userId", userId,
                    "productId", request.getProductId(),
                    "quantity", request.getQuantity(),
                    "toppingIds", request.getToppingIds() != null ? request.getToppingIds() : List.of(),
                    "action", action
            );
            kafkaProducerService.sendMessage("cart-topic", objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Error sending Kafka event for user {}: {}", userId, e.getMessage(), e);
        }
    }

    private OrderLineResponse toOrderLineResponse(OrderLineRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Set<Topping> toppings = cartUtilityService.validateToppings(request.getToppingIds(), product);
        BigDecimal price = cartUtilityService.calculatePrice(product, request.getQuantity(), toppings);
        Set<ToppingResponse> toppingResponses = toppings.stream()
                .map(topping -> ToppingResponse.builder()
                        .id(topping.getId())
                        .name(topping.getName())
                        .price(topping.getPrice())
                        .build())
                .collect(Collectors.toSet());
        return OrderLineResponse.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .selectedToppings(toppingResponses)
                .price(price)
                .build();
    }

    @Transactional
    public void removeCartItemsByOrderId(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.getOrderLines().forEach(line ->
                this.removeCartLine(userId, line.getProduct().getId())); // Use 'this' instead of 'redisCartService'
        order.getOrderLines().forEach(line ->
                cartItemRepository.findByUserIdAndProductId(userId, line.getProduct().getId())
                        .ifPresent(cartItemRepository::delete));
    }

    private Product validateProduct(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        return product;
    }

}