package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserResponse {
    Long id;
    String username;
    String password;
    String email;
    boolean enabled;
    String verificationCode;
    LocalDateTime verificationExpiration;
    Set<RoleResponse> roles;
}
