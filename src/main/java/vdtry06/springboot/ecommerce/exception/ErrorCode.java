package vdtry06.springboot.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized Exception", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(1002, "Username already exists", HttpStatus.BAD_REQUEST),
    PASSWORD_EXISTED(1003, "Password already exists", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1004, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1005, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1006, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1007, "Email is not valid", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EMPTY(1008, "Email cannot be empty", HttpStatus.BAD_REQUEST),
    INVALID_BIRTHDAY(1009, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1010, "User not exists", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1011, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1012, "You do not have permission", HttpStatus.FORBIDDEN),
    ADDRESS_NOT_EXISTED(1013, "Address not exists", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_BELONG_TO_USER(1014, "Address not belong to user", HttpStatus.BAD_REQUEST),
    ADDRESS_ALREADY_EXISTS(1015, "Address already exists", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1016, "Incorrect password", HttpStatus.BAD_REQUEST),
    ACCOUNT_VERIFIED(1017, "Account is already verified", HttpStatus.BAD_REQUEST),
    INVALID_CODE(1018, "Invalid verification code", HttpStatus.BAD_REQUEST),
    CODE_EXPIRED(1019, "Verification code has expired", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(1020, "Email send failed", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
