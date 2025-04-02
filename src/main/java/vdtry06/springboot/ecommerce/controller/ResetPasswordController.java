package vdtry06.springboot.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.response.RegisterUserResponse;
import vdtry06.springboot.ecommerce.dto.response.UserResponse;
import vdtry06.springboot.ecommerce.service.ResetPasswordService;
import vdtry06.springboot.ecommerce.dto.request.ResetPassword;
import vdtry06.springboot.ecommerce.dto.request.SendEmailRequest;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResetPasswordController {
    ResetPasswordService resetPasswordService;

    @PostMapping("/send-email")
    public ApiResponse<RegisterUserResponse> sendEmail(@RequestBody SendEmailRequest request) {
        return ApiResponse.<RegisterUserResponse>builder()
                .data(resetPasswordService.sendEmail(request))
                .build();
    }

    @PostMapping("/verify-email-reset-password")
    public ApiResponse<UserResponse> verifyCodeAndResetPassword(@RequestBody @Valid ResetPassword request) {
        return ApiResponse.<UserResponse>builder()
                .data(resetPasswordService.verifyCodeAndResetPassword(request))
                .build();
    }

}
