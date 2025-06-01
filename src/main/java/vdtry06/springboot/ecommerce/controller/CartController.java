package vdtry06.springboot.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.CartItem;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.service.CartService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class CartController {
    CartService cartService;

    @PostMapping("/{userId}/add")
    public ApiResponse<Void> addToCart(@PathVariable Long userId, @Valid @RequestBody OrderLineRequest request) {
        try {
            cartService.addToCart(userId, request.getProductId(), request.getQuantity(), request.getToppingIds());
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Product added to cart successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error adding to cart for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error adding to cart for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{userId}/update")
    public ApiResponse<Void> updateCartItem(@PathVariable Long userId, @Valid @RequestBody OrderLineRequest request) {
        try {
            cartService.updateCartItem(userId, request.getProductId(), request.getQuantity(), request.getToppingIds());
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Cart item updated successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error updating cart item for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error updating cart item for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/{userId}/remove/{productId}")
    public ApiResponse<Void> removeCartItem(@PathVariable Long userId, @PathVariable Long productId) {
        try {
            cartService.removeCartItem(userId, productId);
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Product removed from cart successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error removing cart item for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error removing cart item for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/{userId}")
    public ApiResponse<Map<Long, CartItem>> getCart(@PathVariable Long userId) {
        try {
            Map<Long, CartItem> cart = cartService.getCart(userId);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(1000)
                    .message("Cart retrieved successfully")
                    .data(cart)
                    .build();
        } catch (AppException e) {
            log.error("Error retrieving cart for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving cart for user {}: {}", userId, e.getMessage(), e);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/toggle-select")
    public ApiResponse<Void> toggleSelectItem(@Valid @RequestBody ToggleSelectRequest request) {
        try {
            cartService.toggleSelectItem(request.userId(), request.productId(), request.selected());
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Item selection updated successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error toggling item selection for user {}: {}", request.userId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error toggling item selection for user {}: {}", request.userId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/guest/add")
    public ApiResponse<Void> addToGuestCart(@Valid @RequestBody GuestCartRequest request) {
        try {
            cartService.addToGuestCart(request.sessionId(), request.productId(), request.quantity(), request.toppingIds());
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Product added to guest cart successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error adding to guest cart for session {}: {}", request.sessionId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error adding to guest cart for session {}: {}", request.sessionId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/guest/{sessionId}")
    public ApiResponse<Map<Long, CartItem>> getGuestCart(@PathVariable String sessionId) {
        try {
            Map<Long, CartItem> cart = cartService.getGuestCart(sessionId);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(1000)
                    .message("Guest cart retrieved successfully")
                    .data(cart)
                    .build();
        } catch (AppException e) {
            log.error("Error retrieving guest cart for session {}: {}", sessionId, e.getMessage(), e);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error retrieving guest cart for session {}: {}", sessionId, e.getMessage(), e);
            return ApiResponse.<Map<Long, CartItem>>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/merge-guest-cart")
    public ApiResponse<Void> mergeGuestCart(@Valid @RequestBody MergeGuestCartRequest request) {
        try {
            cartService.mergeGuestCartToUserCart(request.sessionId(), request.userId());
            return ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Guest cart merged successfully")
                    .build();
        } catch (AppException e) {
            log.error("Error merging guest cart for session {} to user {}: {}", request.sessionId(), request.userId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error merging guest cart for session {} to user {}: {}", request.sessionId(), request.userId(), e.getMessage(), e);
            return ApiResponse.<Void>builder()
                    .code(500)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}

record ToggleSelectRequest(Long userId, Long productId, Boolean selected) {}
record GuestCartRequest(String sessionId, Long productId, Integer quantity, Set<Long> toppingIds) {}
record MergeGuestCartRequest(String sessionId, Long userId) {}