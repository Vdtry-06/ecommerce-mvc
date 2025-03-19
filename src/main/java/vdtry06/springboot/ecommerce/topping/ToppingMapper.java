package vdtry06.springboot.ecommerce.topping;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.topping.dto.ToppingRequest;
import vdtry06.springboot.ecommerce.topping.dto.ToppingResponse;

@Mapper(componentModel = "spring")
public interface ToppingMapper {
    Topping toTopping(ToppingRequest topping);

    ToppingResponse toToppingResponse(Topping topping);
}
