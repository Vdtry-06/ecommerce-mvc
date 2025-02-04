package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.order.OrderLineRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderLineResponse;
import vdtry06.springboot.ecommerce.service.OrderLineService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order-lines")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderLineController {
    OrderLineService orderLineService;

    @PostMapping
    public ApiResponse<Long> saveOrderLine(@RequestBody OrderLineRequest request) {
        return ApiResponse.<Long>builder()
                .data(orderLineService.saveOrderLine(request))
                .build();
    }

    @GetMapping("/{order-id}")
    public ApiResponse<List<OrderLineResponse>> findAllByOrderId(@PathVariable("order-id") Long orderId) {
        return ApiResponse.<List<OrderLineResponse>>builder()
                .data(orderLineService.findAllByOrderId(orderId))
                .build();
    }
}
