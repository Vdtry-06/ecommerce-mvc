package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;
import vdtry06.springboot.ecommerce.dto.response.OrderResponse;
import vdtry06.springboot.ecommerce.service.OrderService;
import vdtry06.springboot.ecommerce.service.RedisCartService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    RedisCartService redisCartService;
    OrderService orderService;

    @PostMapping("/{userId}/add")
    public ApiResponse<OrderLineResponse> addToCart(@PathVariable Long userId, @RequestBody OrderLineRequest request) {
        log.info("Adding product to cart for user: {}", userId);
        OrderLineResponse response = redisCartService.addOrUpdateCartLine(userId, request);
        return ApiResponse.<OrderLineResponse>builder()
                .message("Product added to cart successfully")
                .data(response)
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<List<OrderLineResponse>> getCart(@PathVariable Long userId) {
        log.info("Fetching cart for user: {}", userId);
        List<OrderLineResponse> cart = redisCartService.getCart(userId);
        return ApiResponse.<List<OrderLineResponse>>builder()
                .message("Cart fetched successfully")
                .data(cart)
                .build();
    }

    @PutMapping("/{userId}/update/{productId}")
    public ApiResponse<OrderLineResponse> updateCartLine(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody OrderLineRequest request) {
        log.info("Updating cart line for user: {}, product: {}", userId, productId);
        OrderLineResponse response = redisCartService.updateCartLine(userId, productId, request);
        return ApiResponse.<OrderLineResponse>builder()
                .message("Cart line updated successfully")
                .data(response)
                .build();
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ApiResponse<Void> removeCartLine(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        log.info("Removing product {} from cart for user: {}", productId, userId);
        redisCartService.removeCartLine(userId, productId);
        return ApiResponse.<Void>builder()
                .message("Product removed from cart successfully")
                .data(null)
                .build();
    }

    @PostMapping("/{userId}/confirm")
    public ApiResponse<OrderResponse> confirmCart(@PathVariable Long userId) {
        log.info("Confirming cart for user: {}", userId);
        OrderResponse orderResponse = orderService.confirmOrder(userId);
        return ApiResponse.<OrderResponse>builder()
                .message("Cart confirmed and order created successfully")
                .data(orderResponse)
                .build();
    }

    @DeleteMapping("/{userId}/clear")
    public ApiResponse<Void> clearCart(@PathVariable Long userId) {
        log.info("Clearing cart for user: {}", userId);
        redisCartService.clearCart(userId);
        return ApiResponse.<Void>builder()
                .message("Cart cleared successfully")
                .data(null)
                .build();
    }

    @DeleteMapping("/{userId}/order/{orderId}")
    public ApiResponse<Void> removeCartItemsByOrderId(@PathVariable Long userId, @PathVariable Long orderId) {
        log.info("Removing cart items for user: {}, order: {}", userId, orderId);
        redisCartService.removeCartItemsByOrderId(userId, orderId);
        return ApiResponse.<Void>builder()
                .message("Cart items for order removed successfully")
                .data(null)
                .build();
    }
}