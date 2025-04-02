package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoResponse {
    Long id;
    String username;
    String email;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Set<RoleResponse> roles;
    String imageUrl;
    AddressResponse address;
    List<OrderResponse> orders;
}
