package vdtry06.springboot.ecommerce.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.address.dto.AddressResponse;
import vdtry06.springboot.ecommerce.order.dto.OrderResponse;
import vdtry06.springboot.ecommerce.role.dto.RoleResponse;

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
