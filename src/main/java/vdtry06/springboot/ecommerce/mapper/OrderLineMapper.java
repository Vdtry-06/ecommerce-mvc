package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.dto.request.order.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderLineResponse;
import vdtry06.springboot.ecommerce.entity.OrderLine;

@Mapper(componentModel = "spring")
public interface OrderLineMapper {
    OrderLine toOrderLine(OrderLineRequest request);

    OrderLineResponse toOrderLineResponse(OrderLine orderLine);
}
