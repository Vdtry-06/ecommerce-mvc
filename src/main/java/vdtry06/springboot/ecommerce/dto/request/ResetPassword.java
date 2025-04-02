package vdtry06.springboot.ecommerce.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResetPassword {
    String email;
    String verificationCode;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String newPassword;

    String confirmPassword;
}
