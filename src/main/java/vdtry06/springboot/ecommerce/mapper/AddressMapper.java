package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vdtry06.springboot.ecommerce.dto.request.AddressRequest;
import vdtry06.springboot.ecommerce.dto.response.AddressResponse;
import vdtry06.springboot.ecommerce.entity.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    Address toAddress(AddressRequest address);

    @Mapping(target = "username", source = "user.username")
    AddressResponse toAddressResponse(Address address);

    void updateAddress(@MappingTarget Address address, AddressRequest request);
}


