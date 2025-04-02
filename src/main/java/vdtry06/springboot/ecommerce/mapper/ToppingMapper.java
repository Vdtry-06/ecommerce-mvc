package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.entity.Topping;
import vdtry06.springboot.ecommerce.dto.request.ToppingRequest;
import vdtry06.springboot.ecommerce.dto.response.ToppingResponse;

@Mapper(componentModel = "spring")
public interface ToppingMapper {
    Topping toTopping(ToppingRequest topping);

    ToppingResponse toToppingResponse(Topping topping);
}
