package vdtry06.springboot.ecommerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    String username;
    Long id;
    String country;
    String city;
    String district;
    String ward;
    String street;
    String houseNumber;
}
