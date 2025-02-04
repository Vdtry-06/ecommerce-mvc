package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.dto.request.order.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderResponse;
import vdtry06.springboot.ecommerce.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(OrderRequest request);

    OrderResponse toOrderResponse(Order order);
}
