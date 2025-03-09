package vdtry06.springboot.ecommerce.address.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    String country;
    String city;
    String district;
    String ward;
    String street;
    String houseNumber;

}
