package vdtry06.springboot.ecommerce.controller;

import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.service.AuthenticationEmailService;
import vdtry06.springboot.ecommerce.service.AuthenticationService;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.constant.OAuthProvider;
import vdtry06.springboot.ecommerce.constant.PredefinedRole;
import vdtry06.springboot.ecommerce.entity.Role;
import vdtry06.springboot.ecommerce.repository.RoleRepository;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.repository.UserRepository;
import vdtry06.springboot.ecommerce.dto.request.LoginUserRequest;
import vdtry06.springboot.ecommerce.dto.request.LogoutRequest;
import vdtry06.springboot.ecommerce.dto.request.IntrospectRequest;
import vdtry06.springboot.ecommerce.dto.request.RefreshRequest;
import vdtry06.springboot.ecommerce.dto.request.VerifyUserRequest;
import vdtry06.springboot.ecommerce.dto.response.AuthenticationResponse;
import vdtry06.springboot.ecommerce.dto.response.IntrospectResponse;
import vdtry06.springboot.ecommerce.dto.request.RegisterUserRequest;
import vdtry06.springboot.ecommerce.dto.response.RegisterUserResponse;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import com.nimbusds.jose.JOSEException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    AuthenticationEmailService authenticationEmailService;
    RoleRepository roleRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ApiResponse<RegisterUserResponse> register(@RequestBody @Valid RegisterUserRequest request) {
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
                .message("Successfully logged out")
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("/oauth2/success")
    public ApiResponse<AuthenticationResponse> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User) throws ParseException {
        boolean isGithub = oauth2User.getAttribute("login") != null;
        String provider = isGithub ? "github" : "google";

        String email = oauth2User.getAttribute("email");
        if (email == null && isGithub) {
            email = oauth2User.getAttribute("login") + "@github.user";
        }

        String username = isGithub ? oauth2User.getAttribute("login") : Objects.requireNonNull(email).split("@")[0];

        String finalEmail = email;
        if (userRepository.existsByUsername(username) &&
                userRepository.findByUsername(username)
                        .map(u -> !u.getEmail().equals(finalEmail))
                        .orElse(false)) {
            username = username + "-" + provider;
        }

        String finalUsername = username;
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(finalEmail);
                    newUser.setUsername(finalUsername);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setProvider(isGithub ? OAuthProvider.GITHUB : OAuthProvider.GOOGLE);
                    newUser.setEnabled(true);

                    Role userRole = roleRepository.findById(PredefinedRole.USER_ROLE)
                            .orElseThrow(() -> new RuntimeException("Default role not found"));
                    newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

                    return userRepository.save(newUser);
                });

        if (email.equals("tunhoipro0306@gmail.com")) {
            Role adminRole = roleRepository.findById(PredefinedRole.ADMIN_ROLE)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            boolean hasAdminRole = user.getRoles().stream()
                    .anyMatch(role -> role.getName().equals(PredefinedRole.ADMIN_ROLE));

            if (!hasAdminRole) {
                user.getRoles().add(adminRole);
                user = userRepository.save(user);
            }
        }

        String token = authenticationService.generateToken(user);
        long expiryTime = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime().getTime() - System.currentTimeMillis();

        String roleName = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(PredefinedRole.ADMIN_ROLE))
                ? "ADMIN" : "USER";

        return ApiResponse.<AuthenticationResponse>builder()
                .data(AuthenticationResponse.builder()
                        .token(token)
                        .nameRole(roleName)
                        .expiryTime(expiryTime)
                        .build())
                .build();
    }
}