package vdtry06.springboot.ecommerce.core.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;

import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // xử lý trường hợp người dùng không xác thực được
    @Override
    // khi có lỗi xác thực xảy ra nó sẽ được gọi
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        // xác định lỗi và mã phản hồi
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // tạo phản hồi JSON
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // chuyển đổi sang JSON và gửi phản hồi
        ObjectMapper objectMapper = new ObjectMapper();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
