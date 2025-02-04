package vdtry06.springboot.ecommerce.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.request.order.OrderRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderResponse;
import vdtry06.springboot.ecommerce.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.createOrder(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orderResponses = orderService.findAllOrders();
        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderResponses)
                .build();
    }

    @GetMapping("/{order-id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable("order-id") Long orderId) {
        OrderResponse orderResponse = orderService.findOrderById(orderId);
        return ApiResponse.<OrderResponse>builder()
                .data(orderResponse)
                .build();
    }
}
