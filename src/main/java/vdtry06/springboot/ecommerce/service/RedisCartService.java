package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.request.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;
import vdtry06.springboot.ecommerce.dto.response.ToppingResponse;
import vdtry06.springboot.ecommerce.entity.CartItem;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.CartItemRepository;
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

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final long CART_TTL_DAYS = 7;

    public List<OrderLineResponse> getCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Object cartData = redisTemplate.opsForValue().get(key);
        if (cartData == null) {
            return restoreCartFromDatabase(userId);
        }
        try {
            List<OrderLineRequest> cart = objectMapper.convertValue(cartData, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, OrderLineRequest.class));
            return cart.stream().map(this::toOrderLineResponse).toList();
        } catch (Exception e) {
            log.error("Error reading cart from Redis for key {}: {}", key, e.getMessage(), e);
            throw new AppException(ErrorCode.CART_READ_FAILED);
        }
    }

    public OrderLineResponse addOrUpdateCartLine(Long userId, OrderLineRequest request) {
        String key = CART_KEY_PREFIX + userId;
        List<OrderLineRequest> cart = getCartAsList(userId);

        Product product = validateProduct(request.getProductId(), request.getQuantity());
        Set<Topping> toppings = validateToppings(request.getToppingIds(), product);

        Optional<OrderLineRequest> existing = cart.stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
            existing.get().setToppingIds(request.getToppingIds());
        } else {
            cart.add(request);
        }

        saveCart(key, cart);
        sendKafkaEvent(userId, request, "ADD");
        return toOrderLineResponse(request);
    }

    public OrderLineResponse updateCartLine(Long userId, Long productId, OrderLineRequest request) {
        String key = CART_KEY_PREFIX + userId;
        List<OrderLineRequest> cart = getCartAsList(userId);

        OrderLineRequest line = cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Product product = validateProduct(productId, request.getQuantity());
        Set<Topping> toppings = validateToppings(request.getToppingIds(), product);

        line.setQuantity(request.getQuantity());
        line.setToppingIds(request.getToppingIds());

        saveCart(key, cart);
        sendKafkaEvent(userId, line, "UPDATE");
        return toOrderLineResponse(line);
    }

    public void removeCartLine(Long userId, Long productId) {
        String key = CART_KEY_PREFIX + userId;
        List<OrderLineRequest> cart = getCartAsList(userId);
        Optional<OrderLineRequest> line = cart.stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        cart.removeIf(item -> item.getProductId().equals(productId));
        if (cart.isEmpty()) {
            redisTemplate.delete(key);
        } else {
            saveCart(key, cart);
        }
        if (line.isPresent()) {
            sendKafkaEvent(userId, line.get(), "REMOVE");
        }
    }

    public void clearCart(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        List<OrderLineRequest> cart = getCartAsList(userId);
        redisTemplate.delete(key);
        cart.forEach(line -> sendKafkaEvent(userId, line, "REMOVE"));
        cartItemRepository.deleteByUserId(userId);
    }

    public OrderRequest getCartAsOrderRequest(Long userId) {
        List<OrderLineRequest> cart = getCartAsList(userId);
        return OrderRequest.builder()
                .userId(userId)
                .orderLines(cart)
                .build();
    }

    private List<OrderLineRequest> getCartAsList(Long userId) {
        String key = CART_KEY_PREFIX + userId;
        Object cartData = redisTemplate.opsForValue().get(key);
        if (cartData == null) {
            return new ArrayList<>(restoreCartFromDatabase(userId).stream()
                    .map(this::toOrderLineRequest)
                    .toList());
        }
        try {
            log.debug("Cart data from Redis for key {}: {}", key, cartData);
            List<OrderLineRequest> cart = objectMapper.convertValue(cartData, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, OrderLineRequest.class));
            return cart != null ? new ArrayList<>(cart) : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error converting cart data from Redis for key {}: {}", key, e.getMessage(), e);
            throw new AppException(ErrorCode.CART_READ_FAILED);
        }
    }

    private List<OrderLineResponse> restoreCartFromDatabase(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<OrderLineRequest> cart = cartItems.stream()
                .map(item -> {
                    Set<Long> toppingIds = item.getToppingIds() != null
                            ? objectMapper.convertValue(item.getToppingIds(), objectMapper.getTypeFactory()
                            .constructCollectionType(Set.class, Long.class))
                            : Set.of();
                    return OrderLineRequest.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .toppingIds(toppingIds)
                            .build();
                })
                .toList();
        if (!cart.isEmpty()) {
            String key = CART_KEY_PREFIX + userId;
            saveCart(key, cart);
        }
        return new ArrayList<>(cart.stream().map(this::toOrderLineResponse).toList());
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

    private void saveCart(String key, List<OrderLineRequest> cart) {
        try {
            redisTemplate.opsForValue().set(key, cart, CART_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Cart saved to Redis for key: {}", key);
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
            String message = objectMapper.writeValueAsString(event);
            kafkaProducerService.sendMessage("cart-topic", message);
        } catch (Exception e) {
            log.error("Error sending Kafka event for user {}: {}", userId, e.getMessage(), e);
        }
    }

    private OrderLineResponse toOrderLineResponse(OrderLineRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Set<Topping> toppings = validateToppings(request.getToppingIds(), product);
        BigDecimal price = calculatePrice(product, request.getQuantity(), toppings);
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

    private Product validateProduct(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getAvailableQuantity() < quantity) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        return product;
    }

    private Set<Topping> validateToppings(Set<Long> toppingIds, Product product) {
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

    private BigDecimal calculatePrice(Product product, Integer quantity, Set<Topping> toppings) {
        BigDecimal basePrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal toppingsPrice = toppings.stream()
                .map(topping -> topping.getPrice() != null ? topping.getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(quantity));
        return basePrice.add(toppingsPrice);
    }
}