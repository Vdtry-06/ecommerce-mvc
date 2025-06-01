package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.entity.CartItem;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.repository.CartItemRepository;
import vdtry06.springboot.ecommerce.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartSyncService { // kafka đồng bộ giỏ hàng sang mysql
    CartItemRepository cartItemRepository;
    ObjectMapper objectMapper;
    KafkaProducerService kafkaProducerService;
    OrderRepository orderRepository;
    RedisTemplate<String, Object> redisTemplate;

    private static final String CART_KEY_PREFIX = "cart:user:";
    private static final long CART_TTL_DAYS = 60;

    @KafkaListener(topics = "cart-topic")
    public void syncCart(String message, Acknowledgment acknowledgment) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            String action = (String) event.get("action");
            Long userId = Long.valueOf(event.get("userId").toString());

            switch (action) {
                case "ADD":
                case "UPDATE":
                    Long productId = Long.valueOf(event.get("productId").toString());
                    Integer quantity = (Integer) event.get("quantity");
                    Set<Long> toppingIds = objectMapper.convertValue(event.get("toppingIds"), objectMapper.getTypeFactory()
                            .constructCollectionType(Set.class, Long.class));
                    CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                            .orElse(CartItem.builder()
                                    .userId(userId)
                                    .productId(productId)
                                    .quantity(quantity)
                                    .build());
                    cartItem.setQuantity(quantity);
                    cartItem.setToppingIds(objectMapper.writeValueAsString(toppingIds));
                    cartItem.setUpdatedAt(LocalDateTime.now());
                    if (cartItem.getCreatedAt() == null) {
                        cartItem.setCreatedAt(LocalDateTime.now());
                    }
                    cartItemRepository.save(cartItem);

                    String redisKey = CART_KEY_PREFIX + userId;
                    redisTemplate.opsForHash().put(redisKey, productId.toString(), objectMapper.writeValueAsString(
                            OrderLineRequest.builder()
                                    .productId(productId)
                                    .quantity(quantity)
                                    .toppingIds(toppingIds)
                                    .build()));
                    redisTemplate.expire(redisKey, CART_TTL_DAYS, TimeUnit.SECONDS);
                    log.info("Synced cart item for user: {}, product: {}", userId, productId);
                    break;
                case "REMOVE":
                    productId = Long.valueOf(event.get("productId").toString());
                    cartItemRepository.findByUserIdAndProductId(userId, productId)
                            .ifPresent(cartItemRepository::delete);
                    redisTemplate.opsForHash().delete(CART_KEY_PREFIX + userId, productId.toString());
                    log.info("Removed cart item for user: {}, product: {}", userId, productId);
                    break;
                case "CLEAR_ORDER_ITEMS":
                    Long orderId = Long.valueOf(event.get("orderId").toString());
                    Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
                    order.getOrderLines().forEach(line -> {
                        cartItemRepository.findByUserIdAndProductId(userId, line.getProduct().getId())
                                .ifPresent(cartItemRepository::delete);
                        redisTemplate.opsForHash().delete(CART_KEY_PREFIX + userId, line.getProduct().getId().toString());
                    });
                    log.info("Cleared cart items for order: {} for user: {}", orderId, userId);
                    break;
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error syncing cart: {}", e.getMessage());
            kafkaProducerService.sendMessage("cart-dlq-topic", message);
        }
    }
}
