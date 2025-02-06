package vdtry06.springboot.ecommerce.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.role.dto.RoleResponse;

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
