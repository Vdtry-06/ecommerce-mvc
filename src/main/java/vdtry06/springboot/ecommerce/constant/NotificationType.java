package vdtry06.springboot.ecommerce.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum NotificationType {
    PAYMENT_CONFIRMATION("payment-confirmation.html", "Payment successfully processed"),
    PAYMENT_CANCELLATION("payment-cancellation.html", "Payment was cancelled"),
    DELIVERY_CONFIRMATION("delivery-confirmation.html", "Your order has been delivered"),
    ORDER_SHIPPED("order-shipped.html", "Your order is on the way"),
    ORDER_RECEIVED("order-received.html", "You have received your order"),
    EMAIL_VERIFICATION("confirm-email.html", "Account verification"),
    CART_EXPIRATION("cart-expiration.html", "Your Cart Has Expired"),
    ;
    String template;
    String subject;
}
