package vdtry06.springboot.ecommerce.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.validator.BirthConstraint;
import vdtry06.springboot.ecommerce.validator.EmailConstraint;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdationRequest {
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;

    String firstName;
    String lastName;

    @BirthConstraint(min = 16, message = "INVALID_BIRTHDAY")
    LocalDate dateOfBirth;
}
