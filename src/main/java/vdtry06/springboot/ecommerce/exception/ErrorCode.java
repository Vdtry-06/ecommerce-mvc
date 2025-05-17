package vdtry06.springboot.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid key provided", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(1002, "Username already exists", HttpStatus.BAD_REQUEST),
    PASSWORD_EXISTED(1003, "Password already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1004, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1005, "Username must be at least {min} characters long", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1006, "Password must be at least {min} characters long", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1007, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EMPTY(1008, "Email cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_BIRTHDAY(1009, "You must be at least {min} years old", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1010, "User does not exist", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1011, "Authentication required", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1012, "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    ADDRESS_NOT_EXISTED(1013, "Address does not exist", HttpStatus.NOT_FOUND),
    ADDRESS_NOT_BELONG_TO_USER(1014, "Address does not belong to the user", HttpStatus.FORBIDDEN),
    ADDRESS_ALREADY_EXISTS(1015, "Address already exists", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1016, "Incorrect password", HttpStatus.BAD_REQUEST),
    ACCOUNT_VERIFIED(1017, "Account is already verified", HttpStatus.BAD_REQUEST),
    INVALID_CODE(1018, "Invalid verification code", HttpStatus.BAD_REQUEST),
    CODE_EXPIRED(1019, "Verification code has expired", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1020, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_NOT_VERIFIED(1021, "Account is not verified", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1022, "Invalid token", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH(1023, "Passwords do not match", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND(1024, "Email not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_EXISTED(1025, "Category does not exist", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTS(1026, "Category name already exists", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_EXISTS(1027, "Product name already exists", HttpStatus.BAD_REQUEST),
    NEGATIVE_PRICE(1028, "Price must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1029, "Product not found", HttpStatus.NOT_FOUND),
    NEGATIVE_QUANTITY(1030, "Quantity cannot be negative", HttpStatus.BAD_REQUEST),
    PRODUCTS_NOT_FOUND(1031, "One or more products do not exist", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(1032, "Insufficient stock quantity for product", HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1033, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_LINE_NOT_FOUND(1034, "Order line not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_PAID(1035, "Order already paid", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS(1036, "Invalid order status", HttpStatus.BAD_REQUEST),
    ORDER_NOT_PAID(1037, "Order not paid", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CANCELLED(1038, "Order already cancelled", HttpStatus.BAD_REQUEST),
    NULL_KEY_OR_DATA(1039, "Null key or data", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(1040, "Payment failed", HttpStatus.BAD_REQUEST),
    ORDER_STATE_PENDING(1041, "Order cannot be deleted as it is not in PENDING status.", HttpStatus.BAD_REQUEST),
    INVALID_FILE(1042, "Invalid uploaded file", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1043, "Error uploading image to Cloudinary", HttpStatus.BAD_REQUEST),
    DELETE_FAILED(1044, "Error deleting image from Cloudinary", HttpStatus.BAD_REQUEST ),
    TOPPING_NOT_FOUND(1055, "Topping not found", HttpStatus.NOT_FOUND),
    TOPPING_NAME_EXISTS(1056, "Topping name already exists", HttpStatus.BAD_REQUEST),
    TOPPING_NOT_EXISTED(1057, "Topping does not exist", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_FOUND(1058, "Review not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_EXISTED(1059, "Review does not exist", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(1060, "Review already exists", HttpStatus.BAD_REQUEST),
    NO_ITEMS_SELECTED(1061, "No items selected", HttpStatus.BAD_REQUEST ),
    USER_NOT_PURCHASED_PRODUCT(1062, "User is not purchase product", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_REVIEW_UPDATE(1063, "Unauthorized review update", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_REVIEW_DELETE(1064, "Unauthorized review delete", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND(1065, "Role not found", HttpStatus.NOT_FOUND),
    INVALID_TOPPING(1065, "Invalid topping", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(1066, "Order line has null quantity", HttpStatus.BAD_REQUEST),
    TOPPING_UPDATE_FAILED(1067, "Topping update failed by user", HttpStatus.BAD_REQUEST),
    PRICE_CALCULATION_FAILED(1068, "Price calculation failed", HttpStatus.BAD_REQUEST),
    ORDER_LINE_SAVE_FAILED(1069, "Order line save failed", HttpStatus.BAD_REQUEST),

    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
