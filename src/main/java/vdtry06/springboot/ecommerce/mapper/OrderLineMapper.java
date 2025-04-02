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

    @Mapping(target = "productId", source = "product")
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
