package vdtry06.springboot.ecommerce.topping;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.core.ApiResponse;
import vdtry06.springboot.ecommerce.topping.dto.ToppingRequest;
import vdtry06.springboot.ecommerce.topping.dto.ToppingResponse;

import java.util.List;

@RestController
@RequestMapping("/topping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ToppingController {
    ToppingService toppingService;

    @PostMapping("/add")
    public ApiResponse<ToppingResponse> addTopping(@RequestBody ToppingRequest toppingRequest) {
        return ApiResponse.<ToppingResponse>builder()
                .data(toppingService.addTopping(toppingRequest))
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<ToppingResponse> updateTopping(@PathVariable Long id, @RequestBody ToppingRequest request) {
        ToppingResponse response = toppingService.updateTopping(id, request);
        return ApiResponse.<ToppingResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/get-topping/{id}")
    public ApiResponse<ToppingResponse> getTopping(@PathVariable Long id) {
        ToppingResponse response = toppingService.getTopping(id);
        return ApiResponse.<ToppingResponse>builder()
                .data(response)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteTopping(@PathVariable Long id) {
        toppingService.deleteTopping(id);
        return ApiResponse.<String>builder()
                .data(String.format("Topping with id %s deleted", id))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<ToppingResponse>> getAllToppings() {
        List<ToppingResponse> toppings = toppingService.getAllToppings();
        return ApiResponse.<List<ToppingResponse>>builder()
                .data(toppings)
                .build();
    }
}
