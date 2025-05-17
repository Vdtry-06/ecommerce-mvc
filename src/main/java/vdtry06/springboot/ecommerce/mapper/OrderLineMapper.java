package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.entity.OrderLine;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderLineMapper {
    OrderLine toOrderLine(OrderLineRequest request);

    @Mapping(target = "productId", expression = "java(orderLine.getProduct() != null ? orderLine.getProduct().getId() : null)")
    @Mapping(target = "selectedToppings", expression = "java(orderLine.getSelectedToppings().stream().map(topping -> new vdtry06.springboot.ecommerce.dto.response.ToppingResponse(topping.getId(), topping.getName(), topping.getPrice())).collect(java.util.stream.Collectors.toSet()))")
    OrderLineResponse toOrderLineResponse(OrderLine orderLine);

    List<OrderLine> toOrderLines(List<OrderLineResponse> orderLineResponses);
}
