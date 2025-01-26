package vdtry06.springboot.ecommerce.dto.request.address;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {

    String street;
    String houseNumber;
    String zipCode;

}
