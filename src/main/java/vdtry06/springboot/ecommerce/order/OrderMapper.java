package vdtry06.springboot.ecommerce.order;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vdtry06.springboot.ecommerce.orderline.OrderLineMapper;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineResponse;
import vdtry06.springboot.ecommerce.orderline.OrderLine;
import vdtry06.springboot.ecommerce.order.dto.OrderRequest;
import vdtry06.springboot.ecommerce.order.dto.OrderResponse;

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
