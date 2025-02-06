package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vdtry06.springboot.ecommerce.dto.request.order.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderLineResponse;
import vdtry06.springboot.ecommerce.dto.response.order.OrderResponse;
import vdtry06.springboot.ecommerce.entity.Order;
import vdtry06.springboot.ecommerce.entity.OrderLine;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderLineMapper orderLineMapper = Mappers.getMapper(OrderLineMapper.class);
    Order toOrder(OrderRequest request);

    default OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .orderLines(order.getOrderLines().stream()
                        .map(orderLineMapper::toOrderLineResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    OrderLineResponse toOrderLineResponse(OrderLine orderLine);

    OrderLine toOrderLine(OrderLineResponse orderLineResponse);


}
