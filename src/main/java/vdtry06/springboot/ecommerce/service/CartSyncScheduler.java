package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vdtry06.springboot.ecommerce.dto.response.CartExpirationNotification;
import vdtry06.springboot.ecommerce.dto.response.CartItem;
import vdtry06.springboot.ecommerce.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartSyncScheduler {
    CartService cartService;
    UserRepository userRepository;
    RedisTemplate<String, Object> redisTemplate;
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000)
    public void syncCarts() {
        userRepository.findAll().forEach(user -> {
            try {
                cartService.syncCartToDatabase(user.getId());
            } catch (Exception e) {
                log.error("Failed to sync cart for user {}: {}", user.getId(), e.getMessage());
            }
        });
        log.info("Completed scheduled cart sync at {}", LocalDateTime.now());
    }

    @Scheduled(fixedDelay = 2000)
    public void checkExpiredCarts() {
        log.debug("Starting cart expiration check at {}", LocalDateTime.now());
        Set<String> cartKeys = redisTemplate.keys("cart:user:*");
        if (cartKeys == null || cartKeys.isEmpty()) {
            log.info("No user carts found for expiration check");
            return;
        }

        log.debug("Found {} cart keys: {}", cartKeys.size(), cartKeys);
        for (String cartKey : cartKeys) {
            Long ttl = redisTemplate.getExpire(cartKey, TimeUnit.SECONDS);
            log.debug("Cart key {} has TTL: {}", cartKey, ttl);
            if (ttl != null && (ttl <= 2 || ttl == -2)) {
                String userIdStr = cartKey.replace("cart:user:", "");
                try {
                    Long userId = Long.valueOf(userIdStr);
                    userRepository.findById(userId).ifPresentOrElse(user -> {
                        if (user.getEmail() == null || user.getEmail().isEmpty()) {
                            log.warn("No email found for user ID: {}, skipping notification", userId);
                            return;
                        }
                        Map<Object, Object> cartItemsMap = redisTemplate.opsForHash().entries(cartKey);
                        List<CartItem> cartItems = new ArrayList<>();
                        log.debug("Cart items map for user {}: {}", userId, cartItemsMap);
                        for (Map.Entry<Object, Object> entry : cartItemsMap.entrySet()) {
                            try {
                                String jsonValue = entry.getValue().toString();
                                CartItem item = objectMapper.readValue(jsonValue, CartItem.class);
                                cartItems.add(item);
                                log.debug("Deserialized cart item for user {}: {}", userId, item);
                            } catch (JsonProcessingException e) {
                                log.error("Failed to deserialize cart item for user {}: {}", userId, e.getMessage());
                            }
                        }
                        if (cartItems.isEmpty()) {
                            log.warn("No valid cart items found for user {}", userId);
                        }
                        CartExpirationNotification notification = CartExpirationNotification.builder()
                                .userId(userId)
                                .username(user.getUsername() != null ? user.getUsername() : "Customer")
                                .email(user.getEmail())
                                .cartItems(cartItems)
                                .build();
                        try {
                            String message = objectMapper.writeValueAsString(notification);
                            kafkaTemplate.send("cart-expiration-topic", userIdStr, message)
                                    .whenComplete((result, ex) -> {
                                        if (ex == null) {
                                            log.info("Sent cart expiration notification for user: {} to topic cart-expiration-topic with email: {} and {} cart items", userId, user.getEmail(), cartItems.size());
                                        } else {
                                            log.error("Failed to send cart expiration notification to Kafka for user {}: {}", userId, ex.getMessage());
                                        }
                                    });
                            redisTemplate.delete(cartKey);
                            log.info("Deleted expired cart key: {}", cartKey);
                        } catch (JsonProcessingException e) {
                            log.error("Failed to serialize cart expiration notification for user {}: {}", userId, e.getMessage());
                        }
                    }, () -> log.warn("User not found for ID: {}", userId));
                } catch (NumberFormatException e) {
                    log.error("Invalid user ID format in cart key {}: {}", cartKey, e.getMessage());
                } catch (Exception e) {
                    log.error("Unexpected error processing cart key {}: {}", cartKey, e.getMessage());
                }
            }
        }
        log.info("Completed scheduled cart expiration check at {}", LocalDateTime.now());
    }
}