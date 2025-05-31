package vdtry06.springboot.ecommerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.entity.CartItem;
import vdtry06.springboot.ecommerce.repository.CartItemRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartSyncService { // kafka đồng bộ giỏ hàng sang mysql
    CartItemRepository cartItemRepository;
    ObjectMapper objectMapper;

    @KafkaListener(topics = "cart-topic")
    public void syncCart(String message, Acknowledgment acknowledgment) {
        try {
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            Long userId = Long.valueOf(event.get("userId").toString());
            Long productId = Long.valueOf(event.get("productId").toString());
            Integer quantity = (Integer) event.get("quantity");
            Set<Long> toppingIds = objectMapper.convertValue(event.get("toppingIds"), objectMapper.getTypeFactory()
                    .constructCollectionType(Set.class, Long.class));
            String action = (String) event.get("action");

            switch (action) {
                case "ADD": case "UPDATE":
                    CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                            .orElse(CartItem.builder()
                                    .userId(userId)
                                    .productId(productId)
                                    .quantity(quantity)
                                    .build());
                    cartItem.setQuantity(quantity);
                    cartItem.setToppingIds(objectMapper.writeValueAsString(toppingIds));
                    cartItem.setUpdatedAt(LocalDateTime.now());
                    cartItemRepository.save(cartItem);
                    log.info("Synced cart item for user: {}, product: {}", userId, productId);
                    break;
                case "REMOVE":
                    cartItemRepository.findByUserIdAndProductId(userId, productId)
                            .ifPresent(cartItemRepository::delete);
                    log.info("Removed cart item for user: {}, product: {}", userId, productId);
                    break;
            }
        } catch (Exception e) {
            log.error("Error syncing cart: {}", e.getMessage());
        }
    }
}
