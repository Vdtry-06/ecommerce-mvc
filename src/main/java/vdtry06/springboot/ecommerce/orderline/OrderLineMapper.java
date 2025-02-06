package vdtry06.springboot.ecommerce.orderline;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineRequest;
import vdtry06.springboot.ecommerce.orderline.dto.OrderLineResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderLineMapper {
    OrderLine toOrderLine(OrderLineRequest request);

    default OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        if (orderLine == null) {
            return null;
        }

        return OrderLineResponse.builder()
                .id(orderLine.getId())
                .productId(orderLine.getProduct() != null ? orderLine.getProduct().getId() : null) // Ensure the productId is fetched
                .quantity(orderLine.getQuantity())
                .price(orderLine.getPrice())
                .build();
    }

    List<OrderLine> toOrderLines(List<OrderLineResponse> orderLineResponses);

    OrderLine toOrderLine(OrderLineResponse orderLineResponse);
}
