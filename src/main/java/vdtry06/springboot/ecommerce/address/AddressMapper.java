package vdtry06.springboot.ecommerce.address;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.address.dto.AddressRequest;
import vdtry06.springboot.ecommerce.address.dto.AddressResponse;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toAddress(AddressRequest address);

    @Mapping(target = "username", source = "user.username")
    AddressResponse toAddressResponse(Address address);

}


