package vdtry06.springboot.ecommerce.controller;

import java.text.ParseException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.user.LoginUserRequest;
import vdtry06.springboot.ecommerce.dto.request.user.LogoutRequest;
import vdtry06.springboot.ecommerce.dto.request.user.RegisterUserRequest;
import vdtry06.springboot.ecommerce.dto.request.verify.IntrospectRequest;
import vdtry06.springboot.ecommerce.dto.request.verify.RefreshRequest;
import vdtry06.springboot.ecommerce.dto.request.verify.VerifyUserRequest;
import vdtry06.springboot.ecommerce.dto.response.verify.AuthenticationResponse;
import vdtry06.springboot.ecommerce.dto.response.verify.IntrospectResponse;
import vdtry06.springboot.ecommerce.dto.response.user.RegisterUserResponse;
import vdtry06.springboot.ecommerce.service.AuthenticationService;
import vdtry06.springboot.ecommerce.service.email.AuthenticationEmailService;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    AuthenticationEmailService authenticationEmailService;

    @PostMapping("/signup")
    public ApiResponse<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        return ApiResponse.<RegisterUserResponse>builder()
                .data(authenticationEmailService.signup(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody LoginUserRequest request) throws ParseException {
        var result = authenticationService.login(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/verify")
    public ApiResponse<String> verifyUser(@RequestBody VerifyUserRequest request) {
        authenticationEmailService.verifyUserSignup(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Successfully verified user")
                .data(null)
                .build();
    }

    @PostMapping("/resend")
    public ApiResponse<RegisterUserResponse> resendVerificationCode(@RequestParam String email) {
        return ApiResponse.<RegisterUserResponse>builder()
                .data(authenticationEmailService.resendVerificationCode(email))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        log.info("Refresh request: {}", request);
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }
}