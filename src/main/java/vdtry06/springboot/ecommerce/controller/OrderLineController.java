package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.order.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderLineResponse;
import vdtry06.springboot.ecommerce.entity.OrderLine;
import vdtry06.springboot.ecommerce.service.OrderLineService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders/order-lines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderLineController {
    OrderLineService orderLineService;

    @PostMapping
    public ApiResponse<OrderLineResponse> addOrderLine(@RequestBody OrderLineRequest request) {
        return ApiResponse.<OrderLineResponse>builder()
                .data(orderLineService.addOrderLine(request))
                .build();
    }

    @PutMapping("/{orderLineId}")
    public ApiResponse<OrderLineResponse> updateOrderLine(@PathVariable Long orderLineId, @RequestBody OrderLineRequest request) {
        return ApiResponse.<OrderLineResponse>builder()
                .data(orderLineService.updateOrderLine(orderLineId, request))
                .build();
    }

    @DeleteMapping("/{orderLineId}")
    public ApiResponse<String> removeOrderLine(@PathVariable Long orderLineId) {
        orderLineService.removeOrderLine(orderLineId);
        return ApiResponse.<String>builder()
                .message("Order line removed")
                .data(null)
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderLineResponse>> getOrderLines(@PathVariable Long orderId) {
        return ApiResponse.<List<OrderLineResponse>>builder()
                .data(orderLineService.getOrderLines(orderId))
                .build();
    }
}
