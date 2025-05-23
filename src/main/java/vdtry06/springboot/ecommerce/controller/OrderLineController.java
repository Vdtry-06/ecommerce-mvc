package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.service.OrderLineService;
import vdtry06.springboot.ecommerce.dto.request.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.OrderLineResponse;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderLineController {
    OrderLineService orderLineService;

    @PostMapping("/{orderId}/order-lines")
    public ApiResponse<OrderLineResponse> addOrderLine(@PathVariable Long orderId, @RequestBody OrderLineRequest request) {
        return ApiResponse.<OrderLineResponse>builder()
                .data(orderLineService.addOrderLine(orderId, request))
                .build();
    }

    @PutMapping("/{orderId}/order-lines/{orderLineId}")
    public ApiResponse<OrderLineResponse> updateOrderLine(@PathVariable Long orderId, @PathVariable Long orderLineId, @RequestBody OrderLineRequest request) {
        return ApiResponse.<OrderLineResponse>builder()
                .data(orderLineService.updateOrderLine(orderId, orderLineId, request))
                .build();
    }

    @DeleteMapping("{orderId}/order-lines/{orderLineId}")
    public ApiResponse<String> removeOrderLine(@PathVariable Long orderId, @PathVariable Long orderLineId) {
        orderLineService.removeOrderLine(orderId, orderLineId);
        return ApiResponse.<String>builder()
                .message("Order line removed")
                .data(null)
                .build();
    }

    @GetMapping("/{orderId}/order-lines")
    public ApiResponse<List<OrderLineResponse>> getOrderLines(@PathVariable Long orderId) {
        return ApiResponse.<List<OrderLineResponse>>builder()
                .data(orderLineService.getOrderLines(orderId))
                .build();
    }
}
