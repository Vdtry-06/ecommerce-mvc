package vdtry06.springboot.ecommerce.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.validator.EmailConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterUserRequest {

    @EmailConstraint(message = "EMAIL_INVALID")
    @NotEmpty(message = "EMAIL_NOT_EMPTY")
    String email;

    @Size(min = 5, message = "USERNAME_INVALID")
    String username;

    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

//    String firstName;
//    String lastName;
//
//    @BirthConstraint(min = 16, message = "INVALID_BIRTHDAY")
//    LocalDate dateOfBirth;
}
