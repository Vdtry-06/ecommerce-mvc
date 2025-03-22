package vdtry06.springboot.ecommerce.order;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.order.dto.OrderRequest;
import vdtry06.springboot.ecommerce.order.dto.OrderResponse;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        return ApiResponse.<OrderResponse>builder()
                .message("Order created successfully")
                .data(orderResponse)
                .build();
    }
    @GetMapping("/user-id/{userId}")
    public ApiResponse<List<OrderResponse>> getAllOrdersOfUser(@PathVariable Long userId) {
        List<OrderResponse> orderResponse = orderService.getAllOrdersOfUser(userId);
        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderResponse)
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse orderResponse = orderService.getOrderById(orderId);
        return ApiResponse.<OrderResponse>builder()
                .message("Order fetched successfully")
                .data(orderResponse)
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orderResponseList = orderService.getAllOrders();
        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderResponseList)
                .build();
    }

    @DeleteMapping("/delete/{orderId}")
    public ApiResponse<List<OrderResponse>> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        List<OrderResponse> orderResponseList = orderService.getAllOrders();
        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderResponseList)
                .build();
    }


//    @PutMapping("/{orderId}/status/paid")
//    public ApiResponse<OrderResponse> updateOrderStatusToPaid(@PathVariable Long orderId) {
//        OrderResponse orderResponse = orderService.updateOrderStatusToPaid(orderId);
//        return ApiResponse.<OrderResponse>builder()
//                .message("Order status updated to PAID successfully")
//                .data(orderResponse)
//                .build();
//    }
//
//    @PutMapping("/{orderId}/status/delivered")
//    public ApiResponse<OrderResponse> updateOrderStatusToDelivered(@PathVariable Long orderId) {
//        OrderResponse orderResponse =  orderService.updateOrderStatusToDelivered(orderId);
//        return ApiResponse.<OrderResponse>builder()
//                .message("Order status updated to DELIVERED successfully")
//                .data(orderResponse)
//                .build();
//    }
}
