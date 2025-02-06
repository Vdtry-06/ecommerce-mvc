package vdtry06.springboot.ecommerce.core.exception;

import java.util.Map;
import java.util.Objects;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.View;

import lombok.extern.slf4j.Slf4j;
import vdtry06.springboot.ecommerce.core.ApiResponse;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";
    private final View error;

    public GlobalExceptionHandler(View error) {
        this.error = error;
    }

    // lỗi ngoại lệ không tìm thấy
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        //        log.error("Exception: ", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // lỗi ngoại lệ bắt được
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    // lỗi user tương tác
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        // lỗi sai key message
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        // xử lý thông báo lỗi khi thay đổi min
        Map attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation =
                    exception.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString());
        } catch (IllegalArgumentException e) {

        }

        // nhập email lỗi hoặc để trống email
        if (errorCode == ErrorCode.EMAIL_INVALID || errorCode == ErrorCode.EMAIL_NOT_EMPTY) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(errorCode.getCode());
            apiResponse.setMessage(errorCode.getMessage());

            return ResponseEntity.badRequest().body(apiResponse);
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(
                                Objects.nonNull(attributes)
                                        ? mapAttribute(errorCode.getMessage(), attributes)
                                        : errorCode.getMessage())
                        .build());
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = attributes.get(MIN_ATTRIBUTE).toString();
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
